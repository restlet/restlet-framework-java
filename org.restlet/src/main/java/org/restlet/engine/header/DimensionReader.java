/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
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
     * @param header The header to read.
     * @param collection The collection to update.
     */
    public static void addValues(Header header, Collection<Dimension> collection) {
        new DimensionReader(header.getValue()).addValues(collection);
    }

    /**
     * Constructor.
     *
     * @param header The header to read.
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
            } else if (value.equalsIgnoreCase(HeaderConstants.HEADER_ACCEPT_CHARSET)) {
                result = Dimension.CHARACTER_SET;
            } else if (value.equalsIgnoreCase(HeaderConstants.HEADER_ACCEPT_ENCODING)) {
                result = Dimension.ENCODING;
            } else if (value.equalsIgnoreCase(HeaderConstants.HEADER_ACCEPT_LANGUAGE)) {
                result = Dimension.LANGUAGE;
            } else if (value.equalsIgnoreCase(HeaderConstants.HEADER_AUTHORIZATION)) {
                result = Dimension.AUTHORIZATION;
            } else if (value.equalsIgnoreCase(HeaderConstants.HEADER_USER_AGENT)) {
                result = Dimension.CLIENT_AGENT;
            } else if (value.equalsIgnoreCase(HeaderConstants.HEADER_ORIGIN)) {
                result = Dimension.ORIGIN;
            } else if (value.equals("*")) {
                result = Dimension.UNSPECIFIED;
            }
        }

        return result;
    }
}
