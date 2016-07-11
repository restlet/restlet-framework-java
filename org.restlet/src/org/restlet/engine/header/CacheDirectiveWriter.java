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

package org.restlet.engine.header;

import java.util.List;

import org.restlet.data.CacheDirective;

/**
 * Cache directive header writer.
 * 
 * @author Thierry Boileau
 */
public class CacheDirectiveWriter extends HeaderWriter<CacheDirective> {

    /**
     * Writes a list of cache directives with a comma separator.
     * 
     * @param directives
     *            The list of cache directives.
     * @return The formatted list of cache directives.
     */
    public static String write(List<CacheDirective> directives) {
        return new CacheDirectiveWriter().append(directives).toString();
    }

    @Override
    public CacheDirectiveWriter append(CacheDirective directive) {
        appendExtension(directive);
        return this;
    }

}
