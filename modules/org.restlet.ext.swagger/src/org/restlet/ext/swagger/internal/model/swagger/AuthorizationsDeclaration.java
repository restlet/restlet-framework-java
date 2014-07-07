package org.restlet.ext.swagger.internal.model.swagger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class AuthorizationsDeclaration {

    private BasicAuthorizationDeclaration basicAuth;

    private ApiKeyAuthorizationDeclaration apiKey;

    private OAuth2AuthorizationDeclaration oauth2;

    public BasicAuthorizationDeclaration getBasicAuth() {
        return basicAuth;
    }

    public void setBasicAuth(BasicAuthorizationDeclaration basicAuth) {
        this.basicAuth = basicAuth;
    }

    public ApiKeyAuthorizationDeclaration getApiKey() {
        return apiKey;
    }

    public void setApiKey(ApiKeyAuthorizationDeclaration apiKey) {
        this.apiKey = apiKey;
    }

    public OAuth2AuthorizationDeclaration getOauth2() {
        return oauth2;
    }

    public void setOauth2(OAuth2AuthorizationDeclaration oauth2) {
        this.oauth2 = oauth2;
    }
}
