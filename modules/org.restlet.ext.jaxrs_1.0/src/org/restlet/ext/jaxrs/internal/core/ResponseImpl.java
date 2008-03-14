/*
 * Copyright 2005-2008 Noelios Consulting.
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
package org.restlet.ext.jaxrs.internal.core;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * @author Stephan Koops
 * 
 */
public class ResponseImpl extends Response {

    private Object entity;

    private MultivaluedMap<String, Object> metadata;

    private int status = 200;

    /**
     * Creates a new Response Instance with status code 200
     */
    public ResponseImpl() {
    }

    /**
     * Creates a new Response Instance.
     * 
     * @param status
     *                the status code for the new response.
     */
    public ResponseImpl(int status) {
        this.status = status;
    }

    @Override
    public ResponseImpl clone() {
        ResponseImpl newResp = new ResponseImpl(this.status);
        newResp.entity = this.entity;
        newResp.metadata = new MultivaluedMapImpl<String, Object>(this.metadata);
        return newResp;
    }

    /**
     * @see javax.ws.rs.core.Response#getEntity()
     */
    @Override
    public Object getEntity() {
        return this.entity;
    }

    /**
     * @see javax.ws.rs.core.Response#getMetadata()
     */
    @Override
    public MultivaluedMap<String, Object> getMetadata() {
        if (this.metadata == null)
            this.metadata = new MultivaluedMapImpl<String, Object>();
        return this.metadata;
    }

    /**
     * @see javax.ws.rs.core.Response#getStatus()
     */
    @Override
    public int getStatus() {
        return this.status;
    }

    void setEntity(Object entity) {
        this.entity = entity;
    }

    void setStatus(int status) {
        this.status = status;
    }
}