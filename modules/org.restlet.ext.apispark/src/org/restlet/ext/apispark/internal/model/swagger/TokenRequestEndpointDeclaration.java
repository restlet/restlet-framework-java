package org.restlet.ext.apispark.internal.model.swagger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class TokenRequestEndpointDeclaration {

    private String url;

    private String clientIdName;

    private String clientSecretName;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getClientIdName() {
        return clientIdName;
    }

    public void setClientIdName(String clientIdName) {
        this.clientIdName = clientIdName;
    }

    public String getClientSecretName() {
        return clientSecretName;
    }

    public void setClientSecretName(String clientSecretName) {
        this.clientSecretName = clientSecretName;
    }
}
