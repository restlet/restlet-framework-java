/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.platform.internal.model;

/**
 * Represents the contact of the person responsible for the Web API
 *
 * @author Cyprien Quilici
 * @deprecated Will be removed in 2.5 release.
 */
@Deprecated
public class Contact {

    /** The e-mail to use to join the contact of the Web API. */
    private String email;

    /** The name of the contact */
    private String name;

    /** The URL on which information about the Web API is. */
    private String url;

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
