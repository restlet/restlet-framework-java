package org.restlet.ext.apispark.internal.model.swagger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class TokenEndpointDeclaration {

    private String tokenName;

    private String url;

    public String getTokenName() {
        return tokenName;
    }

    public String getUrl() {
        return url;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
