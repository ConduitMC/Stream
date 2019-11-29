package systems.conduit.stream.gradle;

import lombok.Getter;
import lombok.Setter;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;

@Getter
@Setter
public class ConduitExtension {

    private Property<String> version;
    private Property<String> minecraft;
    private Property<String> java;

    public ConduitExtension(Project project) {
        version = project.getObjects().property(String.class);
        minecraft = project.getObjects().property(String.class);
        java = project.getObjects().property(String.class);
    }
}
