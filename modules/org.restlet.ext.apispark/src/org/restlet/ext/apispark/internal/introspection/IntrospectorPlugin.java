package org.restlet.ext.apispark.internal.introspection;

import org.restlet.Application;
import org.restlet.engine.resource.MethodAnnotationInfo;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.resource.Directory;
import org.restlet.resource.ServerResource;

import java.lang.reflect.Method;

/**
 *
 * Introspector Plugin implementation should have an empty constructor
 * in order to be create new instance from its Class.
 *
 */
public interface IntrospectorPlugin {


    void processDefinition(Definition definition, Application application);

    void processResource(Resource resource, ServerResource serverResource);

    void processResource(Resource resource, Directory directory);

    void processOperation(Operation operation, MethodAnnotationInfo methodAnnotationInfo);

    void processRepresentation(Representation representation, Class<?> representationType);

    void processProperty(Property property, Method readMethod);
}
