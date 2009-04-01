/**
 * Copyright 2005-2009 Noelios Technologies.
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

import java.util.Date;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * {@link HeaderDelegate} for {@link MediaType}.
 * 
 * @author Stephan Koops
 */
public class DateHeaderDelegate implements HeaderDelegate<Date> {

    // TODO DateHeaderDelegate
    // TODO use headerDelegates, if one is available
    
    /**
     * Obtain an instance of a HeaderDelegate for the MediTape class.
     * 
     * @see RuntimeDelegate#createHeaderDelegate(Class)
     */
    public DateHeaderDelegate() {
    }

    /**
     * Parse the supplied value and create an instance of <code>T</code>.
     * 
     * @param contentType
     *                the contentType
     * @return the newly created instance of <code>T</code>
     * @throws IllegalArgumentException
     *                 if the supplied string cannot be parsed
     * @see javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate#fromString(java.lang.String)
     */
    public Date fromString(String date) throws IllegalArgumentException {
        throw new org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException(
                "HeaderDelegate<Date> is not yet implemented");
    }

    /**
     * Convert the supplied value to a String.
     * 
     * @param value
     *                the value of type <code>T</code>
     * @return a String representation of the value
     * @throws IllegalArgumentException
     *                 if the supplied object cannot be serialized
     * @see javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate#toString(java.lang.Object)
     */
    public String toString(Date date) {
        throw new org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException(
                "HeaderDelegate<Date> is not yet implemented");
    }
}