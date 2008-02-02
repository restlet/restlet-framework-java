package org.restlet.ext.jaxrs.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.logging.Level;

import javax.ws.rs.ext.Provider;

import org.restlet.ext.jaxrs.JaxRsRouter;

/**
 * If the automatic loading of the {@link Provider}s doesn't work, you can use
 * this method to load the providers, that's class names are listed in the files
 * META-INF/services/javax.ws.rs.ext.MessageBodyWriter and
 * META-INF/services/javax.ws.rs.ext.MessageBodyWriter reachable with the given
 * {@link ClassLoader}.
 * 
 * @author Stephan Koops
 * 
 */
public class ProviderLoader {

    private static final String META_INF_SERVICES = "META-INF/services/";

    /**
     * @param classLoader
     * @param throwOnException
     * @param jaxRsRouter the {@link JaxRsRouter} to add the Providers to.
     * @return true, if everything got right, false if a problem occurs, and
     *         throwOnException was false.
     * @throws IllegalArgumentException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static boolean loadProviders(ClassLoader classLoader,
            boolean throwOnException, JaxRsRouter jaxRsRouter)
            throws IllegalArgumentException, IOException,
            ClassNotFoundException {
        boolean result = true;
        try {
            Class<?> providerClass = javax.ws.rs.ext.MessageBodyWriter.class;
            // TODO z.Zt. nur MessageBodyWriter
            Collection<Class<?>> messageBodyReaderClasses = loadClasses(
                    classLoader, providerClass, throwOnException, jaxRsRouter);
            for (Class<?> messageBodyReaderClass : messageBodyReaderClasses) {
                try {
                    jaxRsRouter
                            .addProvider(messageBodyReaderClass, false, true);
                    // TODO z.Zt. nur MessageBodyWriter
                } catch (IllegalArgumentException e) {
                    if (throwOnException)
                        throw e;
                    jaxRsRouter.getLogger().log(
                            Level.WARNING,
                            "Could not add the MessageBodyReader "
                                    + messageBodyReaderClass.getName(), e);
                    result = false;

                }
            }
        } catch (IOException e) {
            if (throwOnException)
                throw e;
            jaxRsRouter.getLogger().log(Level.WARNING,
                    "Could not add the MessageBodyReaders", e);
            result = false;
        } catch (ClassNotFoundException e) {
            if (throwOnException)
                throw e;
            jaxRsRouter.getLogger().log(Level.WARNING,
                    "Could not load a MessageBodyReader: " + e.getMessage(), e);
            result = false;
        }
        return result;
    }

    /**
     * @param classLoader
     *                the ClassLoader to load the classes with
     * @param interfacce
     *                The interface to load implementations for.
     * @param throwOnClassNotFound
     *                if true, a catched {@link ClassNotFoundException} is
     *                thrown, otherwise logged.
     * @throws IOException
     *                 if the resource under the given URL can not be read.
     * @throws ClassNotFoundException
     *                 if throwClassNotFound is true and a class could not be
     *                 loaded.
     */
    private static Collection<Class<?>> loadClasses(ClassLoader classLoader,
            Class<?> interfacce, boolean throwOnClassNotFound,
            JaxRsRouter jaxRsRouter) throws IOException, ClassNotFoundException {
        String filename = META_INF_SERVICES + interfacce.getName();
        Collection<Class<?>> classes = new ArrayList<Class<?>>();
        try {
            Enumeration<URL> resEnum = classLoader.getResources(filename);
            while (resEnum.hasMoreElements()) {
                URL url = resEnum.nextElement();
                loadClasses(url, classes, classLoader, throwOnClassNotFound,
                        jaxRsRouter);
            }
        } catch (IOException e) {
            loadClasses(classLoader.getResource(filename), classes,
                    classLoader, throwOnClassNotFound, jaxRsRouter);
        }
        return classes;
    }

    /**
     * Loads the classes with the names in the resource under the given URL and
     * them to the given Collection.
     * 
     * @param url
     *                the URL which describes the resource with the class names
     * @param classColl
     *                the collection to add the classes
     * @param classLoader
     *                the ClassLoader to load the classes with
     * @param throwOnClassNotFound
     *                if true, a catched {@link ClassNotFoundException} is
     *                thrown, otherwise logged.
     * @throws IOException
     *                 if the resource under the given URL can not be read.
     * @throws ClassNotFoundException
     *                 if throwClassNotFound is true and a class could not be
     *                 loaded.
     */
    private static void loadClasses(URL url, Collection<Class<?>> classColl,
            ClassLoader classLoader, boolean throwOnClassNotFound,
            JaxRsRouter jaxRsRouter) throws IOException, ClassNotFoundException {
        InputStream inputStream = url.openStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream));
        String className;
        while ((className = reader.readLine()) != null) {
            className = className.trim();
            if (className.startsWith("#") || className.startsWith("//"))
                continue; // comment
            try {
                Class<?> clazz = classLoader.loadClass(className);
                classColl.add(clazz);
            } catch (ClassNotFoundException e) {
                if (throwOnClassNotFound)
                    throw e;
                jaxRsRouter.getLogger().log(Level.WARNING,
                        "Class with name " + className + " not found", e);
            }
        }
    }
}