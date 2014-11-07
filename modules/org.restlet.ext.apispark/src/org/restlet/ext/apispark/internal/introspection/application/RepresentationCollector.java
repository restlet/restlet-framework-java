package org.restlet.ext.apispark.internal.introspection.application;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Type;
import java.util.List;

import org.restlet.engine.util.BeanInfoUtils;
import org.restlet.ext.apispark.internal.introspection.IntrospectorPlugin;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Section;
import org.restlet.ext.apispark.internal.model.Types;
import org.restlet.ext.apispark.internal.reflect.ReflectUtils;

/**
 * @author Manuel Boillod
 */
public class RepresentationCollector {

    /**
     * Returns the description of the given class as a {@link Representation}.
     * 
     * @param clazz
     *            The class to document.
     * @param type
     *            The class to document.
     * @param introspectorPlugins
     *            The introspector plugins
     *
     * @return The name of representation type if added, null otherwise
     *         {@link Representation}.
     */
    public static String addRepresentation(CollectInfo collectInfo,
                                           Class<?> clazz, Type type,
                                           List<? extends IntrospectorPlugin> introspectorPlugins) {
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

            // add representation in cache before complete it to avoid infinite loop
            collectInfo.addRepresentation(representation);

            if (!isRaw) {
                // add properties definition

                BeanInfo beanInfo = BeanInfoUtils.getBeanInfo(representationType);
                for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                    Class<?> propertyClazz = pd.getReadMethod().getReturnType();
                    Type propertyType = pd.getReadMethod().getGenericReturnType();

                    Property property = new Property();
                    property.setName(pd.getName());
                    property.setDescription("");
                    property.setType(Types.convertPrimitiveType(ReflectUtils.getSimpleClass(propertyType)));
                    property.setMinOccurs(0);
                    boolean isCollection = ReflectUtils.isListType(propertyClazz);
                    property.setMaxOccurs(isCollection ? -1 : 1);

                    addRepresentation(collectInfo, propertyClazz,
                            propertyType, introspectorPlugins);

                    for (IntrospectorPlugin introspectorPlugin : introspectorPlugins) {
                        introspectorPlugin.processProperty(property, pd.getReadMethod());
                    }

                    representation.getProperties().add(property);
                }
            }
            
            for (IntrospectorPlugin introspectorPlugin : introspectorPlugins) {
                introspectorPlugin.processRepresentation(representation, representationType);
            }

        }
        return representation.getIdentifier();
    }
}
