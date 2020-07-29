package systems.conduit.stream.gradle;

import org.gradle.api.Project;

public class ConduitExtension {

    public String version;
    public String minecraft;
    public String java;

    public ConduitExtension(Project project) {
        version = project.getObjects().property(String.class).getOrNull();
        minecraft = project.getObjects().property(String.class).getOrNull();
        java = project.getObjects().property(String.class).getOrNull();
    }
}
