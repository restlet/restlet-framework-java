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

package org.restlet.ext.apispark.internal.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Represents a Web API
 * 
 * @author Cyprien Quilici
 */
public class Definition {

    /** RWADef version of the definition */
    public static final String SPEC_VERSION = "1.0.0";

    /** Company or individual's name */
    private String attribution;

    /** Any useful information for a user that plans to access to the API. */
    private Contact contact;

    /** Contract of this API. */
    private Contract contract = new Contract();

    /**
     * Endpoints on which one can access the Web API.
     */
    private List<Endpoint> endpoints = new ArrayList<>();

    /** A list of the keywords describing the Web API */
    private List<String> keywords = new ArrayList<>();

    /** URL of the description of the license used by the API. */
    private License license;

    /** The spec version, this field is used for serialization */
    private final String specVersion = SPEC_VERSION;

    /** A textual description of the terms of service of the Web API */
    private String termsOfService;

    /** Current version of the API. */
    private String version;

    public String getAttribution() {
        return attribution;
    }

    public Contact getContact() {
        return contact;
    }

    public Contract getContract() {
        return contract;
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    @JsonInclude(Include.NON_EMPTY)
    public List<String> getKeywords() {
        return keywords;
    }

    public License getLicense() {
        return license;
    }

    public String getSpecVersion() {
        return specVersion;
    }

    public String getTermsOfService() {
        return termsOfService;
    }

    public String getVersion() {
        return version;
    }

    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public void setTermsOfService(String termsOfService) {
        this.termsOfService = termsOfService;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
