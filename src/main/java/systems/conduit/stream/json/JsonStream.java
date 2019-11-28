package systems.conduit.stream.json;

import lombok.Getter;

@Getter
public class JsonStream {

    private JsonMinecraft minecraft = new JsonMinecraft();
    private JsonConduit conduit = new JsonConduit();
}
