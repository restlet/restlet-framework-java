package org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class BasicAuthorizationDeclaration extends AuthorizationDeclaration {

    public BasicAuthorizationDeclaration() {
        this.setType(AuthorizationType.BASIC_AUTH);
    }
}
