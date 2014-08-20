package org.restlet.ext.swagger.internal.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ResourceListing {
    private String apiVersion;

    private String swaggerVersion;

    private String basePath;

    // private String resourcePath";
    private List<ResourceDeclaration> apis;

    private ApiInfo info;

    private AuthorizationsDeclaration authorizations;

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getSwaggerVersion() {
        return swaggerVersion;
    }

    public void setSwaggerVersion(String swaggerVersion) {
        this.swaggerVersion = swaggerVersion;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public List<ResourceDeclaration> getApis() {
        if (apis == null) {
            apis = new ArrayList<ResourceDeclaration>();
        }
        return apis;
    }

    public void setApis(List<ResourceDeclaration> apis) {
        this.apis = apis;
    }

    public ApiInfo getInfo() {
        return info;
    }

    public void setInfo(ApiInfo info) {
        this.info = info;
    }

    public AuthorizationsDeclaration getAuthorizations() {
        return authorizations;
    }

    public void setAuthorizations(AuthorizationsDeclaration authorizations) {
        this.authorizations = authorizations;
    }
}
