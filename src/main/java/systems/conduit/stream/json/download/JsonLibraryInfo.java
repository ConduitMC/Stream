package systems.conduit.stream.json.download;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class JsonLibraryInfo {

    private final String type;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String url;
}
