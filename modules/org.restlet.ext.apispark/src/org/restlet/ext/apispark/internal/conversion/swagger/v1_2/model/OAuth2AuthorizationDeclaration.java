package org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class OAuth2AuthorizationDeclaration extends AuthorizationDeclaration {

    private GrantTypesDeclaration grantTypes;

    private List<ScopeDeclaration> scopes;

    public GrantTypesDeclaration getGrantTypes() {
        return grantTypes;
    }

    public List<ScopeDeclaration> getScopes() {
        if (scopes == null) {
            scopes = new ArrayList<ScopeDeclaration>();
        }
        return scopes;
    }

    public void setGrantTypes(GrantTypesDeclaration grantTypes) {
        this.grantTypes = grantTypes;
    }

    public void setScopes(List<ScopeDeclaration> scopes) {
        this.scopes = scopes;
    }
}
