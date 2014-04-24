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
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet
 */

package org.restlet.ext.apispark.reflect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;

/**
 * Handles Java reflection operations.
 * 
 * @author Thierry Boileau
 */
public class ReflectUtils {

    /** The internal logger. */
    private final static Logger logger = Context.getCurrentLogger();

    @SuppressWarnings("rawtypes")
    public static Field[] getAllDeclaredFields(Class type) {
        List<Field> fields = new ArrayList<Field>();
        Class currentType = type;
        while (currentType != null) {
            Field[] currentFields = currentType.getDeclaredFields();
            Collections.addAll(fields, currentFields);
            currentType = currentType.getSuperclass();
            if (currentType.equals(Object.class)) {
                currentType = null;
            }
        }
        return fields.toArray(new Field[fields.size()]);
    }

    @SuppressWarnings("rawtypes")
    public static Field getDeclaredField(Class type, String fieldName) {
        Field[] fields = getAllDeclaredFields(type);
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static Field getField(Class type, String fieldName) {
        Field[] fields = getAllDeclaredFields(type);
        for (Field field : fields) {
            if (field.getName().equals(fieldName)) {
                return field;
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static Class getFieldClass(Class clazz, String fieldName) {
        try {
            Field field = getDeclaredField(clazz, fieldName);
            if (field != null) {
                return getFieldClass(field);
            }
        } catch (Exception ex) {
            logger.log(Level.INFO, "Error when getting type of field "
                    + fieldName + " for class " + clazz.getCanonicalName(), ex);
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static Class getFieldClass(Field field) {
        return field.getType();
    }

    @SuppressWarnings("rawtypes")
    public static String getFieldType(Class clazz, String fieldName) {
        try {
            Field field = getDeclaredField(clazz, fieldName);
            if (field != null) {
                return getFieldType(field);
            }
        } catch (Exception ex) {
            logger.log(Level.INFO, "Error when getting type of field "
                    + fieldName + " for class " + clazz.getCanonicalName(), ex);
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static String getFieldType(Field field) {
        Class type = field.getType();
        return type.getCanonicalName();
    }

    @SuppressWarnings("rawtypes")
    public static boolean isListType(Object value) {
        if (value != null) {
            Class type = value.getClass();
            return ("java.util.List".equals(type.getName()) || "java.util.ArrayList"
                    .equals(type.getName()));
        }
        return false;
    }
}
