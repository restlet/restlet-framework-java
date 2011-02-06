/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.data;

import org.restlet.Request;
import org.restlet.engine.util.SystemUtils;

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
 * @see <a
 *      href="http://wiki.restlet.org/docs_2.0/13-restlet/27-restlet/330-restlet/58-restlet.html">User
 *      Guide - Getting parameter values</a>
 * @author Jerome Louvel
 */
public class Cookie extends Parameter {
    /** The domain name. */
    private volatile String domain;

    /** The validity path. */
    private volatile String path;

    /** The version number. */
    private volatile int version;

    /**
     * Constructor.
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
        super(name, value);
        this.version = version;
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
        boolean result = (obj == this);

        // if obj == this no need to go further
        if (!result) {
            // test for equality at Parameter level i.e. name and value.
            if (super.equals(obj)) {
                // if obj isn't a cookie or is null don't evaluate further
                if (obj instanceof Cookie) {
                    final Cookie that = (Cookie) obj;
                    result = (this.version == that.version);
                    if (result) // if versions are equal test domains
                    {
                        if (!(this.domain == null)) // compare domains
                        // taking care of nulls
                        {
                            result = (this.domain.equals(that.domain));
                        } else {
                            result = (that.domain == null);
                        }

                        if (result) // if domains are equal test the paths
                        {
                            if (!(this.path == null)) // compare paths taking
                            // care of nulls
                            {
                                result = (this.path.equals(that.path));
                            } else {
                                result = (that.path == null);
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
     * Returns the validity path.
     * 
     * @return The validity path.
     */
    public String getPath() {
        return this.path;
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
        return SystemUtils.hashCode(super.hashCode(), getVersion(), getPath(),
                getDomain());
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
     * Sets the validity path.
     * 
     * @param path
     *            The validity path.
     */
    public void setPath(String path) {
        this.path = path;
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
}
