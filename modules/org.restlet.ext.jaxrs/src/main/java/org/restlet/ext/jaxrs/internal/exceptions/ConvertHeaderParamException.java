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

import javax.ws.rs.HeaderParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

/**
 * This kind of exception is thrown, if a header parameter could not be
 * converted.
 * 
 * @author Stephan Koops
 * @see HeaderParam
 * @deprecated Will be removed in next minor release.
 */
@Deprecated
public class ConvertHeaderParamException extends WebApplicationException {

    private static final long serialVersionUID = -2880948194279374251L;

    /**
     * @param cpe
     */
    public ConvertHeaderParamException(ConvertParameterException cpe) {
        super(cpe.getCause(), Status.BAD_REQUEST);
        setStackTrace(cpe.getStackTrace());
    }
}
