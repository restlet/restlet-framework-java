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

package org.restlet.engine.http.header;

import java.io.IOException;
import java.util.Collection;

import org.restlet.data.Dimension;
import org.restlet.data.Parameter;

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
    public static void addValues(Parameter header,
            Collection<Dimension> collection) {
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
