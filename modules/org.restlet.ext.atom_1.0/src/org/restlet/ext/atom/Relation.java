/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.atom;

/**
 * Enumeration of relation types.
 * 
 * @author Jerome Louvel (contact@noelios.com)
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
