package systems.conduit.stream.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.TaskContainer;
import systems.conduit.stream.Constants;
import systems.conduit.stream.SharedLaunch;

import java.nio.file.Path;
import java.util.Collections;

public class StreamGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        // Project Defaults
        setDefaults(project);
        // Our Conduit info
        ConduitExtension extension = project.getExtensions().create("conduit", ConduitExtension.class, project);
        // Run on evaluated
        project.getGradle().projectsEvaluated(action -> {
            JavaPluginConvention java = (JavaPluginConvention) project.getConvention().getPlugins().get("java");
            if (extension.getJava().isPresent()) {
                java.setSourceCompatibility(extension.getJava().get());
                java.setTargetCompatibility(extension.getJava().get());
            }
        });
        // Do the conduit memes
        Path cacheFolder = project.getGradle().getGradleUserHomeDir().toPath().resolve(Constants.CONDUIT_CACHE);
        if (!cacheFolder.toFile().exists() && !cacheFolder.toFile().mkdirs()) {
            System.out.println("Failed to make cache directory");
            System.exit(0);
        }
        SharedLaunch.start(cacheFolder, project);
    }

    public void setDefaults(Project project) {
        // Set encoding to utf-8
        System.setProperty("file.encoding", "utf-8");
        // Default plugins
        project.apply(Collections.singletonMap("plugin", "java"));
        project.apply(Collections.singletonMap("plugin", "java-library"));
        project.apply(Collections.singletonMap("plugin", "maven-publish"));
        // Default repositories
        project.getRepositories().mavenLocal();
        project.getRepositories().jcenter();
        project.getRepositories().maven(repo -> repo.setUrl(Constants.CONDUIT_REPO));
        // Default dependencies
        project.getDependencies().add(Constants.GRADLE_CONFIGURATION_ANNOTATION, Constants.LOMBOK_DEPENDENCY);
        project.getDependencies().add(Constants.GRADLE_CONFIGURATION_API, Constants.LOMBOK_DEPENDENCY);
        // Default install maven task
        if (!taskExists(project.getTasks(), "install")) project.getTasks().register("install").configure(task -> task.dependsOn("publishToMavenLocal"));
    }

    private boolean taskExists(TaskContainer tasks, String task) {
        try {
            tasks.getByName(task);
        } catch (UnknownTaskException e) {
            return false;
        }
        return true;
    }
}
