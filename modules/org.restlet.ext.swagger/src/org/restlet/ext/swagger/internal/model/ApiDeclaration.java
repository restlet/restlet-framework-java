package org.restlet.ext.swagger.internal.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ApiDeclaration {
    private String apiVersion;

    private String swaggerVersion;

    private String basePath;

    private String resourcePath;

    private List<String> produces;

    private List<String> consumes;

    // private String resourcePath";
    private List<ResourceDeclaration> apis;

    private Map<String, ModelDeclaration> models;

    private AuthorizationsDeclaration authorizations;

    private ApiInfo info;

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

    public Map<String, ModelDeclaration> getModels() {
        if (models == null) {
            models = new HashMap<String, ModelDeclaration>();
        }
        return models;
    }

    public void setModels(Map<String, ModelDeclaration> models) {
        this.models = models;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public List<String> getProduces() {
        if (produces == null) {
            produces = new ArrayList<String>();
        }
        return produces;
    }

    public void setProduces(List<String> produces) {
        this.produces = produces;
    }

    public List<String> getConsumes() {
        if (consumes == null) {
            consumes = new ArrayList<String>();
        }
        return consumes;
    }

    public void setConsumes(List<String> consumes) {
        this.consumes = consumes;
    }

    public AuthorizationsDeclaration getAuthorizations() {
        return authorizations;
    }

    public void setAuthorizations(AuthorizationsDeclaration authorizations) {
        this.authorizations = authorizations;
    }
}
