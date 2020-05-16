package systems.conduit.stream.json;

public class JsonStream {

    private final JsonMinecraft minecraft;
    private final JsonConduit conduit;

    public JsonStream(JsonMinecraft minecraft, JsonConduit conduit) {
        this.minecraft = minecraft;
        this.conduit = conduit;
    }

    public JsonMinecraft getMinecraft() {
        return minecraft;
    }

    public JsonConduit getConduit() {
        return conduit;
    }
}
