package systems.conduit.stream.json.minecraft;

import lombok.Getter;

@Getter
public class MinecraftRule {

    private String action = "";
    private MinecraftOS os = new MinecraftOS();

    protected boolean isMac() {
        return action != null && action.equals("allow") && os != null && os.getName().equals("osx");
    }
}
