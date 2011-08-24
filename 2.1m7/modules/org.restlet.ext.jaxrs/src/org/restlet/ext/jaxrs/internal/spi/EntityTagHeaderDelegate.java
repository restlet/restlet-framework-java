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

package org.restlet.ext.jaxrs.internal.spi;

import javax.ws.rs.core.EntityTag;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.restlet.data.Tag;
import org.restlet.ext.jaxrs.internal.util.Converter;

/**
 * {@link HeaderDelegate} for {@link EntityTag}
 * 
 * @author Stephan Koops
 */
public class EntityTagHeaderDelegate implements HeaderDelegate<EntityTag> {

    /**
     * Obtain an instance of a HeaderDelegate for the EnityTag class.
     * 
     * @see RuntimeDelegate#createHeaderDelegate(Class)
     */
    EntityTagHeaderDelegate() {
    }

    /**
     * Parse the supplied value and create an instance of <code>T</code>.
     * 
     * @param value
     *            the string value
     * @return the newly created instance of <code>T</code>
     * @throws IllegalArgumentException
     *             if the supplied string cannot be parsed
     * @see javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate#fromString(java.lang.String)
     */
    public EntityTag fromString(String string) throws IllegalArgumentException {
        return Converter.toJaxRsEntityTag(Tag.parse(string));
    }

    /**
     * Convert the supplied value to a String.
     * 
     * @param value
     *            the value of type <code>T</code>
     * @return a String representation of the value
     * @throws IllegalArgumentException
     *             if the supplied object cannot be serialized
     * @see javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate#toString(java.lang.Object)
     */
    public String toString(EntityTag entityTag) {
        return Converter.toRestletTag(entityTag).format();
    }
}