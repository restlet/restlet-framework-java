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

package org.restlet.engine.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Emulate List functions missing from GWT port of List
 * 
 * @author Rob Heittman
 */
public class ListUtils {
    /**
     * Unlike List.subList(), which returns a live view of a set of List
     * elements, this method returns a new copy of the list. List.subList() is
     * not available in GWT 1.5 and was removed on purpose.
     * 
     * @param list
     *                The source List
     * @param fromIndex
     *                Starting index in the source List
     * @param toIndex
     *                Ending index in the source List
     * @throws IndexOutOfBoundsException
     *                 Call exceeds the bounds of the source List
     * @throws IllegalArgumentException
     *                 fromIndex and toIndex are not in sequence
     * @return a copy of the selected range
     */
    public static <T> List<T> copySubList(List<T> list, int fromIndex,
            int toIndex) {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        if (toIndex > list.size())
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        if (fromIndex > toIndex)
            throw new IllegalArgumentException("fromIndex(" + fromIndex
                    + ") > toIndex(" + toIndex + ")");
        ArrayList<T> subList = new ArrayList<T>();
        for (int i = fromIndex; i <= toIndex; i++) {
            subList.add(list.get(i));
        }
        return subList;
    }

}
