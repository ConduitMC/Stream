package systems.conduit.stream.launcher.services;

import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoader;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;
import org.spongepowered.asm.mixin.Mixins;
import systems.conduit.stream.Constants;
import systems.conduit.stream.Logger;
import systems.conduit.stream.launcher.LauncherStart;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.concurrent.Callable;

public class ClientLaunchHandlerService implements ILaunchHandlerService {

    @Override
    public void configureTransformationClassLoader(final ITransformingClassLoaderBuilder builder) {
        // Add transformation paths
        LauncherStart.PATHS.forEach(builder::addTransformationPath);
        // Load Minecraft and Conduit jars if in debug
        if (Constants.DEBUG) {
            Path minecraft = getLoadedJar(Constants.MAIN_CLIENT_FILE);
            if (minecraft != null) {
                Logger.info("Transforming Minecraft remapped");
                builder.addTransformationPath(minecraft);
                Logger.info("Transformed Minecraft remapped");
            }
            Path conduit = getLoadedJar(Constants.MAIN_CONDUIT_FILE);
            if (conduit != null) {
                Logger.info("Transforming Conduit");
                builder.addTransformationPath(conduit);
                Logger.info("Transformed Conduit");
            }
        }
    }

    @Override
    public Callable<Void> launchService(String[] args, ITransformingClassLoader launchClassLoader) {
        // Add mixins to configure
        LauncherStart.MIXINS.forEach(Mixins::addConfiguration);
        return () -> {
            final Class<?> mcClass = Class.forName(Constants.MAIN_CLIENT_FILE, true, launchClassLoader.getInstance());
            final Method mcClassMethod = mcClass.getMethod("main", String[].class);
            mcClassMethod.invoke(null, (Object) args);
            return null;
        };
    }

    private Path getLoadedJar(String className) {
        // Returns the jars from the classpath if loaded by intellij
        try {
            return new File(Class.forName(className).getProtectionDomain().getCodeSource().getLocation().toURI()).toPath();
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public String name() {
        return "minecraft-client";
    }
}
