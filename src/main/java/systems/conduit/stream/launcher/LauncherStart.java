package systems.conduit.stream.launcher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import cpw.mods.modlauncher.Launcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import systems.conduit.stream.Constants;
import systems.conduit.stream.LibraryProcessor;
import systems.conduit.stream.SharedLaunch;
import systems.conduit.stream.json.download.JsonLibraries;
import systems.conduit.stream.json.mixins.JsonMixin;
import systems.conduit.stream.json.mixins.JsonMixins;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

public class LauncherStart {

    public static final List<String> MIXINS = new ArrayList<>();
    public static final List<Path> PATHS = new ArrayList<>();

    public static void main(String[] args) {
        System.setProperty("http.agent", Constants.USER_AGENT);
        System.out.println("Starting launcher...");
        // Start init
        SharedLaunch.start(null, null);
        // Get logger
        Logger logger = LogManager.getLogger(Constants.LOGGER_NAME);
        // Create the mixins folder
        if (!Constants.MIXINS_PATH.toFile().exists() && !Constants.MIXINS_PATH.toFile().mkdirs()) {
            logger.fatal("Failed to make mixins directory");
            System.exit(0);
        }
        // Load mixins json
        JsonMixins mixins = new JsonMixins();
        try (Reader reader = new InputStreamReader(SharedLaunch.class.getResourceAsStream("/mixins.json"), StandardCharsets.UTF_8)) {
            Gson gson = new GsonBuilder().create();
            mixins = gson.fromJson(reader, JsonMixins.class);
        } catch (IOException e) {
            logger.fatal("Error reading mixins json");
            e.printStackTrace();
            System.exit(0);
        }
        // Download Mixins
        if (!mixins.getMixins().isEmpty()) {
            for (JsonMixin mixin : mixins.getMixins()) {
                try {
                    File file = Constants.MIXINS_PATH.resolve(mixin.getName() + ".jar").toFile();
                    if (!file.exists() && mixin.getUrl() != null && !mixin.getUrl().trim().isEmpty()) {
                        logger.info("Downloading mixin (" + mixin.getName() +")");
                        SharedLaunch.downloadFile(new URL(mixin.getUrl()), file);
                    }
                } catch (IOException e) {
                    logger.fatal("Error downloading mixin (" + mixin.getName() + ")");
                    e.printStackTrace();
                    System.exit(0);
                }
            }
        }
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
                        logger.info("Found libraries.json: " + properFileName);
                        try (Reader reader = new InputStreamReader(jarFile.getInputStream(libZip))) {
                            Gson gson = new GsonBuilder().create();
                            JsonLibraries libraries = gson.fromJson(reader, JsonLibraries.class);
                            logger.info("Loading libraries.json: " + properFileName);
                            LibraryProcessor.downloadLibrary(properFileName + " libraries", false, null, null, libraries.getLibs());
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
                    logger.fatal("Error loading mixin (" + properFileName + ")");
                    e.printStackTrace();
                    System.exit(0);
                }
                logger.info("Loaded mixin: " + properFileName);
            }
        }
        // Start modlauncher
        logger.info("Starting modlauncher...");
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
