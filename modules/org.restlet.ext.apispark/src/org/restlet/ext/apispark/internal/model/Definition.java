/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
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

/**
 * 
 * @author
 */
public class Definition {

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
    
    /** RWADef version of the definition */
    private final String rwadefVersion = "1.0";

    public String getRwadefVersion() {
		return rwadefVersion;
	}

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
