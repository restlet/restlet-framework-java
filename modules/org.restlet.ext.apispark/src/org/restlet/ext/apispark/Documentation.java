package org.restlet.ext.apispark;

public class Documentation {

    /** Any useful information for a user that plans to access to the API. */
    private String contact;

    /** Contract of this API. */
    private Contract contract;

    /**
     * Base URL on which you can access the API<br>
     * Note: will enable multiple endpoints and protocols in the future (use
     * class Endpoint in a list).
     */
    private String endpoint;

    /** URL of the description of the license used by the API. */
    private String license;

    /** Current version of the API. */
    private String version;

    public String getContact() {
        return contact;
    }

    public Contract getContract() {
        return contract;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getLicense() {
        return license;
    }

    public String getVersion() {
        return version;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setContract(Contract contract) {
        this.contract = contract;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
