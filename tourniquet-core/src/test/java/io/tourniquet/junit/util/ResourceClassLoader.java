package io.tourniquet.junit.util;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 *
 */
public class ResourceClassLoader extends URLClassLoader {

    public ResourceClassLoader() {

        super(((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs());
    }

    public Class<?> loadClassFromResource(final String classname, String resourceName) throws Exception {

        URL resource = new ResourceResolver(true).resolve(resourceName);
        try (InputStream is = resource.openStream()) {
            int size = is.available();
            byte[] classData = new byte[size];
            is.read(classData);
            Class<?> loadedClass = defineClass(classname, classData, 0, size);
            resolveClass(loadedClass);
            return loadedClass;
        }
    }
}
