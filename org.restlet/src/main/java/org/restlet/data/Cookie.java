/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import java.util.Objects;
import org.restlet.Request;
import org.restlet.engine.util.SystemUtils;
import org.restlet.util.NamedValue;

/**
 * Cookie provided by a client. To get the list of all cookies sent by a client, you can use the
 * {@link Request#getCookies()} method.<br>
 * <br>
 * Note that if you are on the server side and want to set a cookie on the client, you should use
 * the {@link CookieSetting} class instead.<br>
 * <br>
 * Note that when used with HTTP connectors, this class maps to the "Cookie" header.
 *
 * @see Request#getCookies()
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

    /** Default constructor. */
    public Cookie() {
        this(null, null, null, null);
    }

    /**
     * Constructor.
     *
     * @param name The name.
     * @param value The value.
     * @param path The validity path.
     * @param domain The domain name.
     */
    public Cookie(String name, String value, String path, String domain) {
        this.name = name;
        this.value = value;
        this.path = path;
        this.domain = domain;
    }

    /**
     * Constructor.
     *
     * @param name The name.
     * @param value The value.
     */
    public Cookie(String name, String value) {
        this(name, value, null, null);
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Cookie that)) {
            return false;
        }

        return Objects.equals(getName(), that.getName())
                && Objects.equals(getValue(), that.getValue())
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

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(getName(), getValue(), getPath(), getDomain());
    }

    /**
     * Sets the domain name.
     *
     * @param domain The domain name.
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Sets the name.
     *
     * @param name The name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the validity path.
     *
     * @param path The validity path.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Sets the value.
     *
     * @param value The value.
     */
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Cookie [domain="
                + domain
                + ", name="
                + name
                + ", path="
                + path
                + ", value="
                + value
                + "]";
    }
}
