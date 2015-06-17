/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ApiDeclaration {
    // private String resourcePath";
    private List<ResourceDeclaration> apis;

    private String apiVersion;

    private AuthorizationsDeclaration authorizations;

    private String basePath;

    private List<String> consumes;

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

    public Map<String, ModelDeclaration> getModels() {
        if (models == null) {
            models = new LinkedHashMap<String, ModelDeclaration>();
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
