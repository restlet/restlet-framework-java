/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
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

package org.restlet.ext.apispark.model;

import org.restlet.data.Status;

/**
 * 
 * @author Cyprien Quilici
 */
public class Response {

    /** Status code of the response */
    private int code;

    /** Textual description of this response */
    private String description;

    /** Custom content of the body if any. */
    private Entity entity;

    /** Status message of the response. */
    private String message;

    /** Name of this response */
    private String name;

    /**
     * Constructor. The default status code is {@link Status#SUCCESS_OK}.
     */
    public Response() {
        setCode(Status.SUCCESS_OK.getCode());
        setMessage(Status.SUCCESS_OK.getDescription());
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public Entity getEntity() {
        return entity;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setName(String name) {
        this.name = name;
    }
}
