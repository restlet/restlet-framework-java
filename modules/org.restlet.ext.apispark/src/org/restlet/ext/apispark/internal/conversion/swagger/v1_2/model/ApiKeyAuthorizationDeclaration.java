package org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ApiKeyAuthorizationDeclaration extends AuthorizationDeclaration {

    private String keyname;

    private String passAs;

    public String getKeyname() {
        return keyname;
    }

    public String getPassAs() {
        return passAs;
    }

    public void setKeyname(String keyname) {
        this.keyname = keyname;
    }

    public void setPassAs(String passAs) {
        this.passAs = passAs;
    }
}
