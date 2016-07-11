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

import java.io.IOException;
import java.util.Collection;

import org.restlet.data.Dimension;
import org.restlet.data.Header;

/**
 * Dimension header reader.
 * 
 * @author Jerome Louvel
 */
public class DimensionReader extends HeaderReader<Dimension> {
    /**
     * Adds values to the given collection.
     * 
     * @param header
     *            The header to read.
     * @param collection
     *            The collection to update.
     */
    public static void addValues(Header header, Collection<Dimension> collection) {
        new DimensionReader(header.getValue()).addValues(collection);
    }

    /**
     * Constructor.
     * 
     * @param header
     *            The header to read.
     */
    public DimensionReader(String header) {
        super(header);
    }

    @Override
    public Dimension readValue() throws IOException {
        Dimension result = null;
        String value = readRawValue();

        if (value != null) {
            if (value.equalsIgnoreCase(HeaderConstants.HEADER_ACCEPT)) {
                result = Dimension.MEDIA_TYPE;
            } else if (value
                    .equalsIgnoreCase(HeaderConstants.HEADER_ACCEPT_CHARSET)) {
                result = Dimension.CHARACTER_SET;
            } else if (value
                    .equalsIgnoreCase(HeaderConstants.HEADER_ACCEPT_ENCODING)) {
                result = Dimension.ENCODING;
            } else if (value
                    .equalsIgnoreCase(HeaderConstants.HEADER_ACCEPT_LANGUAGE)) {
                result = Dimension.LANGUAGE;
            } else if (value
                    .equalsIgnoreCase(HeaderConstants.HEADER_AUTHORIZATION)) {
                result = Dimension.AUTHORIZATION;
            } else if (value
                    .equalsIgnoreCase(HeaderConstants.HEADER_USER_AGENT)) {
                result = Dimension.CLIENT_AGENT;
            } else if (value.equals("*")) {
                result = Dimension.UNSPECIFIED;
            }
        }

        return result;
    }

}
