package org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AuthorizationsDeclaration {

    private ApiKeyAuthorizationDeclaration apiKey;

    private BasicAuthorizationDeclaration basicAuth;

    private OAuth2AuthorizationDeclaration oauth2;

    public ApiKeyAuthorizationDeclaration getApiKey() {
        return apiKey;
    }

    public BasicAuthorizationDeclaration getBasicAuth() {
        return basicAuth;
    }

    public OAuth2AuthorizationDeclaration getOauth2() {
        return oauth2;
    }

    public void setApiKey(ApiKeyAuthorizationDeclaration apiKey) {
        this.apiKey = apiKey;
    }

    public void setBasicAuth(BasicAuthorizationDeclaration basicAuth) {
        this.basicAuth = basicAuth;
    }

    public void setOauth2(OAuth2AuthorizationDeclaration oauth2) {
        this.oauth2 = oauth2;
    }
}
