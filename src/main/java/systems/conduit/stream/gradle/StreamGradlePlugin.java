package systems.conduit.stream.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.UnknownTaskException;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.TaskContainer;
import systems.conduit.stream.Callback;
import systems.conduit.stream.Constants;
import systems.conduit.stream.Logger;
import systems.conduit.stream.SharedLaunch;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Collections;

public class StreamGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        // Register Conduit info
        ConduitExtension extension = project.getExtensions().create("conduit", ConduitExtension.class, project);
        // Project Defaults
        setDefaults(project);
        // Run on evaluated
        project.getGradle().projectsEvaluated(action -> {
            // Create default cache folder
            Path cacheFolder = project.getGradle().getGradleUserHomeDir().toPath().resolve(Constants.CONDUIT_CACHE);
            if (!cacheFolder.toFile().exists() && !cacheFolder.toFile().mkdirs()) {
                System.out.println("Failed to make cache directory");
                System.exit(0);
            }
            // Get java convention and register java version if wanted
            JavaPluginConvention java = (JavaPluginConvention) project.getConvention().getPlugins().get("java");
            if (extension.getJava().isPresent()) {
                java.setSourceCompatibility(extension.getJava().get());
                java.setTargetCompatibility(extension.getJava().get());
            }
            // Conduit dependency if wanted
            if (extension.getVersion().isPresent()) {
                Logger.info("Loading Conduit: " + extension.getVersion().get());
                project.getDependencies().add(Constants.GRADLE_CONFIGURATION_API, Constants.CONDUIT_DEPENDENCY + extension.getVersion().get());
                Logger.info("Loaded Conduit");
            }
            // Register our dependencies to gradle
            final Callback<File> registerDependency = jar -> {
                try {
                    SharedLaunch.specialSourcePaths.add(jar.toURI().toURL());
                    Dependency dependency = project.getDependencies().create(project.files(jar.toURI().toURL()));
                    project.getDependencies().add(Constants.GRADLE_CONFIGURATION_API, dependency);
                } catch (MalformedURLException e) {
                    Logger.fatal("Error adding " + jar.getPath());
                    e.printStackTrace();
                    System.exit(0);
                }
            };
            // Download required libraries
            SharedLaunch.downloadRequiredLibraries(cacheFolder, registerDependency);
            // Download default libraries
            SharedLaunch.downloadDefaultLibraries(cacheFolder, registerDependency);
            // Make sure minecraft is present. It always should be if done right.
            if (extension.getMinecraft().isPresent()) {
                // Download/load minecraft libraries and download and remap minecraft if need to
                SharedLaunch.setupMinecraft(cacheFolder, extension.getMinecraft().get(), registerDependency);
                // Load minecraft
                registerDependency.callback(Constants.SERVER_MAPPED_JAR_PATH.toFile());
            }
        });
    }

    private void setDefaults(Project project) {
        // Set encoding to utf-8
        System.setProperty("file.encoding", "utf-8");
        // Set our user agent
        System.setProperty("http.agent", Constants.USER_AGENT);
        // Default plugins
        project.apply(Collections.singletonMap("plugin", "java"));
        project.apply(Collections.singletonMap("plugin", "java-library"));
        project.apply(Collections.singletonMap("plugin", "maven-publish"));
        // Default repositories
        project.getRepositories().mavenLocal();
        project.getRepositories().jcenter();
        project.getRepositories().maven(repo -> repo.setUrl(Constants.CONDUIT_REPO));
        project.getRepositories().maven(repo -> repo.setUrl(Constants.MINECRAFT_REPO));
        // Default dependencies
        project.getDependencies().add(Constants.GRADLE_CONFIGURATION_ANNOTATION, Constants.LOMBOK_DEPENDENCY);
        project.getDependencies().add(Constants.GRADLE_CONFIGURATION_API, Constants.LOMBOK_DEPENDENCY);
        // Default install maven task
        if (!doesInstallExist(project.getTasks())) project.getTasks().register("install").configure(task -> task.dependsOn("publishToMavenLocal"));
    }

    private boolean doesInstallExist(TaskContainer tasks) {
        try {
            tasks.getByName("install");
        } catch (UnknownTaskException e) {
            return false;
        }
        return true;
    }
}
