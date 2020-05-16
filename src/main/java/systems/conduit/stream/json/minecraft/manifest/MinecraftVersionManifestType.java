package systems.conduit.stream.json.minecraft.manifest;


public class MinecraftVersionManifestType {

    private final String id;
    private final String url;

    public MinecraftVersionManifestType(String id, String url) {
        this.id = id;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }
}
