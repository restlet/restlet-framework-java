package org.restlet.ext.apispark.internal.model.swagger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class TokenRequestEndpointDeclaration {

    private String clientIdName;

    private String clientSecretName;

    private String url;

    public String getClientIdName() {
        return clientIdName;
    }

    public String getClientSecretName() {
        return clientSecretName;
    }

    public String getUrl() {
        return url;
    }

    public void setClientIdName(String clientIdName) {
        this.clientIdName = clientIdName;
    }

    public void setClientSecretName(String clientSecretName) {
        this.clientSecretName = clientSecretName;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
