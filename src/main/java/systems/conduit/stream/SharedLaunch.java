package systems.conduit.stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import systems.conduit.stream.json.download.JsonLibraries;
import systems.conduit.stream.json.download.JsonLibraryInfo;
import systems.conduit.stream.json.minecraft.MinecraftLibrary;
import systems.conduit.stream.json.minecraft.MinecraftVersion;
import systems.conduit.stream.json.minecraft.manifest.MinecraftVersionManifest;
import systems.conduit.stream.json.minecraft.manifest.MinecraftVersionManifestType;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Stream;

public class SharedLaunch {

    public static List<URL> specialSourcePaths = new ArrayList<>();

    public static void downloadRequiredLibraries(Path basePath, Callback<File> callback) {
        LibraryProcessor.resetArtifacts();
        // Load logger libraries
        LibraryProcessor.downloadLibrary("logger libraries", basePath, Arrays.asList(
                new JsonLibraryInfo("maven", "org.apache.logging.log4j", "log4j-api", "2.11.2", ""),
                new JsonLibraryInfo("maven", "org.apache.logging.log4j", "log4j-core", "2.11.2", "")
        ), callback);
        // Load json library
        LibraryProcessor.downloadLibrary("json library", basePath, Collections.singletonList(
                new JsonLibraryInfo("maven", "com.google.code.gson", "gson", "2.8.0", "")
        ), callback);
    }

    public static void downloadDefaultLibraries(Path basePath, Callback<File> callback) {
        // Load default libraries from json to class
        JsonLibraries defaults = new JsonLibraries(new ArrayList<>());
        try (Reader reader = new InputStreamReader(SharedLaunch.class.getResourceAsStream("/" + Constants.DEFAULTS_JSON), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().create();
            defaults = gson.fromJson(reader, JsonLibraries.class);
        } catch (IOException e) {
            Logger.fatal("Error reading default libraries json");
            e.printStackTrace();
            System.exit(0);
        }
        // Download all the default libraries
        LibraryProcessor.downloadLibrary("default libraries", basePath, defaults.getLibs(), callback);
    }

    public static void setupMinecraft(Path basePath, String version, Callback<File> callback) {
        if (version == null) {
            Logger.fatal("Can not find minecraft version");
            System.exit(0);
        }
        // Set correct paths
        Constants.setMinecraftPaths(basePath, version);
        // Make sure we have the correct directories
        if (!Constants.MINECRAFT_PATH.toFile().exists() && !Constants.MINECRAFT_PATH.toFile().mkdirs()) {
            Logger.fatal("Failed to make minecraft directory");
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
                        Logger.info("Downloading version json");
                        downloadFile(new URL(versionInfo.get().getUrl()), Constants.VERSION_JSON_PATH.toFile());
                    } catch (IOException e) {
                        Logger.fatal("Error creating version json url");
                        e.printStackTrace();
                        System.exit(0);
                    }
                } else {
                    Logger.fatal("Unable to get version info for Minecraft (" + Constants.MINECRAFT_VERSION + ")");
                    System.exit(0);
                }
            } catch (IOException e) {
                Logger.fatal("Error reading Minecraft manifest url");
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
            Logger.fatal("Error reading Minecraft version json");
            e.printStackTrace();
            System.exit(0);
        }
        if (minecraftVersion == null) {
            Logger.fatal("Error finding Minecraft version json");
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
        LibraryProcessor.downloadLibrary("Minecraft libraries", basePath, minecraftLibraries, callback);
        // Download Minecraft and patch if we don't have the file
        if (!Constants.SERVER_MAPPED_JAR_PATH.toFile().exists()) {
            // Download server
            if (!minecraftVersion.getDownloads().getServer().getUrl().isEmpty()) {
                try {
                    Logger.info("Downloading Minecraft server (" + Constants.MINECRAFT_VERSION + ")");
                    downloadFile(new URL(minecraftVersion.getDownloads().getServer().getUrl()), Constants.SERVER_JAR_PATH.toFile());
                } catch (IOException e) {
                    Logger.fatal("Error creating server url");
                    e.printStackTrace();
                    System.exit(0);
                }
            } else {
                Logger.fatal("Error reading Minecraft server url");
                System.exit(0);
            }
            // Cleanup Minecraft
            Logger.info("Cleaning up Minecraft");
            deleteMinecraftTrash(Constants.SERVER_JAR_PATH.toFile());
            Logger.info("Cleaned up Minecraft");
            // Download server mappings
            if (!minecraftVersion.getDownloads().getServerMappings().getUrl().isEmpty()) {
                try {
                    Logger.info("Downloading server mappings");
                    downloadFile(new URL(minecraftVersion.getDownloads().getServerMappings().getUrl()), Constants.SERVER_MAPPINGS_PATH.toFile());
                } catch (IOException e) {
                    Logger.fatal("Error creating server mappings url");
                    e.printStackTrace();
                    System.exit(0);
                }
            } else {
                Logger.fatal("Error reading Minecraft server mappings url");
                System.exit(0);
            }
            // Convert Minecraft mappings
            Logger.info("Converting Minecraft mappings");
            new Mojang2Tsrg(Constants.SERVER_MAPPINGS_PATH, Constants.SERVER_MAPPINGS_CONVERTED_PATH);
            // Remapping Minecraft
            Logger.info("Remapping Minecraft (This might take a bit)");
            ClassLoader classLoader = new URLClassLoader(specialSourcePaths.toArray(new URL[]{}), ClassLoader.getSystemClassLoader());
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
                Logger.fatal("Error remapping Minecraft");
                e.printStackTrace();
                System.exit(0);
            }
            Constants.SERVER_JAR_PATH.toFile().delete();
            Constants.SERVER_MAPPINGS_CONVERTED_PATH.toFile().delete();
            Logger.info("Remapped Minecraft");
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
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.addRequestProperty("User-Agent", Constants.USER_AGENT);
        connection.setRequestMethod("GET");
        connection.connect();
        try (InputStream inputStream = connection.getInputStream()) {
            Files.copy(inputStream, location.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
