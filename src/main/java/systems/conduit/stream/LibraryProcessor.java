package systems.conduit.stream;

import systems.conduit.stream.json.download.JsonLibraryInfo;
import systems.conduit.stream.launcher.Agent;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LibraryProcessor {

    private static ArrayList<String> loadedArtifacts = new ArrayList<>();

    public static void resetArtifacts() {
        loadedArtifacts = new ArrayList<>();
    }

    public static void downloadLibrary(String type, boolean firstLaunch, Path basePath, Object projectObject, List<JsonLibraryInfo> libraries) {
        Logger.info(firstLaunch || basePath != null, "Loading " + type);
        List<String> loadedLibraries = new ArrayList<>();
        for (JsonLibraryInfo library : libraries) {
            if (loadedArtifacts.contains(library.getGroupId() + ":" + library.getArtifactId())) continue;
            File libraryPath;
            if (basePath != null) {
                libraryPath = new File(basePath.toFile()+ File.separator + Constants.LIBRARIES_PATH.toFile() + File.separator + getPath(library));
            } else {
                libraryPath = new File(Constants.LIBRARIES_PATH.toFile() + File.separator + getPath(library));
            }
            try {
                Files.createDirectories(libraryPath.toPath());
                File jar = new File(libraryPath, getFileName(library));
                if (!jar.exists() && library.getType() != null) {
                    Logger.info(firstLaunch || basePath != null, "Downloading " + type + ": " + library.getArtifactId());
                    if (library.getType().trim().equalsIgnoreCase("maven")) {
                        SharedLaunch.downloadFile(getUrl(library), jar);
                    } else if (!library.getType().trim().equalsIgnoreCase("minecraft")) {
                        SharedLaunch.downloadFile(new URL(library.getUrl()), jar);
                    }
                }
                loadedLibraries.add(library.getArtifactId());
                loadedArtifacts.add(library.getGroupId() + ":" + library.getArtifactId());
                if (basePath == null) Agent.addClassPath(jar);
                if (projectObject != null) {
                    SharedLaunch.specialSourcePaths.add(jar.toURI().toURL());
                    org.gradle.api.Project project = ((org.gradle.api.Project) projectObject);
                    org.gradle.api.artifacts.Dependency dependency = project.getDependencies().create(((org.gradle.api.Project) projectObject).files(jar.toURI().toURL()));
                    project.getDependencies().add(Constants.GRADLE_CONFIGURATION_API, dependency);
                }
            } catch (Exception e) {
                Logger.fatal(firstLaunch || basePath != null, "Error loading " + type + ": " + library.getArtifactId());
                e.printStackTrace();
                System.exit(0);
            }

        }
        if (!loadedLibraries.isEmpty()) Logger.info(firstLaunch || basePath != null, "Loaded " + type + ": " + loadedLibraries);
    }

    private static URL getUrl(JsonLibraryInfo library) throws MalformedURLException {
        String repo = Constants.DEFAULT_REPO;
        if (library.getUrl() != null && !library.getUrl().trim().isEmpty()) repo = library.getUrl().trim();
        return new URL((repo.endsWith("/") ? repo : repo + "/") + getPath(library) + getFileName(library));
    }

    private static String getFileName(JsonLibraryInfo library) {
        return library.getArtifactId() + "-" + library.getVersion() + ".jar";
    }

    private static String getPath(JsonLibraryInfo library) {
        return library.getGroupId().replaceAll("\\.", "/") + "/" + library.getArtifactId() + "/" + library.getVersion() + "/";
    }
}
