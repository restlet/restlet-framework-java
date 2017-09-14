/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.internal.introspection;

import java.lang.reflect.Method;
import java.util.List;

import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;

/**
 * Describes the behavior of helpers used to enrich documentation of Web API
 * during introspection. Implementations should have an empty constructor.
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
     *
     * @return List of representation classes used. If not null or empty,
     *         representations are added in api definition.
     */
    List<Class<?>> processOperation(Resource resource, Operation operation,
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
