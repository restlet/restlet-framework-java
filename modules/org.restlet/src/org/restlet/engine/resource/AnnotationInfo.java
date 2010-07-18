/**
 * Copyright 2005-2010 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.resource;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.service.MetadataService;

// [excludes gwt]
/**
 * Descriptor for method annotations.
 * 
 * @author Jerome Louvel
 */
public class AnnotationInfo {

    /**
     * Returns the actual type for a given generic type name.
     * 
     * @param currentClass
     *            The current class to walk up.
     * @param genericTypeName
     *            The generic type name to resolve.
     * @return The actual type.
     */
    private static Class<?> getJavaActualType(Class<?> currentClass,
            String genericTypeName) {
        Class<?> result = null;

        // Lookup in the super class
        result = getJavaActualType(currentClass.getGenericSuperclass(),
                genericTypeName);

        if (result == null) {
            // Lookup in the implemented interfaces
            Type[] interfaceTypes = currentClass.getGenericInterfaces();

            for (int i = 0; (result == null) && (i < interfaceTypes.length); i++) {
                result = getJavaActualType(interfaceTypes[i], genericTypeName);
            }
        }

        return result;
    }

    /**
     * Returns the actual type for a given generic type name.
     * 
     * @param currentType
     *            The current type to start with.
     * @param genericTypeName
     *            The generic type name to resolve.
     * @return The actual type.
     */
    private static Class<?> getJavaActualType(Type currentType,
            String genericTypeName) {
        Class<?> result = null;

        if (currentType != null) {
            if (currentType instanceof Class<?>) {
                // Look in the generic super class or the implemented interfaces
                result = getJavaActualType((Class<?>) currentType,
                        genericTypeName);
            } else if (currentType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) currentType;
                Class<?> rawType = (Class<?>) parameterizedType.getRawType();
                Type[] actualTypeArguments = parameterizedType
                        .getActualTypeArguments();
                TypeVariable<?>[] typeParameters = rawType.getTypeParameters();

                for (int i = 0; (result == null)
                        && (i < actualTypeArguments.length); i++) {
                    if (genericTypeName.equals(typeParameters[i].getName())) {
                        result = getTypeClass(actualTypeArguments[i]);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the underlying class for a type or null.
     * 
     * @param type
     *            The generic type.
     * @return The underlying class
     */
    private static Class<?> getTypeClass(Type type) {
        Class<?> result = null;

        if (type instanceof Class<?>) {
            result = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            result = getTypeClass(((ParameterizedType) type).getRawType());
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type)
                    .getGenericComponentType();
            Class<?> componentClass = getTypeClass(componentType);

            if (componentClass != null) {
                result = Array.newInstance(componentClass, 0).getClass();
            }
        }

        return result;
    }

    /** The annotated Java method. */
    private final java.lang.reflect.Method javaMethod;

    /** The class that hosts the annotated Java method. */
    private final Class<?> resourceClass;

    /** The matching Restlet method. */
    private final Method restletMethod;

    /** The annotation value. */
    private final String value;

    /**
     * Constructor.
     * 
     * @param resourceClass
     *            The class or interface that hosts the annotated Java method.
     * @param restletMethod
     *            The matching Restlet method.
     * @param javaMethod
     *            The annotated Java method.
     * @param value
     *            The annotation value.
     */
    public AnnotationInfo(Class<?> resourceClass, Method restletMethod,
            java.lang.reflect.Method javaMethod, String value) {
        super();
        this.resourceClass = resourceClass;
        this.restletMethod = restletMethod;
        this.javaMethod = javaMethod;
        this.value = value;
    }

    /**
     * Indicates if the current variant is equal to the given variant.
     * 
     * @param other
     *            The other variant.
     * @return True if the current variant includes the other.
     */
    @Override
    public boolean equals(Object other) {
        boolean result = (other instanceof AnnotationInfo);

        if (result && (other != this)) {
            AnnotationInfo otherAnnotation = (AnnotationInfo) other;

            // Compare the method
            if (result) {
                result = ((getJavaMethod() == null)
                        && (otherAnnotation.getJavaMethod() == null) || (getJavaMethod() != null)
                        && getJavaMethod().equals(
                                otherAnnotation.getJavaMethod()));
            }

            // Compare the resource interface
            if (result) {
                result = ((getResourceClass() == null)
                        && (otherAnnotation.getResourceClass() == null) || (getResourceClass() != null)
                        && getResourceClass().equals(
                                otherAnnotation.getResourceClass()));
            }

            // Compare the Restlet method
            if (result) {
                result = ((getRestletMethod() == null)
                        && (otherAnnotation.getRestletMethod() == null) || (getRestletMethod() != null)
                        && getRestletMethod().equals(
                                otherAnnotation.getRestletMethod()));
            }

            // Compare the value
            if (result) {
                result = ((getValue() == null)
                        && (otherAnnotation.getValue() == null) || (getValue() != null)
                        && getValue().equals(otherAnnotation.getValue()));
            }
        }

        return result;
    }

    /**
     * Returns the input part of the annotation value.
     * 
     * @return The input part of the annotation value.
     */
    public String getInputValue() {
        String result = getValue();

        if (result != null) {
            int colonIndex = result.indexOf(':');

            if (colonIndex != -1) {
                result = result.substring(0, colonIndex);
            }
        }

        return result;
    }

    /**
     * Returns the actual type for a given generic type.
     * 
     * @param initialType
     *            The initial type, which may be generic.
     * @param genericType
     *            The generic type information if any.
     * @return The actual type.
     */
    private Class<?> getJavaActualType(Class<?> initialType, Type genericType) {
        Class<?> result = initialType;

        try {
            if (genericType instanceof TypeVariable<?>) {
                TypeVariable<?> genericTypeVariable = (TypeVariable<?>) genericType;
                String genericTypeName = genericTypeVariable.getName();
                result = getJavaActualType(getResourceClass(), genericTypeName);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return result;
    }

    /**
     * Returns the generic type for the given input parameter.
     * 
     * @param index
     *            The input parameter index.
     * 
     * @return The generic type.
     */
    public Class<?> getJavaInputType(int index) {
        return getJavaActualType(getJavaMethod().getParameterTypes()[index],
                getJavaMethod().getGenericParameterTypes()[index]);
    }

    /**
     * Returns the input types of the Java method.
     * 
     * @return The input types of the Java method.
     */
    public Class<?>[] getJavaInputTypes() {
        int count = getJavaMethod().getParameterTypes().length;
        Class<?>[] classes = new Class[count];

        for (int i = 0; i < count; i++) {
            classes[i] = getJavaInputType(i);
        }

        return classes;
    }

    /**
     * Returns the annotated Java method.
     * 
     * @return The annotated Java method.
     */
    public java.lang.reflect.Method getJavaMethod() {
        return javaMethod;
    }

    /**
     * Returns the output type of the Java method.
     * 
     * @return The output type of the Java method.
     */
    public Class<?> getJavaOutputType() {
        return getJavaActualType(getJavaMethod().getReturnType(),
                getJavaMethod().getGenericReturnType());
    }

    /**
     * Returns the output part of the annotation value.
     * 
     * @return The output part of the annotation value.
     */
    public String getOutputValue() {
        String result = getValue();

        if (result != null) {
            int colonIndex = result.indexOf(':');

            if (colonIndex != -1) {
                result = result.substring(colonIndex + 1);
            }
        }

        return result;
    }

    // [ifndef gwt] method
    /**
     * Returns a list of request variants based on the annotation value.
     * 
     * @param metadataService
     *            The metadata service to use.
     * @return A list of request variants.
     */
    @SuppressWarnings("unchecked")
    public List<Variant> getRequestVariants(MetadataService metadataService,
            org.restlet.service.ConverterService converterService) {
        List<Variant> result = null;
        Class<?>[] classes = getJavaInputTypes();

        if (classes != null && classes.length >= 1) {
            String value = getInputValue();

            if (value != null) {
                String[] extensions = value.split("\\|");

                if (extensions != null) {
                    for (String extension : extensions) {
                        List<MediaType> mediaTypes = metadataService
                                .getAllMediaTypes(extension);

                        if (mediaTypes != null) {
                            if (result == null) {
                                result = new ArrayList<Variant>();
                            }

                            for (MediaType mediaType : mediaTypes) {
                                result.add(new Variant(mediaType));
                            }
                        }
                    }
                }
            }

            if (result == null) {
                Class<?> inputClass = classes[0];
                result = (List<Variant>) converterService.getVariants(
                        inputClass, null);
            }
        }

        return result;
    }

    /**
     * Returns the resource interface value.
     * 
     * @return The resource interface value.
     */
    public Class<?> getResourceClass() {
        return resourceClass;
    }

    // [ifndef gwt] method
    /**
     * Returns a list of response variants based on the annotation value.
     * 
     * @param metadataService
     *            The metadata service to use.
     * @param converterService
     *            The converter service to use.
     * @return A list of response variants.
     */
    @SuppressWarnings("unchecked")
    public List<Variant> getResponseVariants(MetadataService metadataService,
            org.restlet.service.ConverterService converterService) {
        List<Variant> result = null;

        if ((getJavaOutputType() != null)
                && (getJavaOutputType() != void.class)
                && (getJavaOutputType() != Void.class)) {
            String value = getOutputValue();

            if (value != null) {
                String[] extensions = value.split("\\|");
                for (String extension : extensions) {
                    List<MediaType> mediaTypes = metadataService
                            .getAllMediaTypes(extension);

                    if (mediaTypes != null) {
                        for (MediaType mediaType : mediaTypes) {
                            if ((result == null)
                                    || (!result.contains(mediaType))) {
                                if (result == null) {
                                    result = new ArrayList<Variant>();
                                }

                                result.add(new Variant(mediaType));
                            }
                        }
                    }
                }
            }

            if (result == null) {
                result = (List<Variant>) converterService.getVariants(
                        getJavaOutputType(), null);
            }
        }

        return result;
    }

    /**
     * Returns the matching Restlet method.
     * 
     * @return The matching Restlet method.
     */
    public Method getRestletMethod() {
        return restletMethod;
    }

    /**
     * Returns the annotation value.
     * 
     * @return The annotation value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Indicates if the annotated method described is compatible with the given
     * parameters.
     * 
     * @param restletMethod
     *            The Restlet method to match.
     * @param requestEntity
     *            Optional request entity.
     * @param metadataService
     *            The metadata service to use.
     * @param converterService
     *            The converter service to use.
     * @return True if the annotated method is compatible.
     */
    public boolean isCompatible(Method restletMethod,
            Representation requestEntity, MetadataService metadataService,
            org.restlet.service.ConverterService converterService) {
        return getRestletMethod().equals(restletMethod)
                && isCompatibleRequestEntity(requestEntity, metadataService,
                        converterService);
    }

    /**
     * Indicates if the given request entity is compatible with the annotated
     * method described.
     * 
     * @param requestEntity
     *            Optional request entity.
     * @param metadataService
     *            The metadata service to use.
     * @param converterService
     *            The converter service to use.
     * @return True if the given request entity is compatible with the annotated
     *         method described.
     */
    public boolean isCompatibleRequestEntity(Representation requestEntity,
            MetadataService metadataService,
            org.restlet.service.ConverterService converterService) {
        boolean result = true;

        if ((requestEntity != null) && requestEntity.isAvailable()) {
            List<Variant> requestVariants = getRequestVariants(metadataService,
                    converterService);

            if ((requestVariants != null) && !requestVariants.isEmpty()) {
                // Check that the compatibility
                result = false;

                for (int i = 0; (!result) && (i < requestVariants.size()); i++) {
                    result = (requestVariants.get(i)
                            .isCompatible(requestEntity));
                }
            } else {
                result = false;
            }
        }

        return result;
    }

    @Override
    public String toString() {
        return "AnnotationInfo [javaMethod=" + javaMethod
                + ", resourceInterface=" + resourceClass + ", restletMethod="
                + restletMethod + ", value=" + value + "]";
    }

}