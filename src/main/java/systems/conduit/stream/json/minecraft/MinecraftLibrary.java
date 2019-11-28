package systems.conduit.stream.json.minecraft;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class MinecraftLibrary {

    private String name = "";
    private List<MinecraftRule> rules = new ArrayList<>();

    public boolean isMac() {
        return rules.stream().anyMatch(MinecraftRule::isMac);
    }
}
