package org.restlet.ext.apispark.internal.model.swagger;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ResourceListing {
    // private String resourcePath";
    private List<ResourceDeclaration> apis;

    private String apiVersion;

    private AuthorizationsDeclaration authorizations;

    private String basePath;

    private ApiInfo info;

    private String swaggerVersion;

    public List<ResourceDeclaration> getApis() {
        if (apis == null) {
            apis = new ArrayList<ResourceDeclaration>();
        }
        return apis;
    }

    public ResourceDeclaration getApi(String path) {
        for (ResourceDeclaration rd : apis) {
            if (path.equals(rd.getPath())) {
                return rd;
            }
        }
        return null;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public AuthorizationsDeclaration getAuthorizations() {
        return authorizations;
    }

    public String getBasePath() {
        return basePath;
    }

    public ApiInfo getInfo() {
        return info;
    }

    public String getSwaggerVersion() {
        return swaggerVersion;
    }

    public void setApis(List<ResourceDeclaration> apis) {
        this.apis = apis;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public void setAuthorizations(AuthorizationsDeclaration authorizations) {
        this.authorizations = authorizations;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setInfo(ApiInfo info) {
        this.info = info;
    }

    public void setSwaggerVersion(String swaggerVersion) {
        this.swaggerVersion = swaggerVersion;
    }
}
