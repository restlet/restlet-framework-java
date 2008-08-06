/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.util;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Range;

/**
 * Range manipulation utilities.
 * 
 * @author Jerome Louvel
 */
public class RangeUtils {

    /**
     * Parse the Range header and returns the list of corresponding Range
     * objects.
     * 
     * @param rangeHeader
     *            The Range header value.
     * @return The list of corresponding Range objects.
     */
    public static List<Range> parseRangeHeader(String rangeHeader) {
        List<Range> result = null;
        String prefix = "bytes=";
        if (rangeHeader != null && rangeHeader.startsWith(prefix)) {
            result = new ArrayList<Range>();

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
                            value.length() - 2));
                    length = Range.SIZE_MAX;
                } else {
                    String[] tab = value.split("-");
                    if (tab.length == 2) {
                        index = Long.parseLong(tab[0]);
                        length = index + Long.parseLong(tab[1]);
                    }
                }
                result.add(new Range(index, length));
            }
        }

        return result;
    }
}
