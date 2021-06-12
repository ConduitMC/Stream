package systems.conduit.stream;


import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants {

    // Default versions. Will changed down the line by the launcher or gradle.
    public static String MINECRAFT_VERSION = "1.16.1";
    public static String CONDUIT_VERSION = "0.0.5";
    public static String STREAM_VERSION = "@VERSION@";

    public static boolean DEBUG = false;

    public static final String LOGGER_NAME = "Launcher";

    public static final String DEFAULTS_JSON = "defaults.json";

    public static final String STREAM_JSON = "stream.json";
    public static final Path STREAM_JSON_PATH = Paths.get(STREAM_JSON);

    public static final Path STREAM_CACHE = Paths.get("cache");
    public static Path MINECRAFT_PATH = Paths.get(".minecraft");

    // TODO: Detect side and use .libs for server
    public static final Path LIBRARIES_PATH = Paths.get("libraries");

    public static Path VERSION_JSON_PATH;
    public static Path JAR_PATH;
    public static Path MAPPED_JAR_PATH;
    public static Path MAPPINGS_PATH;
    public static Path MAPPINGS_CONVERTED_PATH;

    public static final Path MIXINS_PATH = Paths.get(".mixins");
    public static Path CONDUIT_MIXIN_PATH;
    public static String CONDUIT_DOWNLOAD_PATH;

    public static final String VERSION_MANIFEST_ENDPOINT = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    public static final String CONDUIT_REPO = "https://repo.conduit.systems/repository/releases/";
    public static final String MINECRAFT_REPO = "https://libraries.minecraft.net/";
    public static final String DEFAULT_REPO = "https://repo.maven.apache.org/maven2/";

    public static final String LOMBOK_DEPENDENCY = "org.projectlombok:lombok:1.18.10";
    public static final String CONDUIT_DEPENDENCY = "systems.conduit:Conduit:";
    public static final String STREAM_DEPENDENCY = "systems.conduit:Stream:";
    public static final String GRADLE_CONFIGURATION_ANNOTATION = "annotationProcessor";
    public static final String GRADLE_CONFIGURATION_API = "api";

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";

    public static final String MAIN_SERVER_FILE = "net.minecraft.server.Main";
    public static final String MAIN_CLIENT_FILE = "net.minecraft.client.main.Main";
    public static final String MAIN_CONDUIT_FILE = "systems.conduit.main.Conduit";

    public static void setMinecraftPaths(Side side, Path basePath, String minecraftVersion) {
        MINECRAFT_VERSION = minecraftVersion;
        if (basePath != null) MINECRAFT_PATH = basePath.resolve(MINECRAFT_PATH);
        VERSION_JSON_PATH = MINECRAFT_PATH.resolve(MINECRAFT_VERSION + ".json");
        JAR_PATH = MINECRAFT_PATH.resolve(side.name().toLowerCase() + "-" + MINECRAFT_VERSION + ".jar");
        MAPPED_JAR_PATH = MINECRAFT_PATH.resolve(side.name().toLowerCase() + "-" + MINECRAFT_VERSION + "-remapped.jar");
        MAPPINGS_PATH = MINECRAFT_PATH.resolve(side.name().toLowerCase() + "-" + MINECRAFT_VERSION + "-mappings.txt");
        MAPPINGS_CONVERTED_PATH =  MINECRAFT_PATH.resolve(side.name().toLowerCase() + "-" + MINECRAFT_VERSION + "-mappings-converted.txt");
    }

    public static void setConduitPaths(String conduitVersion) {
        CONDUIT_VERSION = conduitVersion;
        CONDUIT_MIXIN_PATH = MIXINS_PATH.resolve("Conduit-" + CONDUIT_VERSION + ".jar");
        CONDUIT_DOWNLOAD_PATH = CONDUIT_REPO + "systems/conduit/Conduit/" + CONDUIT_VERSION +  "/Conduit-" + CONDUIT_VERSION + ".jar";
    }

    public enum Side {
        CLIENT,
        SERVER
    }
}
