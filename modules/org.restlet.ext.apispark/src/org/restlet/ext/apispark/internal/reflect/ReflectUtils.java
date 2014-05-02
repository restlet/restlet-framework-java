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

package org.restlet.ext.apispark.internal.reflect;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Handles Java reflection operations.
 * 
 * @author Thierry Boileau
 */
public class ReflectUtils {

    @SuppressWarnings("rawtypes")
    public static Field[] getAllDeclaredFields(Class type) {
        List<Field> fields = new ArrayList<Field>();
        Class currentType = type;

        while (currentType != null) {
            Field[] currentFields = currentType.getDeclaredFields();
            Collections.addAll(fields, currentFields);
            currentType = currentType.getSuperclass();
            if (currentType != null && currentType.equals(Object.class)) {
                currentType = null;
            }
        }

        return fields.toArray(new Field[fields.size()]);
    }

    /**
     * TODO: need Javadocs
     * 
     * @param type
     * @return
     */
    public static boolean isListType(Class<?> type) {
        return Collection.class.isAssignableFrom(type);
    }

    /**
     * TODO: need Javadocs
     * 
     * @param clazz
     * @return
     */
    public static boolean isJdkClass(Class<?> clazz) {
        return (clazz.getPackage().getName().startsWith("java.") || clazz
                .getPackage().getName().startsWith("javax."));
    }

}
