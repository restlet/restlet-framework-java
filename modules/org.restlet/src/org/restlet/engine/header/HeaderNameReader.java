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

package org.restlet.engine.header;

import org.restlet.data.Header;
import org.restlet.data.HeaderName;

import java.io.IOException;
import java.util.Collection;

/**
 * HeaderName header reader.
 * 
 * @author Manuel Boillod
 */
public class HeaderNameReader extends HeaderReader<HeaderName> {

    /**
     * Adds values to the given collection.
     *
     * @param header
     *            The header to read.
     * @param collection
     *            The collection to update.
     */
    public static void addValues(Header header, Collection<HeaderName> collection) {
        new HeaderNameReader(header.getValue()).addValues(collection);
    }

    /**
     * Constructor.
     *
     * @param header
     *            The header to read.
     */
    public HeaderNameReader(String header) {
        super(header);
    }

    @Override
    public HeaderName readValue() throws IOException {
        return new HeaderName(readToken());
    }

}
