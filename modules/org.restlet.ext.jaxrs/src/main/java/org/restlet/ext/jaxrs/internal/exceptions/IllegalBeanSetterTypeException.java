/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jaxrs.internal.exceptions;

/**
 * This kind of exception is thrown, when a type of a bean setter must not be
 * annotated with the given JAX-RS annotation
 * 
 * @author Stephan Koops
 * @deprecated Will be removed in next minor release.
 */
@Deprecated
public class IllegalBeanSetterTypeException extends IllegalTypeException {

    private static final long serialVersionUID = 6423619202690501704L;

    /**
     * @param ite
     */
    public IllegalBeanSetterTypeException(IllegalTypeException ite) {
        super(ite.getMessage());
        setStackTrace(ite.getStackTrace());
    }
}
