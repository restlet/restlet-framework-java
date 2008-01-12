/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.data;

import org.restlet.util.Engine;

/**
 * Cookie provided by a client.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Cookie extends Parameter {
    /** The version number. */
    private int version;

    /** The validity path. */
    private String path;

    /** The domain name. */
    private String domain;

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
     *                The version number.
     * @param name
     *                The name.
     * @param value
     *                The value.
     */
    public Cookie(int version, String name, String value) {
        this(version, name, value, null, null);
    }

    /**
     * Constructor.
     * 
     * @param version
     *                The version number.
     * @param name
     *                The name.
     * @param value
     *                The value.
     * @param path
     *                The validity path.
     * @param domain
     *                The domain name.
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
     *                The name.
     * @param value
     *                The value.
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
                if ((obj instanceof Cookie) && obj != null) {
                    Cookie that = (Cookie) obj;
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
        return Engine.hashCode(super.hashCode(), getVersion(), getPath(),
                getDomain());
    }

    /**
     * Sets the domain name.
     * 
     * @param domain
     *                The domain name.
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Sets the validity path.
     * 
     * @param path
     *                The validity path.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Sets the cookie specification version.
     * 
     * @param version
     *                The cookie specification version.
     */
    public void setVersion(int version) {
        this.version = version;
    }

}
