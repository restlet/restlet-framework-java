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

package org.restlet.engine.header;

import java.util.Set;

import org.restlet.data.Method;

/**
 * Method header writer.
 * 
 * @author Jerome Louvel
 */
public class MethodWriter extends HeaderWriter<Method> {

    /**
     * Writes a set of methods with a comma separator.
     * 
     * @param methods
     *            The set of methods.
     * @return The formatted set of methods.
     */
    public static String write(Set<Method> methods) {
        return new MethodWriter().append(methods).toString();
    }

    @Override
    public MethodWriter append(Method method) {
        return (MethodWriter) appendToken(method.getName());
    }

}
