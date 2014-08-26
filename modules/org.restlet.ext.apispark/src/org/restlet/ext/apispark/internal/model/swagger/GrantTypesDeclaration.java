package org.restlet.ext.apispark.internal.model.swagger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class GrantTypesDeclaration {

    private AuthorizationCodeDeclaration authorization_code;

    private ImplicitDeclaration implicit;

    public AuthorizationCodeDeclaration getAuthorization_code() {
        return authorization_code;
    }

    public ImplicitDeclaration getImplicit() {
        return implicit;
    }

    public void setAuthorization_code(
            AuthorizationCodeDeclaration authorization_code) {
        this.authorization_code = authorization_code;
    }

    public void setImplicit(ImplicitDeclaration implicit) {
        this.implicit = implicit;
    }
}
