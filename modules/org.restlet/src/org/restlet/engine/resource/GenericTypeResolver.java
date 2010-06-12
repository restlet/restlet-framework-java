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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

// [excludes gwt]
/**
 * Resolver of generic types able to check return types, parameter types of Java
 * methods.
 * 
 * @author Valdis Rigdon
 */
@SuppressWarnings("unchecked")
public class GenericTypeResolver {

    /** Cache from Class to TypeVariable Map. */
    private static final Map<Class, Reference<Map<TypeVariable, Type>>> typeVariablesCache = Collections
            .synchronizedMap(new WeakHashMap<Class, Reference<Map<TypeVariable, Type>>>());

    /**
     * Determines the raw type for the given generic parameter type.
     * 
     * @param genericType
     *            The generic type to resolve.
     * @param typeVariableMap
     *            The TypeVariable Map to resolved against.
     * @return The resolved raw type.
     */
    private static Type getRawType(Type genericType,
            Map<TypeVariable, Type> typeVariableMap) {
        Type rawType = genericType;
        if (genericType instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) genericType;
            rawType = typeVariableMap.get(tv);
            if (rawType == null) {
                rawType = getUpperBound(tv);
            }
        }
        if (rawType instanceof ParameterizedType) {
            return ((ParameterizedType) rawType).getRawType();
        } else {
            return rawType;
        }
    }

    /**
     * Returns a mapping of TypeVariable names to concrete classes for the
     * specified class. It explores all super types, enclosing types and
     * interfaces.
     * 
     * @param clazz
     *            The class to explore.
     * @return A mapping of TypeVariable namesto concrete class.
     */
    private static Map<TypeVariable, Type> getTypeVariablesMap(Class clazz) {
        Reference<Map<TypeVariable, Type>> ref = typeVariablesCache.get(clazz);
        Map<TypeVariable, Type> typeVariableMap = (ref != null ? ref.get()
                : null);

        if (typeVariableMap == null) {
            typeVariableMap = new HashMap<TypeVariable, Type>();

            // Explore the implemented interfaces.
            getTypeVariables(clazz.getGenericInterfaces(), typeVariableMap);

            // Explore the super class.
            Type genericType = clazz.getGenericSuperclass();
            Class type = clazz.getSuperclass();
            while (type != null && !Object.class.equals(type)) {
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) genericType;
                    populate(pt, typeVariableMap);
                }
                getTypeVariables(type.getGenericInterfaces(), typeVariableMap);
                genericType = type.getGenericSuperclass();
                type = type.getSuperclass();
            }

            // Explore the enclosing class.
            type = clazz;
            while (type.isMemberClass()) {
                genericType = type.getGenericSuperclass();
                if (genericType instanceof ParameterizedType) {
                    populate((ParameterizedType) genericType, typeVariableMap);
                }
                type = type.getEnclosingClass();
            }

            typeVariablesCache
                    .put(clazz, new WeakReference<Map<TypeVariable, Type>>(
                            typeVariableMap));
        }

        return typeVariableMap;
    }

    /**
     * Returns the TypeVariables from the supplied generic interfaces and
     * populates the given Map.
     * 
     * @param genericInterfaces
     *            The array of generic interfaces to explore.
     * @param typeVariableMap
     *            The map to complete.
     */
    private static void getTypeVariables(Type[] genericInterfaces,
            Map<TypeVariable, Type> typeVariableMap) {
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericInterface;
                populate(pt, typeVariableMap);
                if (pt.getRawType() instanceof Class) {
                    getTypeVariables(((Class) pt.getRawType())
                            .getGenericInterfaces(), typeVariableMap);
                }
            } else if (genericInterface instanceof Class) {
                getTypeVariables(((Class) genericInterface)
                        .getGenericInterfaces(), typeVariableMap);
            }
        }
    }

    /**
     * Returns the upper bound type for a given TypeVariable.
     * 
     * @param typeVariable
     *            The typeVariable.
     * @return The bound type for a given TypeVariable.
     */
    private static Type getUpperBound(TypeVariable typeVariable) {
        Type[] bounds = typeVariable.getBounds();
        if (bounds.length == 0) {
            return Object.class;
        }
        Type bound = bounds[0];
        if (bound instanceof TypeVariable) {
            bound = getUpperBound((TypeVariable) bound);
        }

        return bound;
    }

    /**
     * Completes the given Map with the TypeVariable taken from the given
     * parametrized type.
     * 
     * @param type
     *            The parameterized type to explore.
     * @param typeVariableMap
     *            The map to complete.
     */
    private static void populate(ParameterizedType type,
            Map<TypeVariable, Type> typeVariableMap) {
        if (type.getRawType() instanceof Class) {
            Type[] actualTypeArguments = type.getActualTypeArguments();
            TypeVariable[] typeVariables = ((Class) type.getRawType())
                    .getTypeParameters();
            for (int i = 0; i < actualTypeArguments.length; i++) {
                Type actualTypeArgument = actualTypeArguments[i];
                TypeVariable variable = typeVariables[i];
                if (actualTypeArgument instanceof Class) {
                    typeVariableMap.put(variable, actualTypeArgument);
                } else if (actualTypeArgument instanceof GenericArrayType) {
                    typeVariableMap.put(variable, actualTypeArgument);
                } else if (actualTypeArgument instanceof ParameterizedType) {
                    typeVariableMap.put(variable, actualTypeArgument);
                } else if (actualTypeArgument instanceof TypeVariable) {
                    // Get the bounded type.
                    TypeVariable typeVariableArgument = (TypeVariable) actualTypeArgument;
                    Type resolvedType = typeVariableMap
                            .get(typeVariableArgument);
                    if (resolvedType == null) {
                        resolvedType = getUpperBound(typeVariableArgument);
                    }
                    typeVariableMap.put(variable, resolvedType);
                }
            }
        }
    }

    /**
     * Determines the target type for the given generic parameter type of the
     * given method.
     * 
     * @param method
     *            The method to introspect.
     * @param parameterIndex
     *            The index of the parameter to resolve.
     * @param clazz
     *            The class to resolve type variables against.
     * @return The corresponding generic parameter type.
     */
    public static Class<?> resolveParameterType(Method method,
            int parameterIndex, Class clazz) {
        if (parameterIndex >= 0
                && parameterIndex < method.getGenericParameterTypes().length) {
            Type genericType = method.getGenericParameterTypes()[parameterIndex];
            Type rawType = getRawType(genericType, getTypeVariablesMap(clazz));
            return (rawType instanceof Class ? (Class) rawType : method
                    .getParameterTypes()[parameterIndex]);
        }

        return null;
    }

    /**
     * Determines the target type for the generic return type of the given
     * method.
     * 
     * @param method
     *            The method to introspect.
     * @param clazz
     *            The class to resolve type variables against.
     * @return The corresponding generic parameter or return type.
     */
    public static Class<?> resolveReturnType(Method method, Class clazz) {
        Type genericType = method.getGenericReturnType();
        Type rawType = getRawType(genericType, getTypeVariablesMap(clazz));
        return (rawType instanceof Class ? (Class) rawType : method
                .getReturnType());
    }

}
