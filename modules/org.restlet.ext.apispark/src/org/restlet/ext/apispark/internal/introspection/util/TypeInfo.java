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

package org.restlet.ext.apispark.internal.introspection.util;

import org.restlet.ext.apispark.internal.reflect.ReflectUtils;
import org.restlet.representation.Representation;

import java.io.File;
import java.lang.reflect.Type;

/**
 * @author Manuel Boillod
 */
public class TypeInfo {

    private final Class<?> clazz;
    private final Type type;
    private final String identifier;
    private final Class<?> representationClazz;

    private final boolean isList;
    private final TypeInfo componentTypeInfo;

    private final boolean isPrimitive;
    private final boolean isJdkClass;
    private final boolean isFile;
    private final boolean isRaw;

    /**
     * Use {@link Types#getTypeInfo(Class, java.lang.reflect.Type)} to have a new instance of TypeInfo
     */
   protected TypeInfo(Class<?> clazz, Type type) {
        this.clazz = clazz;
        this.type = type;


        Class<?> componentClazz = ReflectUtils.getComponentClass(type);
        //representation class is the component class (for list, array, class that extends list, ...) or the class itself
        representationClazz = (componentClazz != null) ? componentClazz : clazz;
        identifier = Types.convertPrimitiveType(representationClazz);

        isList = ReflectUtils.isListType(clazz);

        if (isList) {
            //Don't know how to instrospect the list if the componentClazz is null or a JDK class which is not a primitive
            if (componentClazz == null || (!Types.isPrimitiveType(componentClazz) && ReflectUtils.isJdkClass(componentClazz))) {
              throw new UnsupportedTypeException("Type " + Types.toString(clazz, type) +
                      " is a list/array and its component type is unknown or not supported");
            }
            componentTypeInfo = Types.getTypeInfo(componentClazz,
                    null);
        } else {
            componentTypeInfo = null;
        }

        isPrimitive = Types.isPrimitiveType(representationClazz);
        isJdkClass = ReflectUtils.isJdkClass(representationClazz);
        isFile = Representation.class.isAssignableFrom(representationClazz) ||
                File.class.isAssignableFrom(representationClazz);
        isRaw = isFile || isJdkClass;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Type getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public boolean isList() {
        return isList;
    }

    public TypeInfo getComponentTypeInfo() {
        return componentTypeInfo;
    }

    public Class<?> getRepresentationClazz() {
        return representationClazz;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public boolean isJdkClass() {
        return isJdkClass;
    }

    public boolean isFile() {
        return isFile;
    }

    public boolean isRaw() {
        return isRaw;
    }
}
