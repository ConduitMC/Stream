package systems.conduit.stream.gradle;

import java.net.URL;
import java.net.URLClassLoader;

public class SpecialSourceClassLoader extends URLClassLoader {

    public SpecialSourceClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
}
