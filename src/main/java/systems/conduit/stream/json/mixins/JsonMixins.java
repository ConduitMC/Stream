package systems.conduit.stream.json.mixins;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class JsonMixins {

    @Getter private List<JsonMixin> mixins = new ArrayList<>();

}
