package org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AuthorizationCodeDeclaration {

    private TokenEndpointDeclaration tokenEndpoint;

    private TokenRequestEndpointDeclaration tokenRequestEndpoint;

    public TokenEndpointDeclaration getTokenEndpoint() {
        return tokenEndpoint;
    }

    public TokenRequestEndpointDeclaration getTokenRequestEndpoint() {
        return tokenRequestEndpoint;
    }

    public void setTokenEndpoint(TokenEndpointDeclaration tokenEndpoint) {
        this.tokenEndpoint = tokenEndpoint;
    }

    public void setTokenRequestEndpoint(
            TokenRequestEndpointDeclaration tokenRequestEndpoint) {
        this.tokenRequestEndpoint = tokenRequestEndpoint;
    }
}
