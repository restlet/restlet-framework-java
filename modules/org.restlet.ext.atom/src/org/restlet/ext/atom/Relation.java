/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.ext.atom;

/**
 * Enumeration of relation types.
 * 
 * @author Jerome Louvel
 */
public enum Relation {
    /**
     * Signifies that the IRI in the value of the href attribute identifies an
     * alternate version of the resource described by the containing element.
     */
    ALTERNATE,

    /**
     * Signifies that the IRI in the value of the href attribute identifies a
     * resource that is able to edit the current resource.
     */
    EDIT,

    /**
     * Signifies that the IRI in the value of the href attribute identifies a
     * related resource that is potentially large in size and might require
     * special handling. For atom:link elements with rel="enclosure", the length
     * attribute SHOULD be provided.
     */
    ENCLOSURE,

    /**
     * Signifies that the IRI in the value of the href attribute identifies the
     * first resource in a series including the current resource.
     */
    FIRST,

    /**
     * Signifies that the IRI in the value of the href attribute identifies the
     * last resource in a series including the current resource.
     */
    LAST,

    /**
     * Signifies that the IRI in the value of the href attribute identifies the
     * next resource in a series including the current resource.
     */
    NEXT,

    /**
     * Signifies that the IRI in the value of the href attribute identifies the
     * previous resource in a series including the current resource.
     */
    PREVIOUS,

    /**
     * Signifies that the IRI in the value of the href attribute identifies a
     * resource related to the resource described by the containing element.
     */
    RELATED,

    /**
     * Signifies that the IRI in the value of the href attribute identifies a
     * resource equivalent to the containing element.
     */
    SELF,

    /**
     * Signifies that the IRI in the value of the href attribute identifies a
     * resource that is the source of the information provided in the containing
     * element.
     */
    VIA;

    /**
     * Parses a relation name into the equivalent enumeration item.
     * 
     * @param rel
     *            The relation name to parse.
     * @return The equivalent enumeration item.
     */
    public static Relation parse(String rel) {
        Relation result = ALTERNATE;

        if (rel != null) {
            if (rel.equalsIgnoreCase("alternate")) {
                result = ALTERNATE;
            } else if (rel.equalsIgnoreCase("edit")) {
                result = EDIT;
            } else if (rel.equalsIgnoreCase("enclosure")) {
                result = ENCLOSURE;
            } else if (rel.equalsIgnoreCase("first")) {
                result = FIRST;
            } else if (rel.equalsIgnoreCase("last")) {
                result = LAST;
            } else if (rel.equalsIgnoreCase("next")) {
                result = NEXT;
            } else if (rel.equalsIgnoreCase("previous")) {
                result = PREVIOUS;
            } else if (rel.equalsIgnoreCase("related")) {
                result = RELATED;
            } else if (rel.equalsIgnoreCase("self")) {
                result = SELF;
            } else if (rel.equalsIgnoreCase("via")) {
                result = VIA;
            }
        }

        return result;
    }

    /**
     * Return a String object representing the specified Relation.
     * 
     * @param rel
     *            The relation to be converted.
     * @return The String representation of the argument.
     */
    public static String toString(Relation rel) {
        String result = "alternate";

        if (rel != null) {
            if (rel.equals(ALTERNATE)) {
                result = "alternate";
            } else if (rel.equals(EDIT)) {
                result = "edit";
            } else if (rel.equals(ENCLOSURE)) {
                result = "enclosure";
            } else if (rel.equals(FIRST)) {
                result = "first";
            } else if (rel.equals(LAST)) {
                result = "last";
            } else if (rel.equals(NEXT)) {
                result = "next";
            } else if (rel.equals(PREVIOUS)) {
                result = "previous";
            } else if (rel.equals(RELATED)) {
                result = "related";
            } else if (rel.equals(SELF)) {
                result = "self";
            } else if (rel.equals(VIA)) {
                result = "via";
            }
        }

        return result;
    }
}
