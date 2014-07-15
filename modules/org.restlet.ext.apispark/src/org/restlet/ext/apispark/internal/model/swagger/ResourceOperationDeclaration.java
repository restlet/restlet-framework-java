package org.restlet.ext.apispark.internal.model.swagger;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties("authorizations")
public class ResourceOperationDeclaration {
    private String method;

    private String nickname;

    private String type;

    @JsonProperty("$ref")
    private String ref;

    private ItemsDeclaration items;

    private List<ResourceOperationParameterDeclaration> parameters;

    private String summary;

    private List<ResponseMessageDeclaration> responseMessages;

    private String notes;

    private AuthorizationsDeclaration authorizations;

    private List<String> produces;

    private List<String> consumes;

    private String deprecated;

    // private String notes;
    // "errorResponses":[ ... ]

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ResourceOperationParameterDeclaration> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<ResourceOperationParameterDeclaration>();
        }
        return parameters;
    }

    public void setParameters(
            List<ResourceOperationParameterDeclaration> parameters) {
        this.parameters = parameters;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<ResponseMessageDeclaration> getResponseMessages() {
        if (responseMessages == null) {
            responseMessages = new ArrayList<ResponseMessageDeclaration>();
        }
        return responseMessages;
    }

    public void setResponseMessages(
            List<ResponseMessageDeclaration> responseMessages) {
        this.responseMessages = responseMessages;
    }

    public ItemsDeclaration getItems() {
        if (items == null) {
            items = new ItemsDeclaration();
        }
        return items;
    }

    public void setItems(ItemsDeclaration items) {
        this.items = items;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public AuthorizationsDeclaration getAuthorizations() {
        return authorizations;
    }

    public void setAuthorizations(AuthorizationsDeclaration authorizations) {
        this.authorizations = authorizations;
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

    public String getDeprecated() {
        return deprecated;
    }

    public void setDeprecated(String deprecated) {
        this.deprecated = deprecated;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}
