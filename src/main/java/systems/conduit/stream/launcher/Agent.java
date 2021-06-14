package systems.conduit.stream.launcher;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class Agent {

    private static Instrumentation inst = null;

    // The JRE will call method before launching your main()
    public static void agentmain(final String a, final Instrumentation inst) {
        Agent.inst = inst;
    }

    protected static void addClassPath(File f) {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        try {
            if (inst != null) {
                inst.appendToSystemClassLoaderSearch(new JarFile(f));
                return;
            }
            if (cl instanceof URLClassLoader) {
                Method m = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                m.setAccessible(true);
                m.invoke(cl, f.toURI().toURL());
            }
        } catch (Throwable e) {
            System.out.println("Add to classpath error!");
            e.printStackTrace();
            //System.exit(0);
        }
    }

    protected static void crackModules() {
        final Set<Module> systemUnnamed = Set.of(ClassLoader.getSystemClassLoader().getUnnamedModule());
        Agent.inst.redefineModule(
                Manifest.class.getModule(),
                Set.of(),
                Map.of("sun.security.util", systemUnnamed),
                Map.of("java.util.jar", systemUnnamed, "java.lang", systemUnnamed),
                Set.of(),
                Map.of()
        );
    }
}
