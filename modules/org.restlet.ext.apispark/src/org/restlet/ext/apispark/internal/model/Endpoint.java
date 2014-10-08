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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Web API endpoint. Declares the authentication protocol
 * associated
 * 
 * @author Cyprien Quilici
 */
public class Endpoint {

    /** The domain's name. */
    private String domain;

    /** The endpoint's port. */
    private int port;

    /** Protocol used for this endpoint. */
    private String protocol;

    /**
     * Base path for this endpoint.
     * 
     * Ex: http://example.com:8555/v1/admin => basePath = /v1/admin
     */
    private String basePath;

    /** Authentication protocol used for this endpoint */
    private String authenticationProtocol;

    public Endpoint(String domain, int port, String protocol,
            String basePath, String authenticationProtocol) {
        this.domain = domain;
        this.port = port;
        this.protocol = protocol;
        this.basePath = basePath;
        this.authenticationProtocol = authenticationProtocol;
    }

    public Endpoint(String url) {
        Pattern p = Pattern
                .compile("([a-z]*)://([^:^/]*)(:([0-9]*))?([a-zA-Z0-9+&@#/%=~_|]*)");
        Matcher m = p.matcher(url);
        if (m.matches()) {
            domain = m.group(2);
            protocol = m.group(1);
            basePath = m.group(5);
            if (m.group(4) != null) {
                port = Integer.parseInt(m.group(4));
            } else {
                port = 80;
            }
        } else {
            throw new RuntimeException("url does not match URL pattern: " + url);
        }
    }

    public Endpoint() {
    }

    public String computeUrl() {
        return protocol + "://" + domain
                + (port != 80 ? ":" + port : "") + basePath;
    }

    public String getDomain() {
        return domain;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public String getAuthenticationProtocol() {
        return authenticationProtocol;
    }

    public void setAuthenticationProtocol(String authenticationProtocol) {
        this.authenticationProtocol = authenticationProtocol;
    }
}
