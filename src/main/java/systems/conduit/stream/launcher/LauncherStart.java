package systems.conduit.stream.launcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cpw.mods.modlauncher.Launcher;
import systems.conduit.stream.*;
import systems.conduit.stream.json.JsonStream;
import systems.conduit.stream.json.download.JsonLibraries;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public class LauncherStart {

    public static final List<String> MIXINS = new ArrayList<>();
    public static final List<Path> PATHS = new ArrayList<>();

    public static void main(String... args) {
        // TODO MAKE CONFIG
        Constants.Side side = Constants.Side.SERVER;
        // Defaults
        System.setProperty("http.agent", Constants.USER_AGENT);
        Logger.shouldUseLogger = true;
        System.out.println("Starting launcher...");
        // Register our jars to class path
        final Callback<File> registerJar = Agent::addClassPath;
        // Download required libraries
        SharedLaunch.downloadRequiredLibraries(null, registerJar);
        // Download default libraries
        SharedLaunch.downloadDefaultLibraries(null, registerJar);
        // Add Stream json if does not exist
        if (!Constants.STREAM_JSON_PATH.toFile().exists() && !Constants.DEBUG) {
            try (InputStream inputStream = SharedLaunch.class.getResourceAsStream("/" + Constants.STREAM_JSON)) {
                Files.copy(inputStream, Constants.STREAM_JSON_PATH, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Logger.exception("Error copying Stream json", e);
                System.exit(0);
            }
        }
        // Load Stream from json to class
        JsonStream stream = null;
        if (Constants.STREAM_JSON_PATH.toFile().exists() && !Constants.DEBUG) {
            try (BufferedReader reader = new BufferedReader(new FileReader(Constants.STREAM_JSON_PATH.toFile()))) {
                Gson gson = new GsonBuilder().create();
                stream = gson.fromJson(reader, JsonStream.class);
            } catch (IOException e) {
                Logger.exception("Error reading Stream json", e);
                System.exit(0);
            }
        }

        // Download / load minecraft libraries and download and remap minecraft if need to
        Logger.info("Setting up Minecraft");
        if (!Constants.DEBUG) {
            if (stream != null) {
                SharedLaunch.setupMinecraft(side, null, stream.getMinecraft().getVersion(), registerJar);
            } else {
                Logger.fatal("Error parsing stream json!");
                System.exit(0);
            }
        } else {
            SharedLaunch.setupMinecraft(side, null, Constants.MINECRAFT_VERSION, registerJar);
        }
        Logger.info("Set up Minecraft");
        // Load minecraft
        if (!Constants.DEBUG) {
            Logger.info("Loading Minecraft remapped");
            LauncherStart.PATHS.add(Constants.MAPPED_JAR_PATH);
            Logger.info("Loaded Minecraft remapped");
        }
        // Create the mixins folder
        if (!Constants.MIXINS_PATH.toFile().exists() && !Constants.MIXINS_PATH.toFile().mkdirs()) {
            Logger.fatal("Failed to make .mixins directory");
        }
        // Download conduit
        if (stream != null && !Constants.DEBUG) {
            Constants.setConduitPaths(stream.getConduit().getVersion());
            if (stream.getConduit().shouldDownload()) {
                if (!Constants.CONDUIT_MIXIN_PATH.toFile().exists()) {
                    Logger.info("Downloading Conduit-" + Constants.CONDUIT_VERSION);
                    try {
                        SharedLaunch.downloadFile(new URL(Constants.CONDUIT_DOWNLOAD_PATH), Constants.CONDUIT_MIXIN_PATH.toFile());
                    } catch (IOException e) {
                        Logger.exception("Unable to download Conduit!", e);
                        // Don't exit here. Possibly they have old conduit version already.
                    }
                }
            }
        }
        // Load Mixins
        if (Constants.MIXINS_PATH.toFile().exists()) {
            File[] mixinFiles = Constants.MIXINS_PATH.toFile().listFiles();
            if (mixinFiles != null) {
                for (File file : mixinFiles) {
                    // Skip folders
                    if (!file.isFile()) continue;
                    // Make sure that it ends with .jar
                    if (!file.getName().endsWith(".jar")) continue;
                    // Since it is a file, and it ends with .jar, we can proceed with attempting to load it.
                    String properFileName = file.getName().substring(0, file.getName().length() - 4);
                    try {
                        // Get jar file
                        JarFile jarFile = new JarFile(file);
                        // Load libraries from json
                        ZipEntry libZip = jarFile.getEntry("libraries.json");
                        if (libZip != null) {
                            Logger.info("Found libraries.json: " + properFileName);
                            try (Reader reader = new InputStreamReader(jarFile.getInputStream(libZip))) {
                                Gson gson = new GsonBuilder().create();
                                JsonLibraries libraries = gson.fromJson(reader, JsonLibraries.class);
                                Logger.info("Loading libraries.json: " + properFileName);
                                LibraryProcessor.downloadLibrary(properFileName + " libraries", null, libraries.getLibs(), registerJar);
                            }
                        }
                        // Find all mixins for a jar.
                        List<String> mixinsJson = findMixinEntry(jarFile);
                        mixinsJson = mixinsJson.stream().distinct().collect(Collectors.toList());
                        if (!mixinsJson.isEmpty()) MIXINS.addAll(mixinsJson);
                        // Add to class loader
                        PATHS.add(file.toPath());
                    } catch (IOException e) {
                        Logger.exception("Error loading mixin (" + properFileName + ")", e);
                        System.exit(0);
                    }
                    Logger.info("Loaded mixin: " + properFileName);
                }
            }
        }
        // Start modlauncher
        Logger.info("Starting modlauncher...");
        Launcher.main(Stream.concat(Stream.of("--launchTarget", "minecraft-" + side.name().toLowerCase()), Arrays.stream(args)).toArray(String[]::new));
    }

    private static List<String> findMixinEntry(JarFile file) {
        List<String> mixins = new ArrayList<>();
        for (final Enumeration<? extends ZipEntry> e = file.entries(); e.hasMoreElements();) {
            final ZipEntry ze = e.nextElement();
            if (!ze.isDirectory()) {
                final String name = ze.getName();
                if (name.startsWith("mixins.") && name.endsWith(".json") && name.contains(Constants.MINECRAFT_VERSION.replace(".", ""))) {
                    mixins.add(name);
                } else if (name.equals("mixin_dependencies.json")) {
                    // If we have found a dependency file, load and see if this version of the game has any mixin dependencies.
                    ZipEntry mixinDependencies = file.getEntry(name);
                    if (mixinDependencies != null) {
                        try (Reader reader = new InputStreamReader(file.getInputStream(mixinDependencies))) {
                            Gson gson = new GsonBuilder().create();
                            Map<String, List<String>> mixinDependenciesMap = gson.fromJson(reader, Map.class);
                            List<String> requiredMixins = mixinDependenciesMap.getOrDefault(Constants.MINECRAFT_VERSION,
                                    mixinDependenciesMap.getOrDefault(String.join(".", Arrays.stream(Constants.MINECRAFT_VERSION
                                            .split("\\.")).skip(1).collect(Collectors.toList()).toArray(new String[] {})), new ArrayList<>()));
                            requiredMixins = requiredMixins.stream().map(mixinName -> "mixins.conduit." + mixinName.replaceAll("\\.", "") + ".json").distinct().collect(Collectors.toList());
                            mixins.addAll(requiredMixins);
                        } catch (IOException ioException) {
                            ioException.printStackTrace();
                        }
                    }
                }
            }
        }
        return mixins;
    }
}
