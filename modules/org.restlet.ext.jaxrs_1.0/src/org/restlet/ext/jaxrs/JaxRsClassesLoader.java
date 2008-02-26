package org.restlet.ext.jaxrs;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import org.restlet.ext.jaxrs.exceptions.InstantiateParameterException;
import org.restlet.ext.jaxrs.exceptions.InstantiateRootRessourceException;
import org.restlet.ext.jaxrs.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.exceptions.RequestHandledException;
import org.restlet.ext.jaxrs.wrappers.RootResourceClass;

/**
 * This class loads the root resource classes and the {@link Provider}s. If the
 * automatic loading of the {@link Provider}s doesn't work, check
 * {@link #loadProvidersFromFile(ClassLoader, boolean, JaxRsRouter)}.
 * 
 * This class has is NOT a java {@link ClassLoader}, also if the name may
 * suggest it a little bit.
 * 
 * @author Stephan Koops
 */
class JaxRsClassesLoader {

    /**
     * Adds the classes as root resource classes to the given router.
     * 
     * @param classes
     *                the classes to add. May be null.
     * @param jaxRsRouter
     *                the {@link JaxRsRouter} to add the class to.
     * @throws IllegalArgumentException
     */
    static void addProvidersToRouter(Collection<Class<?>> classes,
            JaxRsRouter jaxRsRouter) {
        if (classes == null)
            return;
        for (Class<?> clazz : classes)
            addProviderToRouter(clazz, jaxRsRouter);
    }

    /**
     * Adds the class to the given router.
     * 
     * @param clazz
     *                the class to add
     * @param jaxRsRouter
     *                the {@link JaxRsRouter} to add the class to.
     * @throws IllegalArgumentException
     */
    static void addProviderToRouter(Class<?> clazz, JaxRsRouter jaxRsRouter)
            throws IllegalArgumentException {
        int modifiers = clazz.getModifiers();
        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
            return;
            // TODO warn
        }
        Constructor<?> providerConstructor = RootResourceClass
                .findJaxRsConstructor(clazz);
        Object provider;
        try {
            provider = RootResourceClass.createInstance(providerConstructor, false,
                    null, jaxRsRouter);
        } catch (InstantiateParameterException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the MessageBodyWriter, class "
                            + clazz.getName(), e);
        } catch (MissingAnnotationException e) {
            throw new IllegalArgumentException(
                    "Could not instantiate the MessageBodyWriter, class "
                            + clazz.getName(), e);
        } catch (InstantiateRootRessourceException e) {
            throw new IllegalArgumentException(
                    "Could not instantiate the MessageBodyWriter, class "
                            + clazz.getName(), e);
        } catch (RequestHandledException e) {
            throw new IllegalArgumentException(
                    "Could not instantiate the MessageBodyWriter, class "
                            + clazz.getName(), e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(
                    "Could not instantiate the MessageBodyWriter, class "
                            + clazz.getName(), e.getCause());
        }
        jaxRsRouter.addProvider(provider);
    }

    /**
     * Adds the classes as root resource class to the given router.
     * 
     * @param classes
     *                the classes to add. May be null.
     * @param jaxRsRouter
     *                the {@link JaxRsRouter} to add the class to.
     * @throws IllegalArgumentException
     */
    static void addRrcsToRouter(Collection<Class<?>> classes,
            JaxRsRouter jaxRsRouter) {
        if (classes == null)
            return;
        for (Class<?> clazz : classes)
            addRrcToRouter(clazz, jaxRsRouter);
    }

    /**
     * Adds the class to the given router.
     * 
     * @param clazz
     *                the class to add
     * @param jaxRsRouter
     *                the {@link JaxRsRouter} to add the class to.
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("deprecation")
    static void addRrcToRouter(Class<?> clazz, JaxRsRouter jaxRsRouter) {
        int modifiers = clazz.getModifiers();
        if (Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers)) {
            return;
            // TODO warn
        }
        if (clazz.isAnnotationPresent(Path.class)) {
            jaxRsRouter.attach(clazz);
        } else {
            // TODO warn
        }

    }
}