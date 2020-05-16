package systems.conduit.stream.json.download;


public class JsonLibraryInfo {

    private final String type;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String url;

    public JsonLibraryInfo(String type, String groupId, String artifactId, String version) {
        this(type, groupId, artifactId, version, "");
    }

    public JsonLibraryInfo(String type, String groupId, String artifactId, String version, String url) {
        this.type = type;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getUrl() {
        return url;
    }
}
