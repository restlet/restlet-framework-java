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
 * Superclass for Exceptions, if a type of a &#64;*Param annotated parameter,
 * field or bean setter is not valid.
 * 
 * @author Stephan Koops
 * @deprecated Will be removed in next minor release.
 */
@Deprecated
public abstract class IllegalParamTypeException extends JaxRsException {

    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    public IllegalParamTypeException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param throwable
     */
    public IllegalParamTypeException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * @param throwable
     */
    public IllegalParamTypeException(Throwable throwable) {
        super(throwable);
    }
}
