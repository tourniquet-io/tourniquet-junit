package io.tourniquet.junit.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A test classloader for dynamically loading external jar files, allowing to declare for which packages this
 * classloader takes precedence over the parent classloader.
 */
public class TestClassLoader extends URLClassLoader {

    private final Set<String> blacklist;

    private final Set<String> packages;

    /**
     * Creates a classloader for testing purposes. The classloader loads the classes from the test jars.
     *
     * @param testJars
     *         jar files to be loaded by this classloader. For all packages and classes contained in these jars this
     *         classloader takes precedence over the parent classloader.
     * @param packages
     *         additional packages for which this classloader should take precedence over the parent classloader
     */
    public TestClassLoader(Collection<URL> testJars, Collection<String> packages, String... excludePackages) {

        super(getURLs(testJars), ClassLoader.getSystemClassLoader().getParent());
        this.packages = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.packages.addAll(packages);
        this.packages.addAll(new JarScanner().addJar(testJars).ignore(excludePackages).scanPackages());
        this.blacklist = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.blacklist.addAll(Arrays.asList(excludePackages));
    }

    /**
     * Creates a classloader for testing purposes. The classloader loads the classes from the test jars.
     *
     * @param testJars
     *         jar files to be loaded by this classloader. For all packages and classes contained in these jars this
     *         classloader takes precedence over the parent classloader.
     * @param packages
     *         optional additional packages for which this classloader should take precedence over the parent
     *         classloader
     */
    public TestClassLoader(Collection<URL> testJars, String... packages) {

        this(testJars, Arrays.asList(packages));
    }

    /**
     * Creates a classloader for testing purposes. The classloader loads the classes from the test jars.
     *
     * @param testJars
     *         jar files to be loaded by this classloader. For all packages and classes contained in these jars this
     *         classloader takes precedence over the parent classloader.
     */
    public TestClassLoader(URL... testJars) {
        this(Arrays.asList(testJars));
    }

    /*
     * Method is overridden to load all classes from the defined packages using this classloader. All other classes
     * are loaded either with this classloader or the parent classloader.
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (!blacklist.stream().anyMatch(name::startsWith) && packages.stream().anyMatch(name::startsWith)) {
            //to prevent loading a class twice, we have to check, if the class is already loaded.
            Class<?> cls = super.findLoadedClass(name);
            if (cls != null) {
                return cls;
            }
            return super.findClass(name);
        }
        /*
         * the super implementation of this method first uses the parent classloader to load a class. In case the
          * requested class is already loaded, it is loaded using the parent.
         */
        return super.loadClass(name);
    }

    /**
     * Creates the classpath from the collection of test URLs and the system classpath. The testjars listed on the
     * beginning of the classpath and therefore take precedence in the class loading order.
     *
     * @param testJars
     *         the testjars to be prepended to the current classpath
     *
     * @return array of all the test jars and the system classpath
     */
    private static URL[] getURLs(Collection<URL> testJars) {

        final List<URL> result = new ArrayList<>();
        result.addAll(testJars);
        result.addAll(Arrays.asList(((URLClassLoader) getSystemClassLoader()).getURLs()));
        return result.toArray(new URL[result.size()]);
    }
}
