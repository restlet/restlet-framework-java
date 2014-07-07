package org.restlet.ext.swagger.internal.swagger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ApiKeyAuthorizationDeclaration extends AuthorizationDeclaration {

    private String passAs;

    private String keyname;

    public String getPassAs() {
        return passAs;
    }

    public void setPassAs(String passAs) {
        this.passAs = passAs;
    }

    public String getKeyname() {
        return keyname;
    }

    public void setKeyname(String keyname) {
        this.keyname = keyname;
    }
}
