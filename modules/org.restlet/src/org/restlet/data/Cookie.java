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

package org.restlet.data;

import java.util.Objects;

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
 * @see <a href="http://wiki.restlet.org/docs_2.2/58-restlet.html">User Guide - Getting parameter values</a>
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
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Cookie)) {
            // if obj isn't a cookie or is null don't evaluate further
            return false;
        }

        Cookie that = (Cookie) obj;

        return Objects.equals(getName(), that.getName())
                && Objects.equals(getValue(), that.getValue())
                && (this.version == that.version)
                && Objects.equals(getDomain(), that.getDomain())
                && Objects.equals(getPath(), that.getPath());
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
