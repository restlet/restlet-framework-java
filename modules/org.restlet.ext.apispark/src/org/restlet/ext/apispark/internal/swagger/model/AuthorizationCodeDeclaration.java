package org.restlet.ext.apispark.internal.swagger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AuthorizationCodeDeclaration {

    private TokenRequestEndpointDeclaration tokenRequestEndpoint;

    private TokenEndpointDeclaration tokenEndpoint;

    public TokenRequestEndpointDeclaration getTokenRequestEndpoint() {
        return tokenRequestEndpoint;
    }

    public void setTokenRequestEndpoint(
            TokenRequestEndpointDeclaration tokenRequestEndpoint) {
        this.tokenRequestEndpoint = tokenRequestEndpoint;
    }

    public TokenEndpointDeclaration getTokenEndpoint() {
        return tokenEndpoint;
    }

    public void setTokenEndpoint(TokenEndpointDeclaration tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }
}
