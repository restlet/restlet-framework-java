package org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class ResourceListing {
    // private String resourcePath";
    private List<ResourceListingApi> apis;

    private String apiVersion;

    private AuthorizationsDeclaration authorizations;

    private String basePath;

    private ApiInfo info;

    private String swaggerVersion;

    public List<ResourceListingApi> getApis() {
        if (apis == null) {
            apis = new ArrayList<ResourceListingApi>();
        }
        return apis;
    }

    public ResourceListingApi getApi(String path) {
        for (ResourceListingApi api : apis) {
            if (path.equals(api.getPath())) {
                return api;
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

    public void setApis(List<ResourceListingApi> apis) {
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
