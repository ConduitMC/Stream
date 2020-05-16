package systems.conduit.stream.json;

public class JsonConduit {

    private final String version;
    private final boolean download;

    public JsonConduit(String version) {
        this(version, false);
    }

    public JsonConduit(String version, boolean download) {
        this.version = version;
        this.download = download;
    }

    public boolean shouldDownload() {
        return download;
    }

    public String getVersion() {
        return version;
    }
}
