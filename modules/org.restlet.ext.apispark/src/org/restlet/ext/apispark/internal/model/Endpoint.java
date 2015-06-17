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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Web API endpoint. Declares the authentication protocol
 * associated
 * 
 * @author Cyprien Quilici
 */
public class Endpoint {

    /** Authentication protocol used for this endpoint */
    private String authenticationProtocol;

    /**
     * Base path for this endpoint.
     * 
     * Ex: http://example.com:8555/v1/admin => basePath = /v1/admin
     */
    private String basePath;

    /** The domain's name. */
    private String domain;

    /** The endpoint's port. */
    private Integer port;

    /** Protocol used for this endpoint. */
    private String protocol;

    public Endpoint() {
    }

    public Endpoint(String url) {
        Pattern p = Pattern
                .compile("([a-zA-Z]*)://([^:^/]*)(:([0-9]*))?([a-zA-Z0-9+&@#/%=~_|]*)");
        Matcher m = p.matcher(url);
        if (m.matches()) {
            domain = m.group(2);
            protocol = m.group(1);
            basePath = m.group(5);
            if (m.group(4) != null) {
                port = Integer.parseInt(m.group(4));
            }
        } else {
            throw new RuntimeException(url + " does not match URL pattern");
        }
    }

    /**
     * 
     * @param domain
     *            Domain of the endpoint
     * @param port
     *            Port of the endpoint. Value -1 is considered as null.
     * @param protocol
     *            Protocol of the endpoint
     * @param basePath
     *            Base path of the endpoint
     * @param authenticationProtocol
     *            Authentication scheme of the endpoint
     */
    public Endpoint(String domain, Integer port, String protocol,
            String basePath, String authenticationProtocol) {
        this.domain = domain;
        setPort(port);
        this.protocol = protocol;
        this.basePath = basePath;
        this.authenticationProtocol = authenticationProtocol;
    }

    public String computeUrl() {
        return protocol + "://" + domain + (port != null ? ":" + port : "")
                + (basePath != null ? basePath : "");
    }

    public String getAuthenticationProtocol() {
        return authenticationProtocol;
    }

    public String getBasePath() {
        return basePath;
    }

    public String getDomain() {
        return domain;
    }

    public Integer getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setAuthenticationProtocol(String authenticationProtocol) {
        this.authenticationProtocol = authenticationProtocol;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void setPort(Integer port) {
        if (port != null && port != -1) {
            this.port = port;
        } else {
            port = null;
        }
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
