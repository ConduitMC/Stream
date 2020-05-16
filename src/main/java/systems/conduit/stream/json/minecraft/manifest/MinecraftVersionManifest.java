package systems.conduit.stream.json.minecraft.manifest;

import java.util.List;

public class MinecraftVersionManifest {

    private final List<MinecraftVersionManifestType> versions;

    public MinecraftVersionManifest(List<MinecraftVersionManifestType> versions) {
        this.versions = versions;
    }

    public List<MinecraftVersionManifestType> getVersions() {
        return versions;
    }
}
