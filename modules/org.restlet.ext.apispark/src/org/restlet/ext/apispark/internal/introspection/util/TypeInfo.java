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
            if (componentClazz == null || !Types.isPrimitiveType(componentClazz)) {
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
