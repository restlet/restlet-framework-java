package org.restlet.ext.apispark.internal.conversion.swagger.v1_2.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ApiInfo {
    private String contact;

    private String description;

    private String license;

    private String licenseUrl;

    private String termsOfServiceUrl;

    private String title;

    public String getContact() {
        return contact;
    }

    public String getDescription() {
        return description;
    }

    public String getLicense() {
        return license;
    }

    public String getLicenseUrl() {
        return licenseUrl;
    }

    public String getTermsOfServiceUrl() {
        return termsOfServiceUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public void setTermsOfServiceUrl(String termsOfServiceUrl) {
        this.termsOfServiceUrl = termsOfServiceUrl;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
