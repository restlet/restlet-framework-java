/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Status;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents a response sent by a Web API resource
 * 
 * @author Cyprien Quilici
 */
public class Response {

    /** Status code of the response. */
    private int code;

    /** Textual description of this response. */
    private String description;

    /** The list of Headers associated with this response. */
    private List<Header> headers = new ArrayList<>();

    /** Status message of the response, will be deleted as description is sufficient. */
    @Deprecated
    private String message;

    /** Name of this response. */
    private String name;

    /**
     * Custom content of the body if any. Could be null if the response is of
     * type "204 - No Content"
     */
    private PayLoad outputPayLoad;

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

    @JsonInclude(Include.NON_EMPTY)
    public List<Header> getHeaders() {
        return headers;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public PayLoad getOutputPayLoad() {
        return outputPayLoad;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOutputPayLoad(PayLoad outputPayLoad) {
        this.outputPayLoad = outputPayLoad;
    }
}
