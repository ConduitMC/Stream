package systems.conduit.stream.json.download;

import java.util.List;

public class JsonLibraries {

    private final List<JsonLibraryInfo> libs;

    public JsonLibraries(List<JsonLibraryInfo> libs) {
        this.libs = libs;
    }

    public List<JsonLibraryInfo> getLibs() {
        return libs;
    }
}
