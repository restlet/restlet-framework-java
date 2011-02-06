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

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Range;
import org.restlet.representation.Representation;

/**
 * Range header reader.
 * 
 * @author Jerome Louvel
 */
public class RangeReader {

    /**
     * Parse the Content-Range header value and update the given representation.
     * 
     * @param value
     *            Content-range header.
     * @param representation
     *            Representation to update.
     */
    public static void update(String value,
            Representation representation) {
        String prefix = "bytes ";
        if (value != null && value.startsWith(prefix)) {
            value = value.substring(prefix.length());

            int index = value.indexOf("-");
            int index1 = value.indexOf("/");

            if (index != -1) {
                int startIndex = Integer.parseInt(value.substring(0, index));
                int endIndex = Integer.parseInt(value.substring(index + 1,
                        index1));

                representation.setRange(new Range(startIndex, endIndex
                        - startIndex + 1));
            }

            String strLength = value.substring(index1 + 1, value.length());
            if (!("*".equals(strLength))) {
                representation.setSize(Long.parseLong(strLength));
            }
        }
    }

    /**
     * Parse the Range header and returns the list of corresponding Range
     * objects.
     * 
     * @param rangeHeader
     *            The Range header value.
     * @return The list of corresponding Range objects.
     */
    public static List<Range> read(String rangeHeader) {
        List<Range> result = new ArrayList<Range>();
        String prefix = "bytes=";
        if (rangeHeader != null && rangeHeader.startsWith(prefix)) {
            rangeHeader = rangeHeader.substring(prefix.length());

            String[] array = rangeHeader.split(",");
            for (int i = 0; i < array.length; i++) {
                String value = array[i].trim();
                long index = 0;
                long length = 0;
                if (value.startsWith("-")) {
                    index = Range.INDEX_LAST;
                    length = Long.parseLong(value.substring(1));
                } else if (value.endsWith("-")) {
                    index = Long.parseLong(value.substring(0,
                            value.length() - 1));
                    length = Range.SIZE_MAX;
                } else {
                    String[] tab = value.split("-");
                    if (tab.length == 2) {
                        index = Long.parseLong(tab[0]);
                        length = Long.parseLong(tab[1]) - index + 1;
                    }
                }
                result.add(new Range(index, length));
            }
        }

        return result;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private RangeReader() {
    }
}
