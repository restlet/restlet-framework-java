package org.restlet.ext.swagger.internal.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class OAuth2AuthorizationDeclaration extends AuthorizationDeclaration {

    private List<ScopeDeclaration> scopes;

    private GrantTypesDeclaration grantTypes;

    public List<ScopeDeclaration> getScopes() {
        if (scopes == null) {
            scopes = new ArrayList<ScopeDeclaration>();
        }
        return scopes;
    }

    public void setScopes(List<ScopeDeclaration> scopes) {
        this.scopes = scopes;
    }

    public GrantTypesDeclaration getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(GrantTypesDeclaration grantTypes) {
        this.grantTypes = grantTypes;
    }
}
