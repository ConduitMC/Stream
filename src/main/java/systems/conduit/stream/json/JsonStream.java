package systems.conduit.stream.json;

import lombok.Getter;

public class JsonStream {

    @Getter private JsonMinecraft minecraft = new JsonMinecraft();
    @Getter private JsonConduit conduit = new JsonConduit();

}
