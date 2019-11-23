package systems.conduit.stream.launcher.services;

import cpw.mods.modlauncher.api.ILaunchHandlerService;
import cpw.mods.modlauncher.api.ITransformingClassLoader;
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder;
import org.spongepowered.asm.mixin.Mixins;
import systems.conduit.stream.Constants;
import systems.conduit.stream.launcher.LauncherStart;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

public class ServerLaunchHandlerService implements ILaunchHandlerService {

    @Override
    public String name() {
        return "minecraft-server";
    }

    @Override
    public void configureTransformationClassLoader(final ITransformingClassLoaderBuilder builder) {
        // Add transformation path for Minecraft jar
        builder.addTransformationPath(Constants.SERVER_MAPPED_JAR_PATH);
        // Add mixins to transform and configure
        LauncherStart.MIXINS.forEach(entry -> {
            builder.addTransformationPath(entry.getValue());
            Mixins.addConfiguration(entry.getKey());
        });
    }

    @Override
    public Callable<Void> launchService(String[] args, ITransformingClassLoader launchClassLoader) {
        return () -> {
            final Class<?> mcClass = Class.forName("net.minecraft.server.MinecraftServer", true, launchClassLoader.getInstance());
            final Method mcClassMethod = mcClass.getMethod("main", String[].class);
            mcClassMethod.invoke(null, (Object) args);
            return null;
        };
    }
}
