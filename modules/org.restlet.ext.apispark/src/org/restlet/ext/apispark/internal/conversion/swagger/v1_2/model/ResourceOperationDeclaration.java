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

package org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties("authorizations")
public class ResourceOperationDeclaration {
    private AuthorizationsDeclaration authorizations;

    private List<String> consumes;

    private String deprecated;

    private ItemsDeclaration items;

    private String method;

    private String nickname;

    private String notes;

    private List<ResourceOperationParameterDeclaration> parameters;

    private List<String> produces;

    @JsonProperty("$ref")
    private String ref;

    private List<ResponseMessageDeclaration> responseMessages;

    private String summary;

    private String type;

    // private String notes;
    // "errorResponses":[ ... ]

    public AuthorizationsDeclaration getAuthorizations() {
        return authorizations;
    }

    public List<String> getConsumes() {
        if (consumes == null) {
            consumes = new ArrayList<String>();
        }
        return consumes;
    }

    public String getDeprecated() {
        return deprecated;
    }

    public ItemsDeclaration getItems() {
        if (items == null) {
            items = new ItemsDeclaration();
        }
        return items;
    }

    public String getMethod() {
        return method;
    }

    public String getNickname() {
        return nickname;
    }

    public String getNotes() {
        return notes;
    }

    public List<ResourceOperationParameterDeclaration> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<ResourceOperationParameterDeclaration>();
        }
        return parameters;
    }

    public List<String> getProduces() {
        if (produces == null) {
            produces = new ArrayList<String>();
        }
        return produces;
    }

    public String getRef() {
        return ref;
    }

    public List<ResponseMessageDeclaration> getResponseMessages() {
        if (responseMessages == null) {
            responseMessages = new ArrayList<ResponseMessageDeclaration>();
        }
        return responseMessages;
    }

    public String getSummary() {
        return summary;
    }

    public String getType() {
        return type;
    }

    public void setAuthorizations(AuthorizationsDeclaration authorizations) {
        this.authorizations = authorizations;
    }

    public void setConsumes(List<String> consumes) {
        this.consumes = consumes;
    }

    public void setDeprecated(String deprecated) {
        this.deprecated = deprecated;
    }

    public void setItems(ItemsDeclaration items) {
        this.items = items;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setParameters(
            List<ResourceOperationParameterDeclaration> parameters) {
        this.parameters = parameters;
    }

    public void setProduces(List<String> produces) {
        this.produces = produces;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void setResponseMessages(
            List<ResponseMessageDeclaration> responseMessages) {
        this.responseMessages = responseMessages;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setType(String type) {
        this.type = type;
    }
}
