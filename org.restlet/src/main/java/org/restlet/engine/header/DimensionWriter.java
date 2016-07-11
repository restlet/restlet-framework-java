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

import java.util.Collection;

import org.restlet.data.Dimension;

/**
 * Dimension header writer.
 * 
 * @author Thierry Boileau
 */
public class DimensionWriter extends HeaderWriter<Dimension> {

    /**
     * Creates a vary header from the given dimensions.
     * 
     * @param dimensions
     *            The dimensions to copy to the response.
     * @return Returns the Vary header or null, if dimensions is null or empty.
     */
    public static String write(Collection<Dimension> dimensions) {
        return new DimensionWriter().append(dimensions).toString();
    }

    /**
     * Appends a collection of dimensions as a header.
     * 
     * @param dimensions
     *            The dimensions to format.
     * @return This writer.
     */
    public DimensionWriter append(Collection<Dimension> dimensions) {
        if ((dimensions != null) && !dimensions.isEmpty()) {
            if (dimensions.contains(Dimension.CLIENT_ADDRESS)
                    || dimensions.contains(Dimension.TIME)
                    || dimensions.contains(Dimension.UNSPECIFIED)) {
                // From an HTTP point of view the representations can
                // vary in unspecified ways
                append("*");
            } else {
                boolean first = true;

                for (Dimension dimension : dimensions) {
                    if (first) {
                        first = false;
                    } else {
                        append(", ");
                    }

                    append(dimension);
                }
            }
        }

        return this;
    }

    @Override
    public HeaderWriter<Dimension> append(Dimension dimension) {
        if (dimension == Dimension.CHARACTER_SET) {
            append(HeaderConstants.HEADER_ACCEPT_CHARSET);
        } else if (dimension == Dimension.CLIENT_AGENT) {
            append(HeaderConstants.HEADER_USER_AGENT);
        } else if (dimension == Dimension.ENCODING) {
            append(HeaderConstants.HEADER_ACCEPT_ENCODING);
        } else if (dimension == Dimension.LANGUAGE) {
            append(HeaderConstants.HEADER_ACCEPT_LANGUAGE);
        } else if (dimension == Dimension.MEDIA_TYPE) {
            append(HeaderConstants.HEADER_ACCEPT);
        } else if (dimension == Dimension.AUTHORIZATION) {
            append(HeaderConstants.HEADER_AUTHORIZATION);
        }

        return this;
    }

}
