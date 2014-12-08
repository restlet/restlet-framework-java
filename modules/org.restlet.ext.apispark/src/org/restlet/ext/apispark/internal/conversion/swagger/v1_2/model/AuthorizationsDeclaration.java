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
