package systems.conduit.stream.json.minecraft;

import java.util.List;

public class MinecraftVersion {

    private final MinecraftDownload downloads;
    private final List<MinecraftLibrary> libraries;

    public MinecraftVersion(MinecraftDownload downloads, List<MinecraftLibrary> libraries) {
        this.downloads = downloads;
        this.libraries = libraries;
    }

    public MinecraftDownload getDownloads() {
        return downloads;
    }

    public List<MinecraftLibrary> getLibraries() {
        return libraries;
    }
}
