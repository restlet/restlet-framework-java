/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.platform.internal.conversion.swagger.v1_2.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * @deprecated Will be removed in 2.5 release.
 */
@Deprecated
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
