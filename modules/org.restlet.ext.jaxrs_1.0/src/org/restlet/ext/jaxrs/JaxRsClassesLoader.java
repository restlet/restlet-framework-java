package org.restlet.ext.jaxrs;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.logging.Level;

import javax.ws.rs.Path;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.restlet.ext.jaxrs.todo.NotYetImplementedException;

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
        boolean added = false;
        if (MessageBodyReader.class.isAssignableFrom(clazz)) {
            if (!clazz.isAnnotationPresent(Provider.class)) {
                String msg = "The class "
                        + clazz.getName()
                        + " implements the MessageBodyReader, but is not annotated with @Provider. Will although use it as MessageBodyReader.";
                jaxRsRouter.getLogger().log(Level.INFO, msg);
            }
            jaxRsRouter.addMessageBodyReader(clazz);
            added = true;
        }
        if (MessageBodyWriter.class.isAssignableFrom(clazz)) {
            if (!clazz.isAnnotationPresent(Provider.class)) {
                String msg = "The class "
                        + clazz.getName()
                        + " implements the MessageBodyWriter, but is not annotated with @Provider. Will although use it as MessageBodyWriter.";
                jaxRsRouter.getLogger().log(Level.INFO, msg);
            }
            jaxRsRouter.addMessageBodyWriter(clazz);
            added = true;
        }
        if (ContextResolver.class.isAssignableFrom(clazz)) {
            // TODO do anything with the contextResolver
            throw new NotYetImplementedException(
                    "ContextResolver are not supported yet");
        }
        if (!added) {
            // TODO warn
        }
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
        if(classes == null)
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