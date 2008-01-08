/*
 * Copyright 2005-2007 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */
package org.restlet.ext.jaxrs.spi;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.restlet.ext.jaxrs.todo.NotYetImplementedException;

/**
 * @author Stephan Koops
 *
 */
public class NewCookieHeaderDelegate implements HeaderDelegate<MediaType> {

    /**
     * Obtain an instance of a HeaderDelegate for the supplied class. An 
     * implementation is required to support the following classes:
     * Cookie, CacheControl, EntityTag, NewCookie, MediaType.
     */
    public NewCookieHeaderDelegate() {
        // TODO Auto-generated constructor stub
        throw new NotYetImplementedException();
    }

    /**
     * Parse the supplied value and create an instance of <code>T</code>.
     * @param value the string value
     * @return the newly created instance of <code>T</code>
     * @throws IllegalArgumentException if the supplied string cannot be parsed
     * @see javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate#fromString(java.lang.String)
     */
    public MediaType fromString(String value) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }


    /**
     * Convert the supplied value to a String.
     * @param value the value of type <code>T</code>
     * @return a String representation of the value
     * @throws IllegalArgumentException if the supplied object cannot be serialized
     * @see javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate#toString(java.lang.Object)
     */
    public String toString(MediaType value) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }
}