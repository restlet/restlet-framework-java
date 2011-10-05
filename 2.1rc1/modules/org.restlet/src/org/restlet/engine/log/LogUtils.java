/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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

package org.restlet.engine.log;

/**
 * Logging related utility methods.
 * 
 * @author Jerome Louvel
 */
public class LogUtils {

    /**
     * Prevent instantiation of the class.
     */
    private LogUtils() {
    }

    /**
     * Return the best class name. If the class is anonymous, then it returns
     * the super class name.
     * 
     * @param clazz
     *            The class to name.
     * @return The class name.
     */
    public static String getBestClassName(Class<?> clazz) {
        String result = clazz.getSimpleName();

        if ((result == null) || (result.equals(""))) {
            result = getBestClassName(clazz.getSuperclass());
        }

        return result;
    }

    /**
     * Returns a non-null logger name. It is composed by the canonical class
     * name of the owner object suffixed by the owner's hash code.
     * 
     * @param baseName
     *            The base logger name to prepend, without a trailing dot.
     * @param owner
     *            The context owner.
     * @return The logger name.
     */
    public static String getLoggerName(String baseName, Object owner) {
        String result = baseName;

        if ((owner != null) && (owner.getClass().getSimpleName() != null)) {
            result += "." + getBestClassName(owner.getClass());
        }

        return result;
    }

}
