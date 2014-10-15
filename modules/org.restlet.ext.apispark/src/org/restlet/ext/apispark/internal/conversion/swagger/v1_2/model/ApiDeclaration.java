package org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class ApiDeclaration {
    // private String resourcePath";
    private List<ResourceDeclaration> apis;

    private String apiVersion;

    private AuthorizationsDeclaration authorizations;

    private String basePath;

    private List<String> consumes;

    private ApiInfo info;

    private Map<String, ModelDeclaration> models;

    private List<String> produces;

    private String resourcePath;

    private String swaggerVersion;

    public List<ResourceDeclaration> getApis() {
        if (apis == null) {
            apis = new ArrayList<ResourceDeclaration>();
        }
        return apis;
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

    public List<String> getConsumes() {
        if (consumes == null) {
            consumes = new ArrayList<String>();
        }
        return consumes;
    }

    public ApiInfo getInfo() {
        return info;
    }

    public Map<String, ModelDeclaration> getModels() {
        if (models == null) {
            models = new HashMap<String, ModelDeclaration>();
        }
        return models;
    }

    public List<String> getProduces() {
        if (produces == null) {
            produces = new ArrayList<String>();
        }
        return produces;
    }

    public String getResourcePath() {
        return resourcePath;
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

    public void setConsumes(List<String> consumes) {
        this.consumes = consumes;
    }

    public void setInfo(ApiInfo info) {
        this.info = info;
    }

    public void setModels(Map<String, ModelDeclaration> models) {
        this.models = models;
    }

    public void setProduces(List<String> produces) {
        this.produces = produces;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public void setSwaggerVersion(String swaggerVersion) {
        this.swaggerVersion = swaggerVersion;
    }
}
