package systems.conduit.stream.json.minecraft;

public class MinecraftRule {

    private final String action;
    private final MinecraftOS os;

    public MinecraftRule(String action, MinecraftOS os) {
        this.action = action;
        this.os = os;
    }

    public String getAction() {
        return action;
    }

    public MinecraftOS getOS() {
        return os;
    }

    protected boolean isMac() {
        return action != null && action.equals("allow") && os != null && os.getName().equals("osx");
    }
}
