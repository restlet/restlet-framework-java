package org.restlet.ext.apispark.internal.introspection;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Section;
import org.restlet.ext.apispark.internal.model.Types;
import org.restlet.ext.apispark.internal.reflect.ReflectUtils;

/**
 * Created by manu on 11/10/2014.
 */
public class RepresentationCollector {

    /**
<<<<<<< HEAD
     * Returns the description of the given class as a
     * {@link Representation}.
     *
     * @param clazz The class to document.
     * @param type  The class to document.
     * @param introspectorPlugins
=======
     * Returns the description of the given class as a {@link Representation}.
     * 
     * @param clazz
     *            The class to document.
     * @param type²
     *            The class to document.
>>>>>>> added sections by package
     * @return The name of representation type if added, null otherwise
     *         {@link Representation}.
     */
    public static String addRepresentation(CollectInfo collectInfo,
                                           Class<?> clazz, Type type,
                                           IntrospectorPlugin[] introspectorPlugins) {
        // Introspect the java class
        Representation representation = new Representation();
        representation.setDescription("");

        Class<?> c = ReflectUtils.getSimpleClass(type);
        Class<?> representationType = (c == null) ? clazz : c;
        boolean generic = c != null
                && !c.getCanonicalName().equals(clazz.getCanonicalName());
        boolean isList = ReflectUtils.isListType(clazz);
        // todo check generics use cases
        if (generic || isList) {
            // Collect generic type
            addRepresentation(collectInfo, representationType,
                    representationType.getGenericSuperclass(), introspectorPlugins);
            return null;
        }

        if (Types.isPrimitiveType(representationType)
                || ReflectUtils.isJdkClass(representationType)) {
            // primitives and jdk classes are not collected
            return null;
        }

        boolean isFile = org.restlet.representation.Representation.class
                .isAssignableFrom(clazz);

        if (isFile) {
            representation.setIdentifier("file");
            representation.setName("file");
        } else {
            // type is an Entity
            // Example: "java.util.Contact" or "String"
            representation.setIdentifier(Types
                    .convertPrimitiveType(representationType));

            // Sections
            String packageName = clazz.getPackage().getName();
            representation.getSections().add(packageName);
            if (collectInfo.getSection(packageName) == null) {
                collectInfo.addSection(new Section(packageName));
            }
            // Example: "Contact"
            representation.setName(representationType.getSimpleName());
        }
        boolean isRaw = isFile || ReflectUtils.isJdkClass(representationType);
        representation.setRaw(isRaw);

        // at this point, identifier is known - we check if it exists in cache
        boolean notInCache = collectInfo.getRepresentation(representation
                .getIdentifier()) == null;

        if (notInCache) {
            if (!isRaw) {
                // add properties definition
                for (Field field : ReflectUtils
                        .getAllDeclaredFields(representationType)) {
                    if ("serialVersionUID".equals(field.getName())) {
                        continue;
                    }
                    Property property = new Property();
                    property.setName(field.getName());
                    property.setDescription("");
                    Class<?> fieldType = ReflectUtils.getSimpleClass(field);
                    addRepresentation(collectInfo, fieldType,
                            field.getGenericType(), introspectorPlugins);
                    property.setType(Types.convertPrimitiveType(fieldType));
                    property.setMinOccurs(0);
                    boolean isCollection = ReflectUtils.isListType(field
                            .getType());
                    property.setMaxOccurs(isCollection ? -1 : 1);

                    for (IntrospectorPlugin introspectorPlugin : introspectorPlugins) {
                        introspectorPlugin.processProperty(property, field);
                    }

                    representation.getProperties().add(property);
                }

                // Parent representation are not extracted
                // //add parent representation if any
                // Class<?> parentType = representationType.getSuperclass();
                // if (parentType != null) {
                // String parentTypeIdentifier = addRepresentation(collectInfo,
                // parentType, parentType.getGenericSuperclass());
                // if (parentTypeIdentifier != null) {
                // representation.setExtendedType(parentTypeIdentifier);
                // }
                // }

            }
            
            for (IntrospectorPlugin introspectorPlugin : introspectorPlugins) {
                introspectorPlugin.processRepresentation(representation, representationType);
            }
            // add in cache
            collectInfo.addRepresentation(representation);
        }
        return representation.getIdentifier();
    }
}
