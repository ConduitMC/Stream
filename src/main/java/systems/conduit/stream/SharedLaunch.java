package systems.conduit.stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.log4j.LogManager;
import systems.conduit.stream.gradle.SpecialSourceClassLoader;
import systems.conduit.stream.json.download.JsonLibraries;
import systems.conduit.stream.json.download.JsonLibraryInfo;
import systems.conduit.stream.json.minecraft.JsonMinecraft;
import systems.conduit.stream.json.minecraft.MinecraftLibrary;
import systems.conduit.stream.json.minecraft.MinecraftVersion;
import systems.conduit.stream.json.minecraft.manifest.MinecraftVersionManifest;
import systems.conduit.stream.json.minecraft.manifest.MinecraftVersionManifestType;
import systems.conduit.stream.launcher.Agent;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

public class SharedLaunch {

    public static List<URL> specialSourcePaths = new ArrayList<>();

    public static void start(Path basePath, Object projectObject) {
        // Load logger libraries
        LibraryProcessor.downloadLibrary("logger libraries", true, basePath, projectObject, Arrays.asList(
                new JsonLibraryInfo("maven", "org.apache.logging.log4j", "log4j-api", "2.8.1", ""),
                new JsonLibraryInfo("maven", "org.apache.logging.log4j", "log4j-core", "2.8.1", "")
        ));
        // Load json library
        LibraryProcessor.downloadLibrary("json library", false, basePath, projectObject, Collections.singletonList(
                new JsonLibraryInfo("maven", "com.google.code.gson", "gson", "2.8.0", "")
        ));
        // Load default libraries from json to class
        JsonLibraries defaults = new JsonLibraries();
        try (Reader reader = new InputStreamReader(SharedLaunch.class.getResourceAsStream("/" + Constants.DEFAULTS_JSON), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().create();
            defaults = gson.fromJson(reader, JsonLibraries.class);
        } catch (IOException e) {
            fatal(basePath, "Error reading default libraries json");
            e.printStackTrace();
            System.exit(0);
        }
        // Download all the default libraries
        LibraryProcessor.downloadLibrary("default libraries", false, basePath, projectObject, defaults.getLibs());
        Constants.setMinecraftJsonPath(basePath);
        // Add Minecraft json if does not exist
        if (!Constants.MINECRAFT_JSON_PATH.toFile().exists()) {
            try (InputStream inputStream = SharedLaunch.class.getResourceAsStream("/" + Constants.MINECRAFT_JSON)) {
                Files.copy(inputStream, Constants.MINECRAFT_JSON_PATH, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                fatal(basePath, "Error copying Minecraft json");
                e.printStackTrace();
                System.exit(0);
            }
        }
        // Load Minecraft from json to class
        JsonMinecraft minecraft = new JsonMinecraft();
        try (BufferedReader reader = new BufferedReader(new FileReader(Constants.MINECRAFT_JSON_PATH.toFile()))) {
            Gson gson = new GsonBuilder().create();
            minecraft = gson.fromJson(reader, JsonMinecraft.class);
        } catch (IOException e) {
            fatal(basePath, "Error reading Minecraft libraries json");
            e.printStackTrace();
            System.exit(0);
        }
        // Set correct paths
        Constants.setPaths(basePath, minecraft.getVersion());
        // Make sure we have the correct directories
        if (!Constants.MINECRAFT_PATH.toFile().exists() && !Constants.MINECRAFT_PATH.toFile().mkdirs()) {
            fatal(basePath, "Failed to make minecraft directory");
            System.exit(0);
        }
        if (!Constants.VERSION_JSON_PATH.toFile().exists()) {
            // Read manifest and get version url
            try (InputStreamReader manifestReader = new InputStreamReader(new URL(Constants.VERSION_MANIFEST_ENDPOINT).openStream())) {
                MinecraftVersionManifest manifest = new Gson().fromJson(manifestReader, MinecraftVersionManifest.class);
                Optional<MinecraftVersionManifestType> versionInfo = getVersion(manifest, Constants.MINECRAFT_VERSION);
                // Read version json and get server info
                if (versionInfo.isPresent() && versionInfo.get().getUrl() != null && !versionInfo.get().getUrl().isEmpty()) {
                    try {
                        info(basePath, "Downloading version json");
                        downloadFile(new URL(versionInfo.get().getUrl()), Constants.VERSION_JSON_PATH.toFile());
                    } catch (IOException e) {
                        fatal(basePath, "Error creating version json url");
                        e.printStackTrace();
                        System.exit(0);
                    }
                } else {
                    fatal(basePath, "Unable to get version info for Minecraft (" + Constants.MINECRAFT_VERSION + ")");
                    System.exit(0);
                }
            } catch (IOException e) {
                fatal(basePath, "Error reading Minecraft manifest url");
                e.printStackTrace();
                System.exit(0);
            }
        }
        // Load from version file
        MinecraftVersion minecraftVersion = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(Constants.VERSION_JSON_PATH.toFile()))) {
            Gson gson = new GsonBuilder().create();
            minecraftVersion = gson.fromJson(reader, MinecraftVersion.class);
        } catch (IOException e) {
            fatal(basePath, "Error reading Minecraft version json");
            e.printStackTrace();
            System.exit(0);
        }
        if (minecraftVersion == null) {
            fatal(basePath, "Error finding Minecraft version json");
            System.exit(0);
        }
        // Download libraries
        List<JsonLibraryInfo> minecraftLibraries = new ArrayList<>();
        for (MinecraftLibrary minecraftLibrary : minecraftVersion.getLibraries()) {
            // Should not need mac only for a server. I think?
            if (!minecraftLibrary.isMac()) {
                String[] minecraftLib = minecraftLibrary.getName().split(":");
                minecraftLibraries.add(new JsonLibraryInfo("maven", minecraftLib[0], minecraftLib[1], minecraftLib[2], Constants.MINECRAFT_REPO));
            }
        }
        // Download all the Minecraft libraries
        LibraryProcessor.downloadLibrary("Minecraft libraries", false, basePath, projectObject, minecraftLibraries);
        // Download Minecraft and patch if we don't have the file
        if (!Constants.SERVER_MAPPED_JAR_PATH.toFile().exists()) {
            // Download server
            if (!minecraftVersion.getDownloads().getServer().getUrl().isEmpty()) {
                try {
                    info(basePath, "Downloading Minecraft server (" + Constants.MINECRAFT_VERSION + ")");
                    downloadFile(new URL(minecraftVersion.getDownloads().getServer().getUrl()), Constants.SERVER_JAR_PATH.toFile());
                } catch (IOException e) {
                    fatal(basePath, "Error creating server url");
                    e.printStackTrace();
                    System.exit(0);
                }
            } else {
                fatal(basePath, "Error reading Minecraft server url");
                System.exit(0);
            }
            // Cleanup Minecraft
            info(basePath, "Cleaning up Minecraft");
            deleteMinecraftTrash(Constants.SERVER_JAR_PATH.toFile());
            info(basePath, "Cleaned up Minecraft");
            // Download server mappings
            if (!minecraftVersion.getDownloads().getServerMappings().getUrl().isEmpty()) {
                try {
                    info(basePath, "Downloading server mappings");
                    downloadFile(new URL(minecraftVersion.getDownloads().getServerMappings().getUrl()), Constants.SERVER_MAPPINGS_PATH.toFile());
                } catch (IOException e) {
                    fatal(basePath, "Error creating server mappings url");
                    e.printStackTrace();
                    System.exit(0);
                }
            } else {
                fatal(basePath, "Error reading Minecraft server mappings url");
                System.exit(0);
            }
            // Convert Minecraft mappings
            info(basePath, "Converting Minecraft mappings");
            Mojang2Tsrg m2t = new Mojang2Tsrg();
            try {
                m2t.loadClasses(Constants.SERVER_MAPPINGS_PATH.toFile());
                m2t.writeTsrg(Constants.SERVER_MAPPINGS_PATH.toFile(), Constants.SERVER_MAPPINGS_CONVERTED_PATH.toFile());
            } catch (IOException e) {
                fatal(basePath, "Error converting Minecraft server mappings");
                e.printStackTrace();
                System.exit(0);
            }
            Constants.SERVER_MAPPINGS_PATH.toFile().delete();
            // Remapping Minecraft
            info(basePath, "Remapping Minecraft (This might take a bit)");
            ClassLoader classLoader = ClassLoader.getSystemClassLoader();
            if (basePath != null) {
                classLoader = new SpecialSourceClassLoader(specialSourcePaths.toArray(new URL[]{}), classLoader);
            }
            String[] specialSourceArgs = Stream.of(
                    "--in-jar", Constants.SERVER_JAR_PATH.toFile().getAbsolutePath(),
                    "--out-jar", Constants.SERVER_MAPPED_JAR_PATH.toFile().getAbsolutePath(),
                    "--srg-in", Constants.SERVER_MAPPINGS_CONVERTED_PATH.toFile().getAbsolutePath(),
                    "--quiet"
            ).toArray(String[]::new);
            try {

                Class<?> cls = Class.forName("net.md_5.specialsource.SpecialSource", true, classLoader);
                Method method = cls.getMethod("main", String[].class);
                method.invoke(null, (Object) specialSourceArgs);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | ClassNotFoundException e) {
                fatal(null, "Error remapping Minecraft");
                e.printStackTrace();
                System.exit(0);
            }
            Constants.SERVER_JAR_PATH.toFile().delete();
            Constants.SERVER_MAPPINGS_CONVERTED_PATH.toFile().delete();
            info(basePath, "Remapped Minecraft");
        }
        // Load Minecraft
        if (basePath == null) {
            info(null, "Loading Minecraft remapped");
            Agent.addClassPath(Constants.SERVER_MAPPED_JAR_PATH.toFile());
            info(null, "Loaded Minecraft remapped");
        } else {
            try {
                if (projectObject != null) {
                    org.gradle.api.Project project = ((org.gradle.api.Project) projectObject);
                    org.gradle.api.artifacts.Dependency dependency = project.getDependencies().create(((org.gradle.api.Project) projectObject).files(Constants.SERVER_MAPPED_JAR_PATH.toFile().toURI().toURL()));
                    project.getDependencies().add(Constants.GRADLE_CONFIGURATION_API, dependency);
                }
            } catch (MalformedURLException e) {
                fatal(basePath, "Error adding Minecraft server");
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    private static void info(Path basePath, String message) {
        if (basePath == null) {
            LogManager.getLogger(Constants.LOGGER_NAME).info(message);
        } else {
            System.out.println(message);
        }
    }

    private static void fatal(Path basePath, String message) {
        if (basePath == null) {
            LogManager.getLogger(Constants.LOGGER_NAME).fatal(message);
        } else {
            System.out.println(message);
        }
    }

    private static Optional<MinecraftVersionManifestType> getVersion(MinecraftVersionManifest manifest, String version) {
        return manifest.getVersions().stream().filter(type -> type.getId().equals(version)).findFirst();
    }

    private static void deleteMinecraftTrash(File file) {
        Map<String, String> zipProperties = new HashMap<>();
        zipProperties.put("create", "false");
        try (FileSystem zipFS = FileSystems.newFileSystem(URI.create("jar:" + file.toURI().toString()), zipProperties)) {
            Path[] allTheTrash = new Path[] {
                    zipFS.getPath("com"), zipFS.getPath("io"), zipFS.getPath("it"),
                    zipFS.getPath("javax"), zipFS.getPath("joptsimple"), zipFS.getPath("org")
            };
            for (Path trash : allTheTrash) {
                delete(trash);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void delete(Path directory) {
        try {
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignored) {
        }
    }

    public static void downloadFile(URL url, File location) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            Files.copy(inputStream, location.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
