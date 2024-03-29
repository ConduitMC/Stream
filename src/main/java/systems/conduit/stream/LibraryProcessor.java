package systems.conduit.stream;

import systems.conduit.stream.json.download.JsonLibraryInfo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class LibraryProcessor {

    private static ArrayList<String> loadedArtifacts = new ArrayList<>();

    public static void resetArtifacts() {
        loadedArtifacts = new ArrayList<>();
    }

    public static void downloadLibrary(String type, Path basePath, List<JsonLibraryInfo> libraries, Callback<File> callback) {
        Logger.info("Loading " + type);
        List<String> loadedLibrariesIds = new ArrayList<>();
        List<File> loadedLibrariesFiles = new ArrayList<>();
        for (JsonLibraryInfo library : libraries) {
            if (loadedArtifacts.contains(library.getGroupId() + ":" + library.getArtifactId() + (library.getEnd() != null && !library.getEnd().isEmpty() ? "-" + library.getEnd() : ""))) {
                continue;
            }
            loadedArtifacts.add(library.getGroupId() + ":" + library.getArtifactId() + (library.getEnd() != null && !library.getEnd().isEmpty() ? "-" + library.getEnd() : ""));
            File libraryPath;
            if (basePath != null) {
                libraryPath = new File(basePath.toFile() + File.separator + Constants.LIBRARIES_PATH.toFile() + File.separator + getPath(library));
            } else {
                libraryPath = new File(Constants.LIBRARIES_PATH.toFile() + File.separator + getPath(library));
            }
            // Try to create directory.
            try {
                Files.createDirectories(libraryPath.toPath());
            } catch (Exception e) {
                Logger.exception("Error creating directories for " + type + ": " + library.getArtifactId() + (library.getEnd() != null && !library.getEnd().isEmpty() ? "-" + library.getEnd() : ""), e);
            }
            try {
                File jar = new File(libraryPath, getFileName(library));
                if (!jar.exists() && library.getType() != null) {
                    Logger.info("Downloading " + type + ": " + library.getArtifactId() + (library.getEnd() != null && !library.getEnd().isEmpty() ? "-" + library.getEnd() : ""));
                    if (library.getType().trim().equalsIgnoreCase("maven")) {
                        SharedLaunch.downloadFile(getUrl(library), jar);
                    } else if (!library.getType().trim().equalsIgnoreCase("minecraft")) {
                        SharedLaunch.downloadFile(new URL(library.getUrl()), jar);
                    }
                }
                loadedLibrariesIds.add(library.getArtifactId() + (library.getEnd() != null && !library.getEnd().isEmpty() ? "-" + library.getEnd() : ""));
                loadedLibrariesFiles.add(jar);
            } catch (Exception e) {
                Logger.exception("Error loading url for " + type + ": " + library.getArtifactId(), e);
            }
        }
        if (!loadedLibrariesIds.isEmpty()) {
            loadedLibrariesFiles.forEach(callback::callback);
            Logger.info("Loaded " + type + ": " + loadedLibrariesIds);
        }
    }

    private static URL getUrl(JsonLibraryInfo library) throws MalformedURLException {
        String repo = Constants.DEFAULT_REPO;
        if (library.getUrl() != null && !library.getUrl().trim().isEmpty()) repo = library.getUrl().trim();
        return new URL((repo.endsWith("/") ? repo : repo + "/") + getPath(library) + getFileName(library));
    }

    private static String getFileName(JsonLibraryInfo library) {
        return library.getArtifactId() + "-" + library.getVersion() + (library.getEnd() != null && !library.getEnd().isEmpty() ? "-" + library.getEnd() : "") + ".jar";
    }

    private static String getPath(JsonLibraryInfo library) {
        return library.getGroupId().replaceAll("\\.", "/") + "/" + library.getArtifactId() + "/" + library.getVersion() + "/";
    }
}
