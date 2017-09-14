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

package org.restlet.ext.jaxrs.internal.spi;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.restlet.engine.header.CookieSettingReader;
import org.restlet.engine.header.CookieSettingWriter;
import org.restlet.ext.jaxrs.internal.util.Converter;

/**
 * {@link HeaderDelegate} for {@link NewCookie}
 * 
 * @author Stephan Koops
 */
public class NewCookieHeaderDelegate implements HeaderDelegate<NewCookie> {

    /**
     * Obtain an instance of a HeaderDelegate for the NewCookie class.
     * 
     * @see RuntimeDelegate#createHeaderDelegate(Class)
     */
    NewCookieHeaderDelegate() {
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
    public NewCookie fromString(String string) throws IllegalArgumentException {
        return Converter.toJaxRsNewCookie(CookieSettingReader.read(string));
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
    public String toString(NewCookie newCookie) {
        return CookieSettingWriter.write(Converter
                .toRestletCookieSetting(newCookie));
    }
}
