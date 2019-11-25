package systems.conduit.stream.launcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cpw.mods.modlauncher.Launcher;
import systems.conduit.stream.*;
import systems.conduit.stream.json.download.JsonLibraries;
import systems.conduit.stream.json.minecraft.JsonMinecraft;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public class LauncherStart {

    public static final List<String> MIXINS = new ArrayList<>();
    public static final List<Path> PATHS = new ArrayList<>();

    public static void main(String[] args) {
        System.setProperty("http.agent", Constants.USER_AGENT);
        Logger.shouldUseLogger = true;
        System.out.println("Starting launcher...");
        // Register our jars to class path
        final Callback<File> registerJar = Agent::addClassPath;
        // Download required libraries
        SharedLaunch.downloadRequiredLibraries(null, registerJar);
        // Download default libraries
        SharedLaunch.downloadDefaultLibraries(null, registerJar);
        // Add Minecraft json if does not exist
        if (!Constants.MINECRAFT_JSON_PATH.toFile().exists()) {
            try (InputStream inputStream = SharedLaunch.class.getResourceAsStream("/" + Constants.MINECRAFT_JSON)) {
                Files.copy(inputStream, Constants.MINECRAFT_JSON_PATH, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Logger.fatal("Error copying Minecraft json");
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
            Logger.fatal("Error reading Minecraft libraries json");
            e.printStackTrace();
            System.exit(0);
        }
        // Download/load minecraft libraries and download and remap minecraft if need to
        SharedLaunch.setupMinecraft(null, minecraft.getVersion(), registerJar);
        // Load minecraft
        Logger.info("Loading Minecraft remapped");
        LauncherStart.PATHS.add(Constants.SERVER_MAPPED_JAR_PATH);
        Logger.info("Loaded Minecraft remapped");
        // Create the mixins folder
        if (!Constants.MIXINS_PATH.toFile().exists() && !Constants.MIXINS_PATH.toFile().mkdirs()) {
            Logger.fatal("Failed to make mixins directory");
            System.exit(0);
        }
        // TODO: Download Conduit from a config?
        // Load Mixins
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
                    if (!mixinsJson.isEmpty()) {
                        MIXINS.addAll(mixinsJson);
                    }
                    // Add to class loader
                    PATHS.add(file.toPath());
                } catch (IOException e) {
                    Logger.fatal("Error loading mixin (" + properFileName + ")");
                    e.printStackTrace();
                    System.exit(0);
                }
                Logger.info("Loaded mixin: " + properFileName);
            }
        }
        // Start modlauncher
        Logger.info("Starting modlauncher...");
        Launcher.main(Stream.concat(Stream.of("--launchTarget", "minecraft-server"), Arrays.stream(args)).toArray(String[]::new));
    }

    private static List<String> findMixinEntry(JarFile file) {
        List<String> mixins = new ArrayList<>();
        for (final Enumeration<? extends ZipEntry> e = file.entries(); e.hasMoreElements();) {
            final ZipEntry ze = e.nextElement();
            if (!ze.isDirectory()) {
                final String name = ze.getName();
                if (name.startsWith("mixins.") && name.endsWith(".json")) {
                    mixins.add(name);
                }
            }
        }
        return mixins;
    }
}
