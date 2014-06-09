package org.restlet.ext.apispark.internal.swagger.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ImplicitDeclaration {

    private LoginEndpointDeclaration loginEndpoint;

    private String tokenName;

    public LoginEndpointDeclaration getLoginEndpoint() {
        return loginEndpoint;
    }

    public void setLoginEndpoint(LoginEndpointDeclaration loginEndpoint) {
        this.loginEndpoint = loginEndpoint;
    }

    public String getTokenName() {
        return tokenName;
    }

    public void setTokenName(String tokenName) {
        this.tokenName = tokenName;
    }
}
