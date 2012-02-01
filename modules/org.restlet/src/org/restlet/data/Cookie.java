/**
 * Copyright 2005-2012 Restlet S.A.S.
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
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.data;

import org.restlet.Request;
import org.restlet.engine.util.SystemUtils;
import org.restlet.util.NamedValue;

/**
 * Cookie provided by a client. To get the list of all cookies sent by a client,
 * you can use the {@link Request#getCookies()} method.<br>
 * <br>
 * Note that if you are on the server side and want to set a cookie on the
 * client, you should use the {@link CookieSetting} class instead.<br>
 * <br>
 * Note that when used with HTTP connectors, this class maps to the "Cookie"
 * header.
 * 
 * @see Request#getCookies()
 * @see <a href="http://wiki.restlet.org/docs_2.1/58-restlet.html">User Guide -
 *      Getting parameter values</a>
 * @author Jerome Louvel
 */
public class Cookie implements NamedValue<String> {

    /** The domain name. */
    private volatile String domain;

    /** The name. */
    private volatile String name;

    /** The validity path. */
    private volatile String path;

    /** The value. */
    private volatile String value;

    /** The version number. */
    private volatile int version;

    /**
     * Default constructor.
     */
    public Cookie() {
        this(0, null, null, null, null);
    }

    /**
     * Constructor.
     * 
     * @param version
     *            The version number.
     * @param name
     *            The name.
     * @param value
     *            The value.
     */
    public Cookie(int version, String name, String value) {
        this(version, name, value, null, null);
    }

    /**
     * Constructor.
     * 
     * @param version
     *            The version number.
     * @param name
     *            The name.
     * @param value
     *            The value.
     * @param path
     *            The validity path.
     * @param domain
     *            The domain name.
     */
    public Cookie(int version, String name, String value, String path,
            String domain) {
        this.version = version;
        this.name = name;
        this.value = value;
        this.path = path;
        this.domain = domain;
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The name.
     * @param value
     *            The value.
     */
    public Cookie(String name, String value) {
        this(0, name, value, null, null);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        // if obj == this no need to go further
        boolean result = (obj == this);

        if (!result) {
            result = obj instanceof Cookie;

            // if obj isn't a cookie or is null don't evaluate further
            if (result) {
                Cookie that = (Cookie) obj;
                result = (((that.getName() == null) && (getName() == null)) || ((getName() != null) && getName()
                        .equals(that.getName())));

                // if names are both null or equal continue
                if (result) {
                    result = (((that.getValue() == null) && (getValue() == null)) || ((getValue() != null) && getValue()
                            .equals(that.getValue())));

                    // if values are both null or equal continue
                    if (result) {
                        result = (this.version == that.version);

                        // if versions are equal continue
                        if (result) {
                            result = (((that.getDomain() == null) && (getDomain() == null)) || ((getDomain() != null) && getDomain()
                                    .equals(that.getDomain())));

                            // if domains are equal continue
                            if (result) {
                                // compare paths taking
                                result = (((that.getPath() == null) && (getPath() == null)) || ((getPath() != null) && getPath()
                                        .equals(that.getPath())));
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the domain name.
     * 
     * @return The domain name.
     */
    public String getDomain() {
        return this.domain;
    }

    /**
     * Returns the name.
     * 
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the validity path.
     * 
     * @return The validity path.
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Returns the value.
     * 
     * @return The value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Returns the cookie specification version.
     * 
     * @return The cookie specification version.
     */
    public int getVersion() {
        return this.version;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(getName(), getValue(), getVersion(),
                getPath(), getDomain());
    }

    /**
     * Sets the domain name.
     * 
     * @param domain
     *            The domain name.
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the validity path.
     * 
     * @param path
     *            The validity path.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Sets the value.
     * 
     * @param value
     *            The value.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Sets the cookie specification version.
     * 
     * @param version
     *            The cookie specification version.
     */
    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Cookie [domain=" + domain + ", name=" + name + ", path=" + path
                + ", value=" + value + ", version=" + version + "]";
    }
}
