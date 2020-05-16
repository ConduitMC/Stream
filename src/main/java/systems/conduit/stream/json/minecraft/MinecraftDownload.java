package systems.conduit.stream.json.minecraft;

public class MinecraftDownload {

    private final MinecraftVersionInfo server;
    private final MinecraftVersionInfo server_mappings;

    public MinecraftDownload(MinecraftVersionInfo server, MinecraftVersionInfo server_mappings) {
        this.server = server;
        this.server_mappings = server_mappings;
    }

    public MinecraftVersionInfo getServer() {
        return server;
    }

    public MinecraftVersionInfo getServerMappings() {
        return server_mappings;
    }
}
