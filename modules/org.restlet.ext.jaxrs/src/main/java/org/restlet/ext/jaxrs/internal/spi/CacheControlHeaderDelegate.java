/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jaxrs.internal.spi;

import java.util.List;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

import org.restlet.data.CacheDirective;
import org.restlet.engine.header.CacheDirectiveReader;
import org.restlet.engine.header.CacheDirectiveWriter;
import org.restlet.ext.jaxrs.internal.util.Converter;

/**
 * {@link HeaderDelegate} for {@link CacheControl}.
 * 
 * @author Stephan Koops
 * @deprecated Will be removed in next minor release.
 */
@Deprecated
public class CacheControlHeaderDelegate implements HeaderDelegate<CacheControl> {

    /**
     * Obtain an instance of a HeaderDelegate for the CacheControl class.
     * 
     * @see RuntimeDelegate#createHeaderDelegate(Class)
     */
    CacheControlHeaderDelegate() {
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
    public CacheControl fromString(String value)
            throws IllegalArgumentException {
        CacheDirectiveReader ccr = new CacheDirectiveReader(value);
        List<CacheDirective> cacheDirectives = ccr.readValues();
        return Converter.toJaxRsCacheControl(cacheDirectives);
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
    public String toString(CacheControl cacheControl) {
        List<CacheDirective> directives = Converter
                .toRestletCacheDirective(cacheControl);
        return CacheDirectiveWriter.write(directives);
    }
}
