/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */
package org.restlet.ext.jaxrs.internal.exceptions;

import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;

/**
 * This kind of exception is thrown, if a query parameter could not be
 * converted.
 * 
 * @author Stephan Koops
 * @see QueryParam
 */
public class ConvertFormParamException extends WebApplicationException {

    private static final long serialVersionUID = 131640120766355816L;

    /**
     * @param cpe
     */
    public ConvertFormParamException(ConvertParameterException cpe) {
        super(cpe.getCause(), Status.BAD_REQUEST);
        setStackTrace(cpe.getStackTrace());
    }
}
