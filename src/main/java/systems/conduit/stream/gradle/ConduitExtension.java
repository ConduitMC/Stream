package systems.conduit.stream.gradle;

import org.gradle.api.Project;

public class ConduitExtension {

    public String version;
    public String minecraft;

    public ConduitExtension(Project project) {
        version = project.getObjects().property(String.class).getOrNull();
        minecraft = project.getObjects().property(String.class).getOrNull();
    }

    public String getVersion() {
        return version;
    }

    public String getMinecraft() {
        return minecraft;
    }
}
