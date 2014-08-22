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

package org.restlet.engine.resource;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

// [excludes gwt]
/**
 * Descriptor for Restlet annotations.
 * 
 * @author Jerome Louvel
 */
public abstract class AnnotationInfo {

    /**
     * Returns the actual type for a given generic type name.
     * 
     * @param currentClass
     *            The current class to walk up.
     * @param genericTypeName
     *            The generic type name to resolve.
     * @return The actual type.
     */
    protected static Class<?> getJavaActualType(Class<?> currentClass,
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
    protected static Class<?> getJavaActualType(Type currentType,
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
    protected static Class<?> getTypeClass(Type type) {
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

    /** The raw annotation value. */
    protected final String annotationValue;

    /** The class that hosts the annotated Java method. */
    protected final Class<?> javaClass;

    /** The annotated Java method. */
    protected final java.lang.reflect.Method javaMethod;

    /** The upper implementation of the annotated Java method. */
    protected final java.lang.reflect.Method javaMethodImpl;

    /**
     * Constructor.
     * 
     * @param javaClass
     *            The annotated Java class or parent Java class.
     * @param javaMethod
     *            The annotated Java method.
     * @param value
     *            The annotation value.
     */
    public AnnotationInfo(Class<?> javaClass,
            java.lang.reflect.Method javaMethod, String value) {
        super();
        this.javaClass = javaClass;
        this.javaMethod = javaMethod;
        this.annotationValue = value;
        java.lang.reflect.Method m = null;

        try {
            m = javaClass.getMethod(javaMethod.getName(),
                    javaMethod.getParameterTypes());
        } catch (Exception e) {
            m = javaMethod;
        }

        if (m != null) {
            this.javaMethodImpl = m;
        } else {
            this.javaMethodImpl = javaMethod;
        }
    }

    /**
     * Indicates if the current variant is equal to the given object.
     * 
     * @param other
     *            The other object.
     * @return True if the current object is equal to the given object.
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
                result = ((getJavaClass() == null)
                        && (otherAnnotation.getJavaClass() == null) || (getJavaClass() != null)
                        && getJavaClass()
                                .equals(otherAnnotation.getJavaClass()));
            }

            // Compare the annotation value
            if (result) {
                result = ((getAnnotationValue() == null)
                        && (otherAnnotation.getAnnotationValue() == null) || (getAnnotationValue() != null)
                        && getAnnotationValue().equals(
                                otherAnnotation.getAnnotationValue()));
            }
        }

        return result;
    }

    /**
     * Returns the raw annotation value.
     * 
     * @return The raw annotation value.
     */
    public String getAnnotationValue() {
        return annotationValue;
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
    protected Class<?> getJavaActualType(Class<?> initialType, Type genericType) {
        Class<?> result = initialType;

        try {
            if (genericType instanceof TypeVariable<?>) {
                TypeVariable<?> genericTypeVariable = (TypeVariable<?>) genericType;
                String genericTypeName = genericTypeVariable.getName();
                result = getJavaActualType(getJavaClass(), genericTypeName);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return result;
    }

    /**
     * Returns the resource interface value.
     * 
     * @return The resource interface value.
     */
    public Class<?> getJavaClass() {
        return javaClass;
    }

    /**
     * Returns the annotated Java method.
     * 
     * @return The annotated Java method.
     */
    public java.lang.reflect.Method getJavaMethod() {
        return javaMethod;
    }

    @Override
    public String toString() {
        return "AnnotationInfo [javaMethod: " + javaMethod + ", javaClass: "
                + javaClass + ", value: " + annotationValue + "]";
    }

}