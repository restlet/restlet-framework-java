package org.restlet.ext.apispark.internal.introspection;

import java.lang.reflect.Method;

import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;

/**
 * Describes the behaviour of helpers used to enrich documentation of Web API
 * during introspection. Implementations should have an empty constructor.
 * 
 */
public interface IntrospectionHelper {

    /**
     * Completes the given {@link Definition} by introspecting the given class.
     * 
     * @param definition
     *            The definition to complete.
     * @param applicationClass
     *            The class of application to instrospect.
     */
    void processDefinition(Definition definition, Class<?> applicationClass);

    /**
     * Completes the given {@link Resource} and {@link Operation} by
     * introspecting the given class of resource and method.
     * 
     * @param resource
     *            The {@link Resource} to complete.
     * @param operation
     *            The {@link Operation} to complete.
     * @param resourceClass
     *            The class of resource to instrospect.
     * @param javaMethod
     *            The Java method to instrospect.
     */
    void processOperation(Resource resource, Operation operation,
            Class<?> resourceClass, Method javaMethod);

    /**
     * Completes the given {@link Property} by introspecting the given getter
     * method.
     * 
     * @param property
     *            The {@link Property} to complete.
     * @param readMethod
     *            The property getter to instrospect.
     */
    void processProperty(Property property, Method readMethod);

    /**
     * Completes the given {@link Representation} by introspecting the given
     * class of representation.
     * 
     * @param representation
     *            The {@link Representation} to complete.
     * @param representationClass
     *            The class of representation to instrospect.
     */
    void processRepresentation(Representation representation,
            Class<?> representationClass);

    /**
     * Completes the given {@link Resource} by introspecting the given class of
     * resource.
     * 
     * @param resource
     *            The {@link Resource} to complete.
     * @param resourceClass
     *            The class of resource to instrospect.
     */
    void processResource(Resource resource, Class<?> resourceClass);
}
