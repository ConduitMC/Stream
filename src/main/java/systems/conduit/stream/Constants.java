package systems.conduit.stream;


import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants {

    // Default versions. Will changed down the line by the launcher or gradle.
    public static String MINECRAFT_VERSION = "1.14.4";
    public static String CONDUIT_VERSION = "0.0.3";

    public static final String LOGGER_NAME = "Launcher";

    public static final String DEFAULTS_JSON = "defaults.json";

    public static final String STREAM_JSON = "stream.json";
    public static final Path STREAM_JSON_PATH = Paths.get(STREAM_JSON);

    public static final Path CONDUIT_CACHE = Paths.get("caches", "conduit");
    public static Path MINECRAFT_PATH = Paths.get(".minecraft");

    public static final Path LIBRARIES_PATH = Paths.get(".libs");

    public static Path VERSION_JSON_PATH;
    public static Path SERVER_JAR_PATH;
    public static Path SERVER_MAPPED_JAR_PATH;
    public static Path SERVER_MAPPINGS_PATH;
    public static Path SERVER_MAPPINGS_CONVERTED_PATH;

    public static final Path MIXINS_PATH = Paths.get(".mixins");
    public static Path CONDUIT_MIXIN_PATH;
    public static String CONDUIT_DOWNLOAD_PATH;

    public static final String VERSION_MANIFEST_ENDPOINT = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    public static final String CONDUIT_REPO = "https://repo.conduit.systems/repository/releases/";
    public static final String MINECRAFT_REPO = "https://libraries.minecraft.net/";
    public static final String DEFAULT_REPO = "https://jcenter.bintray.com/";

    public static final String LOMBOK_DEPENDENCY = "org.projectlombok:lombok:1.18.10";
    public static final String CONDUIT_DEPENDENCY = "systems.conduit:Conduit:";
    public static final String GRADLE_CONFIGURATION_ANNOTATION = "annotationProcessor";
    public static final String GRADLE_CONFIGURATION_API = "api";

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";

    public static void setMinecraftPaths(Path basePath, String minecraftVersion) {
        MINECRAFT_VERSION = minecraftVersion;
        if (basePath != null) MINECRAFT_PATH = basePath.resolve(MINECRAFT_PATH);
        VERSION_JSON_PATH = MINECRAFT_PATH.resolve(MINECRAFT_VERSION + ".json");
        SERVER_JAR_PATH = MINECRAFT_PATH.resolve("server-" + MINECRAFT_VERSION + ".jar");
        SERVER_MAPPED_JAR_PATH = MINECRAFT_PATH.resolve("server-" + MINECRAFT_VERSION + "-remapped.jar");
        SERVER_MAPPINGS_PATH = MINECRAFT_PATH.resolve("server-" + MINECRAFT_VERSION + "-mappings.txt");
        SERVER_MAPPINGS_CONVERTED_PATH =  MINECRAFT_PATH.resolve("server-" + MINECRAFT_VERSION + "-mappings-converted.txt");
    }

    public static void setConduitPaths(String conduitVersion) {
        CONDUIT_VERSION = conduitVersion;
        CONDUIT_MIXIN_PATH = MIXINS_PATH.resolve("Conduit-" + CONDUIT_VERSION + ".jar");
        CONDUIT_DOWNLOAD_PATH = CONDUIT_REPO + "systems/conduit/Conduit/" + CONDUIT_VERSION +  "/Conduit-" + CONDUIT_VERSION + ".jar";
    }
}
