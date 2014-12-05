/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
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

package org.restlet.ext.apispark.internal.introspection.application;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
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

        if (typeInfo.isPrimitive() || typeInfo.isJdkClass()) {
            // primitives and jdk classes are not collected
            return null;
        }

        if (typeInfo.isFile()) {
            representation.setName("file");
        } else {
            if (!typeInfo.isPrimitive()) {
                // Example: "java.util.Contact" or "String"
                representation.setDescription("Java type: " + typeInfo.getRepresentationClazz().getName());
            }

            // Sections
            if (collectInfo.isUseSectionNamingPackageStrategy()) {
                String packageName = typeInfo.getClazz().getPackage().getName();
                representation.getSections().add(packageName);
                if (collectInfo.getSection(packageName) == null) {
                    collectInfo.addSection(new Section(packageName));
                }
            }
            // Example: "Contact"
            JsonTypeName jsonType = typeInfo.getClazz().getAnnotation(JsonTypeName.class);
            String typeName = jsonType == null ? typeInfo.getRepresentationClazz()
                    .getSimpleName() : jsonType.value();
            representation.setName(typeName);
        }
        representation.setRaw(typeInfo.isRaw());

        // at this point, identifier is known - we check if it exists in cache
        boolean notInCache = collectInfo.getRepresentation(representation
                .getName()) == null;

        if (notInCache) {

            // add representation in cache before complete it to avoid infinite
            // loop
            collectInfo.addRepresentation(representation);

            if (!typeInfo.isRaw()) {
                // add properties definition

                BeanInfo beanInfo = BeanInfoUtils.getBeanInfo(typeInfo
                        .getRepresentationClazz());

                JsonIgnoreProperties jsonIgnorePropertiesAnnotation = AnnotatedClass.construct(typeInfo.getRepresentationClazz(), new JacksonAnnotationIntrospector(), null).getAnnotation(JsonIgnoreProperties.class);
                List<String> jsonIgnoreProperties = jsonIgnorePropertiesAnnotation == null ? null : Arrays.asList(jsonIgnorePropertiesAnnotation.value());

                for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {

                    if (jsonIgnoreProperties != null && jsonIgnoreProperties.contains(pd.getName())) {
                        //ignore this field
                        continue;
                    }
                    JsonIgnore jsonIgnore = pd.getReadMethod().getAnnotation(JsonIgnore.class);
                    if (jsonIgnore != null && jsonIgnore.value()) {
                        //ignore this field
                        continue;
                    }

                    TypeInfo propertyTypeInfo;
                    try {
                        propertyTypeInfo = Types.getTypeInfo(pd.getReadMethod()
                                .getReturnType(), pd.getReadMethod()
                                .getGenericReturnType());
                    } catch (UnsupportedTypeException e) {
                        LOGGER.warning("Could not add property " + pd.getName()
                                + " of representation "
                                + typeInfo.getRepresentationClazz().getName() + ". "
                                + e.getMessage());
                        continue;
                    }

                    JsonProperty jsonProperty = pd.getReadMethod().getAnnotation(JsonProperty.class);
                    String propertyName = jsonProperty == null ? pd.getName() : jsonProperty.value();

                    // Types
                    Property property = new Property();
                    property.setName(propertyName);
                    property.setDescription("");
                    property.setType(propertyTypeInfo.getRepresentationName());
                    property.setMinOccurs(0);
                    property.setMaxOccurs(propertyTypeInfo.isList() ? -1 : 1);

                    addRepresentation(collectInfo, propertyTypeInfo,
                            introspectionHelper);

                    for (IntrospectionHelper helper : introspectionHelper) {
                        helper.processProperty(property, pd.getReadMethod());
                    }

                    representation.getProperties().add(property);
                }
            }

            for (IntrospectionHelper helper : introspectionHelper) {
                helper.processRepresentation(representation,
                        typeInfo.getRepresentationClazz());
            }

        }
        return representation.getName();
    }
}
