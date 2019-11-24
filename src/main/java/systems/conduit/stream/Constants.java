package systems.conduit.stream;


import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants {

    // Default version. Will get changed by the launcher!
    public static String MINECRAFT_VERSION = "1.14.4";

    public static String LOGGER_NAME = "Launcher";

    public static final String DEFAULTS_JSON = "defaults.json";

    public static final String MINECRAFT_JSON = "minecraft.json";
    public static Path MINECRAFT_JSON_PATH = Paths.get(MINECRAFT_JSON);

    public static Path CONDUIT_CACHE = Paths.get("caches", "conduit");
    public static Path MINECRAFT_PATH = Paths.get(".minecraft");
    public static final Path LIBRARIES_PATH = Paths.get(".libs");

    public static Path VERSION_JSON_PATH;
    public static Path SERVER_JAR_PATH;
    public static Path SERVER_MAPPED_JAR_PATH;
    public static Path SERVER_MAPPINGS_PATH;
    public static Path SERVER_MAPPINGS_CONVERTED_PATH;

    public static final Path MIXINS_PATH = Paths.get(".mixins");

    public static final String VERSION_MANIFEST_ENDPOINT = "https://launchermeta.mojang.com/mc/game/version_manifest.json";

    public static final String CONDUIT_REPO = "https://libraries.minecraft.net/";
    public static final String MINECRAFT_REPO = "https://libraries.minecraft.net/";
    public static final String DEFAULT_REPO = "https://jcenter.bintray.com/";

    public static final String LOMBOK_DEPENDENCY = "org.projectlombok:lombok:1.18.10";
    public static final String GRADLE_CONFIGURATION_ANNOTATION = "annotationProcessor";
    public static final String GRADLE_CONFIGURATION_API = "api";

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11";

    public static void setMinecraftJsonPath(Path basePath) {
        if (basePath != null) MINECRAFT_JSON_PATH = basePath.resolve(MINECRAFT_JSON_PATH);
    }

    public static void setPaths(Path basePath, String minecraftVersion) {
        MINECRAFT_VERSION = minecraftVersion;
        if (basePath != null) MINECRAFT_PATH = basePath.resolve(MINECRAFT_PATH);
        VERSION_JSON_PATH = MINECRAFT_PATH.resolve(MINECRAFT_VERSION + ".json");
        SERVER_JAR_PATH = MINECRAFT_PATH.resolve("server-" + MINECRAFT_VERSION + ".jar");
        SERVER_MAPPED_JAR_PATH = MINECRAFT_PATH.resolve("server-" + MINECRAFT_VERSION + "-remapped.jar");
        SERVER_MAPPINGS_PATH = MINECRAFT_PATH.resolve("server-" + MINECRAFT_VERSION + "-mappings.txt");
        SERVER_MAPPINGS_CONVERTED_PATH =  MINECRAFT_PATH.resolve("server-" + MINECRAFT_VERSION + "-mappings-converted.txt");
    }
}
