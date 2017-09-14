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

package org.restlet.engine.util;

import java.io.Serializable;
import java.util.Comparator;

import org.restlet.data.Reference;

/**
 * Allows to sort the list of references set by the resource.
 * 
 * @author Jerome Louvel
 */
public class AlphabeticalComparator implements Comparator<Reference>,
        Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Compares two references.
     * 
     * @param ref0
     *            The first reference.
     * @param ref1
     *            The second reference.
     * @return The comparison result.
     * @see Comparator
     */
    public int compare(Reference ref0, Reference ref1) {
        final boolean bRep0Null = (ref0.getIdentifier() == null);
        final boolean bRep1Null = (ref1.getIdentifier() == null);

        if (bRep0Null && bRep1Null) {
            return 0;
        }
        if (bRep0Null) {
            return -1;
        }
        if (bRep1Null) {
            return 1;
        }
        return compare(ref0.toString(false, false), ref1.toString(false, false));
    }

    /**
     * Compares two strings.
     * 
     * @param str0
     *            The first string.
     * @param str1
     *            The second string.
     * @return The comparison result.
     * @see Comparator
     */
    public int compare(final String str0, final String str1) {
        return str0.compareTo(str1);
    }
}
