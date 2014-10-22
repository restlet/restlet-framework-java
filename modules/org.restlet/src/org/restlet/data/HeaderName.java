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

package org.restlet.data;

import org.restlet.engine.util.SystemUtils;

/**
 * Represents an HTTP header nae.
 * 
 * @author Manuel Boillod
 */
public class HeaderName {

    /** The name. */
    private volatile String name;

    /**
     * Default constructor.
     */
    public HeaderName() {
    }

    /**
     * Constructor.
     *
     * @param name
     *            The header name.
     */
    public HeaderName(String name) {
        super();
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        // if obj == this no need to go further
        boolean result = (obj == this);

        if (!result) {
            result = obj instanceof HeaderName;

            // if obj isn't a header or is null don't evaluate further
            if (result) {
                HeaderName that = (HeaderName) obj;
                result = (((that.getName() == null) && (getName() == null)) || ((getName() != null) && getName()
                        .equals(that.getName())));
            }
        }

        return result;
    }

    /**
     * Returns the name.
     * 
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(getName());
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

    @Override
    public String toString() {
        return "[" + getName() + "]";
    }

}
