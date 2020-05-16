package systems.conduit.stream.json.minecraft;

import java.util.List;

public class MinecraftLibrary {

    private final String name;
    private final List<MinecraftRule> rules;

    public MinecraftLibrary(String name, List<MinecraftRule> rules) {
        this.name = name;
        this.rules = rules;
    }

    public String getName() {
        return name;
    }

    public List<MinecraftRule> getRules() {
        return rules;
    }

    public boolean isMac() {
        return rules != null && rules.stream().anyMatch(MinecraftRule::isMac);
    }
}
