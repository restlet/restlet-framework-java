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

package org.restlet.ext.apispark.internal.introspection.application;

import java.beans.BeanInfo;
import java.beans.IndexedPropertyDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.restlet.engine.Engine;
import org.restlet.engine.util.BeanInfoUtils;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.Introspector;
import org.restlet.ext.apispark.internal.conversion.ConversionUtils;
import org.restlet.ext.apispark.internal.introspection.IntrospectionHelper;
import org.restlet.ext.apispark.internal.introspection.util.TypeInfo;
import org.restlet.ext.apispark.internal.introspection.util.Types;
import org.restlet.ext.apispark.internal.introspection.util.UnsupportedTypeException;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Section;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

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
     */
    public static void addRepresentation(CollectInfo collectInfo,
            TypeInfo typeInfo,
            List<? extends IntrospectionHelper> introspectionHelper) {
        // Introspect the java class
        Representation representation = new Representation();
        representation.setDescription("");

        if (typeInfo.isList()) {
            // Collect generic type
            addRepresentation(collectInfo, typeInfo.getComponentTypeInfo(),
                    introspectionHelper);
            return;
        }

        if (typeInfo.isPrimitive() || typeInfo.isFile()) {
            // primitives and files are not collected
            return;
        }

        // Example: "java.util.Contact" or "String"
        representation.setDescription("Java type: " + typeInfo.getRepresentationClazz().getName());

        // Sections
        if (collectInfo.isUseSectionNamingPackageStrategy()) {
            String packageName = typeInfo.getClazz().getPackage().getName();
            representation.getSections().add(packageName);
            if (collectInfo.getSection(packageName) == null) {
                String formattedSectionName = ConversionUtils.formatSectionNameFromPackageName(packageName);
                collectInfo.addSection(new Section(formattedSectionName));
            }
        }
        // Example: "Contact"
        JsonRootName jsonType = typeInfo.getClazz().getAnnotation(JsonRootName.class);
        String typeName = jsonType == null ? typeInfo.getRepresentationClazz()
                .getSimpleName() : jsonType.value();
        representation.setName(typeName);
        representation.setRaw(false);

        // at this point, identifier is known - we check if it exists in cache
        boolean notInCache = collectInfo.getRepresentation(representation
                .getName()) == null;

        if (notInCache) {

            // add representation in cache before complete it to avoid infinite
            // loop
            collectInfo.addRepresentation(representation);

            if (typeInfo.isPojo()) {
                // add properties definition

                BeanInfo beanInfo = BeanInfoUtils.getBeanInfo(typeInfo
                        .getRepresentationClazz());

                JsonIgnoreProperties jsonIgnorePropertiesAnnotation = AnnotatedClass.construct(typeInfo.getRepresentationClazz(), new JacksonAnnotationIntrospector(), null).getAnnotation(JsonIgnoreProperties.class);
                List<String> jsonIgnoreProperties = jsonIgnorePropertiesAnnotation == null ? null : Arrays.asList(jsonIgnorePropertiesAnnotation.value());

                for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {

                    if (pd instanceof IndexedPropertyDescriptor) {
                        continue;
                    }

                    if (jsonIgnoreProperties != null && jsonIgnoreProperties.contains(pd.getName())) {
                        //ignore this field
                        continue;
                    }

                    Method readMethod = pd.getReadMethod();

                    if (readMethod == null) {
                        LOGGER.warning("Could not add property " + pd.getName() + " of representation "
                                + typeInfo.getRepresentationClazz().getName() + " because its getter was not found.");
                        continue;
                    }

                    JsonIgnore jsonIgnore = readMethod.getAnnotation(JsonIgnore.class);
                    if (jsonIgnore != null && jsonIgnore.value()) {
                        //ignore this field
                        continue;
                    }

                    TypeInfo propertyTypeInfo;
                    try {
                        propertyTypeInfo = Types.getTypeInfo(readMethod
                                .getReturnType(), readMethod
                                .getGenericReturnType());
                    } catch (UnsupportedTypeException e) {
                        LOGGER.warning("Could not add property " + pd.getName()
                                + " of representation "
                                + typeInfo.getRepresentationClazz().getName() + ". "
                                + e.getMessage());
                        continue;
                    }

                    JsonProperty jsonProperty = readMethod.getAnnotation(JsonProperty.class);
                    String propertyName = jsonProperty != null && !StringUtils.isNullOrEmpty(jsonProperty.value()) ?
                            jsonProperty.value() : pd.getName();

                    JsonPropertyDescription jsonPropertyDescription = readMethod.getAnnotation(JsonPropertyDescription.class);

                    // Types
                    Property property = new Property();
                    property.setName(propertyName);
                    property.setDescription(jsonPropertyDescription != null ? jsonPropertyDescription.value() : "");
                    property.setType(propertyTypeInfo.getRepresentationName());
                    property.setRequired(jsonProperty != null && jsonProperty.required());
                    property.setList(propertyTypeInfo.isList());

                    addRepresentation(collectInfo, propertyTypeInfo,
                            introspectionHelper);

                    for (IntrospectionHelper helper : introspectionHelper) {
                        helper.processProperty(property, readMethod);
                    }

                    representation.getProperties().add(property);
                }
            }

            for (IntrospectionHelper helper : introspectionHelper) {
                helper.processRepresentation(representation,
                        typeInfo.getRepresentationClazz());
            }

        }
    }
}
