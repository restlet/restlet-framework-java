/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.ext.jaxrs.internal.exceptions;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

/**
 * This kind of exception is thrown, if a matrix parameter could not be
 * converted.
 * 
 * @author Stephan Koops
 * @see MatrixParam
 */
public class ConvertMatrixParamException extends WebApplicationException {

    private static final long serialVersionUID = 6704339151884788476L;

    /**
     * @param cpe
     */
    public ConvertMatrixParamException(ConvertParameterException cpe) {
        super(cpe.getCause(), Status.NOT_FOUND);
        setStackTrace(cpe.getStackTrace());
    }
}
