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

package org.restlet.ext.jaxrs.internal.core;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Implementation of {@link Response}.
 * 
 * @author Stephan Koops
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
        final ResponseImpl newResp = new ResponseImpl(this.status);
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
     * Get metadata associated with the response as a map. The returned map may
     * be subsequently modified by the JAX-RS runtime.
     * 
     * @return response metadata as a map
     * @see javax.ws.rs.core.Response#getMetadata()
     */
    @Override
    public MultivaluedMap<String, Object> getMetadata() {
        // TODO use HeaderDelegate to serialize
        if (this.metadata == null) {
            this.metadata = new MultivaluedMapImpl<String, Object>();
        }
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