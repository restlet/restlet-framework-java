package org.restlet.ext.apispark.internal.introspection.application;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.logging.Logger;

import org.restlet.engine.Engine;
import org.restlet.engine.util.BeanInfoUtils;
import org.restlet.ext.apispark.Introspector;
import org.restlet.ext.apispark.internal.introspection.IntrospectionHelper;
import org.restlet.ext.apispark.internal.introspection.util.TypeInfo;
import org.restlet.ext.apispark.internal.introspection.util.Types;
import org.restlet.ext.apispark.internal.introspection.util.UnsupportedTypeException;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Section;

/**
 * @author Manuel Boillod
 */
public class RepresentationCollector {
    private static Logger LOGGER = Engine.getLogger(Introspector.class);

    /**
     * Returns the description of the given class as a {@link Representation}.
     *
     * @param typeInfo
     *            The typeInfo to document.
     * @param introspectionHelper
     *            The introspector helpers.
     *
     * @return The name of representation type if added, null otherwise
     *         {@link Representation}.
     */
    public static String addRepresentation(CollectInfo collectInfo,
                                           TypeInfo typeInfo,
                                           List<? extends IntrospectionHelper> introspectionHelper) {
        // Introspect the java class
        Representation representation = new Representation();
        representation.setDescription("");

        if (typeInfo.isList()) {
            // Collect generic type
            addRepresentation(collectInfo, typeInfo.getComponentTypeInfo(),
                    introspectionHelper);
            return null;
        }

        if (typeInfo.isPrimitive()
                || typeInfo.isJdkClass()) {
            // primitives and jdk classes are not collected
            return null;
        }

        if (typeInfo.isFile()) {
            representation.setIdentifier("file");
            representation.setName("file");
        } else {
            // type is an Entity
            // Example: "java.util.Contact" or "String"
            representation.setIdentifier(typeInfo.getIdentifier());

            // Sections
            if (collectInfo.isUseSectionNamingPackageStrategy()) {
                String packageName = typeInfo.getClazz().getPackage().getName();
                representation.getSections().add(packageName);
                if (collectInfo.getSection(packageName) == null) {
                    collectInfo.addSection(new Section(packageName));
                }
            }
            // Example: "Contact"
            representation.setName(typeInfo.getRepresentationClazz().getSimpleName());
        }
        representation.setRaw(typeInfo.isRaw());

        // at this point, identifier is known - we check if it exists in cache
        boolean notInCache = collectInfo.getRepresentation(representation
                .getIdentifier()) == null;

        if (notInCache) {

            // add representation in cache before complete it to avoid infinite
            // loop
            collectInfo.addRepresentation(representation);

            if (!typeInfo.isRaw()) {
                // add properties definition

                BeanInfo beanInfo = BeanInfoUtils
                        .getBeanInfo(typeInfo.getRepresentationClazz());
                for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {

                    TypeInfo propertyTypeInfo;
                    try {
                        propertyTypeInfo = Types.getTypeInfo(pd.getReadMethod().getReturnType(),
                                pd.getReadMethod().getGenericReturnType());
                    } catch (UnsupportedTypeException e) {
                        LOGGER.warning("Could not add property " + pd.getName() +
                                " of representation " + typeInfo.getIdentifier() + ". " +
                                e.getMessage());
                        continue;
                    }

//                    Types
                    Property property = new Property();
                    property.setName(pd.getName());
                    property.setDescription("");
                    property.setType(propertyTypeInfo.getIdentifier());
                    property.setMinOccurs(0);
                    property.setMaxOccurs(typeInfo.isList() ? -1 : 1);

                    addRepresentation(collectInfo, propertyTypeInfo,
                            introspectionHelper);

                    for (IntrospectionHelper helper : introspectionHelper) {
                        helper.processProperty(property, pd.getReadMethod());
                    }

                    representation.getProperties().add(property);
                }
            }

            for (IntrospectionHelper helper : introspectionHelper) {
                helper.processRepresentation(representation, typeInfo.getRepresentationClazz());
            }

        }
        return representation.getIdentifier();
    }
}
