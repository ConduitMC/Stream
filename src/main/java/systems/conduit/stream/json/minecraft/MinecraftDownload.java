package systems.conduit.stream.json.minecraft;

import lombok.Getter;

public class MinecraftDownload {

    @Getter private MinecraftVersionInfo client =  new MinecraftVersionInfo();
    @Getter private MinecraftVersionInfo server =  new MinecraftVersionInfo();
    private MinecraftVersionInfo client_mappings = new MinecraftVersionInfo();
    private MinecraftVersionInfo server_mappings = new MinecraftVersionInfo();

    public MinecraftVersionInfo getClientMappings() {
        return client_mappings;
    }

    public MinecraftVersionInfo getServerMappings() {
        return server_mappings;
    }
}
