package systems.conduit.stream.json;

import lombok.Getter;

public class JsonConduit {

    @Getter private String version = "";
    private boolean download = false;

    public boolean shouldDownload() {
        return download;
    }
}
