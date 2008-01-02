/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet.ext.jaxrs.ext;

import java.util.Date;
import java.util.List;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;

import org.restlet.ext.jaxrs.todo.NotYetImplementedException;

/**
 * 
 * @author Stephan Koops
 *
 */
public class JaxRsRequest implements Request {
    
    private org.restlet.data.Request restletRequest;
    
    /**
     * Creates a wrapper for a Restlet request.
     * @param restletRequest
     */
    public JaxRsRequest(org.restlet.data.Request restletRequest) {
        this.restletRequest = restletRequest;
    }

    public Response evaluatePreconditions(EntityTag tag, Variant variant) {
        restletRequest.toString(); // than Eclipse want say it isn't used
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    public Response evaluatePreconditions(Date lastModified, Variant variant) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    public Response evaluatePreconditions(Date lastModified, EntityTag tag,
            Variant variant) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    public Variant selectVariant(List<Variant> variants)
            throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }
}