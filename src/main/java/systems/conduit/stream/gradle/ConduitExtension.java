package systems.conduit.stream.gradle;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

public class ConduitExtension {

    @Getter @Setter private Property<String> version;
    @Getter @Setter private Property<String> minecraft;
    @Getter @Setter private Property<String> java;

    public ConduitExtension(Project project) {
        version = project.getObjects().property(String.class);
        minecraft = project.getObjects().property(String.class);
        java = project.getObjects().property(String.class);
    }
}
