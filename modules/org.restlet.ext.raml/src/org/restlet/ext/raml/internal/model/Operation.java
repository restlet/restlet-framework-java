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

package org.restlet.ext.raml.internal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author
 */
public class Operation {

    /** Textual description of this operation. */
    private String description;

    /** Headers to use for this operation. */
    private List<Header> headers;

    /** Representation retrieved by this operation if any. */
    private Body inRepresentation;

    /** HTTP method for this operation. */
    private String method;

    /**
     * Unique name for this operation<br>
     * Note: will be used for client SDK generation in the future.
     */
    private String name;

    /**
     * Representation to send in the body of your request for this operation if
     * any.
     */
    private Body outRepresentation;

    /** Query parameters available for this operation. */
    private List<QueryParameter> queryParameters;

    /** Possible response messages you could encounter. */
    private List<Response> responses;

    /** Mediatypes produced by this operation */
    private List<String> produces;

    /** Mediatypes consumed by this operation */
    private List<String> consumes;

    public String getDescription() {
        return description;
    }

    public List<Header> getHeaders() {
        if (headers == null) {
            headers = new ArrayList<Header>();
        }
        return headers;
    }

    public Body getInRepresentation() {
        return inRepresentation;
    }

    public String getMethod() {
        return method;
    }

    public String getName() {
        return name;
    }

    public Body getOutRepresentation() {
        return outRepresentation;
    }

    public List<QueryParameter> getQueryParameters() {
        if (queryParameters == null) {
            queryParameters = new ArrayList<QueryParameter>();
        }
        return queryParameters;
    }

    public List<Response> getResponses() {
        if (responses == null) {
            responses = new ArrayList<Response>();
        }
        return responses;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public void setInRepresentation(Body inRepresentation) {
        this.inRepresentation = inRepresentation;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOutRepresentation(Body outRepresentation) {
        this.outRepresentation = outRepresentation;
    }

    public void setQueryParameters(List<QueryParameter> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public void setResponses(List<Response> responses) {
        this.responses = responses;
    }

    public List<String> getProduces() {
        if (produces == null) {
            produces = new ArrayList<String>();
        }
        return produces;
    }

    public void setProduces(List<String> produces) {
        this.produces = produces;
    }

    public List<String> getConsumes() {
        if (consumes == null) {
            consumes = new ArrayList<String>();
        }
        return consumes;
    }

    public void setConsumes(List<String> consumes) {
        this.consumes = consumes;
    }
}
