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

package org.restlet.ext.atom;

import org.restlet.engine.util.SystemUtils;

/**
 * Enumeration of relation types.
 * 
 * @author Jerome Louvel
 * @see <a
 *      href="http://www.iana.org/assignments/link-relations/link-relations.xhtml">IANA
 *      Atom relations</a>
 */
public class Relation {
    /**
     * Signifies that the IRI in the value of the href attribute identifies an
     * alternate version of the resource described by the containing element.
     */
    public static final Relation ALTERNATE = new Relation("alternate");

    /**
     * Signifies that the IRI returns a feed document containing the most recent
     * entries in the feed.
     */
    public static final Relation CURRENT = new Relation("current");

    /**
     * Relationship that asserts that resource B provides a description of
     * resource A. There are no constraints on the format or representation of
     * either A or B, neither are there any further constraints on either
     * resource.
     */
    public static final Relation DESCRIBED_BY = new Relation("describedby");

    /**
     * Signifies that the IRI in the value of the href attribute identifies a
     * resource that is able to edit the current resource.
     */
    public static final Relation EDIT = new Relation("edit");

    /**
     * An IRI of an editable Media Resource. When appearing within an
     * atom:entry, the href IRI can be used to retrieve, update and delete the
     * Media Resource associated with that Entry.
     */
    public static final Relation EDIT_MEDIA = new Relation("edit-media");

    /**
     * Signifies that the IRI in the value of the href attribute identifies a
     * related resource that is potentially large in size and might require
     * special handling. For atom:link elements with rel="enclosure", the length
     * attribute SHOULD be provided.
     */
    public static final Relation ENCLOSURE = new Relation("enclosure");

    /**
     * Signifies that the IRI in the value of the href attribute identifies the
     * first resource in a series including the current resource.
     */
    public static final Relation FIRST = new Relation("first");

    /**
     * A URI for a hub that enables registration for real-time updates to the
     * resource.
     */
    public static final Relation HUB = new Relation("hub");

    /**
     * Signifies that the IRI in the value of the href attribute identifies the
     * last resource in a series including the current resource.
     */
    public static final Relation LAST = new Relation("last");

    /**
     * When included on a versioned resource, this link points to a resource
     * containing the latest (e.g., current) version. The latest version is
     * defined by the system. For linear versioning systems, this is probably
     * the latest version by timestamp. For systems that support branching,
     * there will be multiple latest versions, one for each branch in the
     * version history. Some systems may allow multiple of these link relations.
     */
    public static final Relation LAST_VERSION = new Relation("latest-version");

    /**
     * Signifies that the IRI in the value of the href attribute identifies the
     * resource describing the license.
     */
    public static final Relation LICENSE = new Relation("license");

    /**
     * Signifies that the IRI in the value of the href attribute identifies the
     * next resource in a series including the current resource.
     */
    public static final Relation NEXT = new Relation("next");

    /**
     * Signifies that the IRI in the value of the href attribute identifies the
     * immediately following archive document.
     */
    public static final Relation NEXT_ARCHIVE = new Relation("next-archive");

    /**
     * Signifies that the IRI in the value of the href attribute identifies
     * where payment is accepted.
     */
    public static final Relation PAYMENT = new Relation("payment");

    /**
     * When included on a versioned resource, this link points to a resource
     * containing the predecessor version in the version history. Some systems
     * may allow multiple of these link relations in the case of a multiple
     * branches merging.
     */
    public static final Relation PREDECESSOR_VERSION = new Relation(
            "predecessor-version");

    /**
     * Signifies that the IRI in the value of the href attribute identifies the
     * previous resource in a series including the current resource.
     */
    public static final Relation PREVIOUS = new Relation("previous");

    /**
     * A URI that refers to the immediately preceding archive document.
     */
    public static final Relation PREVIOUS_ARCHIVE = new Relation("prev-archive");

    /**
     * Signifies that the IRI in the value of the href attribute identifies a
     * resource related to the resource described by the containing element.
     */
    public static final Relation RELATED = new Relation("related");

    /**
     * Signifies that the IRI in the value of the href attribute identifies a
     * resource where responses to an entry may be found.
     */
    public static final Relation REPLIES = new Relation("replies");

    /**
     * Signifies that the IRI in the value of the href attribute identifies a
     * resource equivalent to the containing element.
     */
    public static final Relation SELF = new Relation("self");

    /**
     * Signifies that the IRI in the value of the href attribute identifies a
     * resource that can be used to retrieve an Atom Publishing Protocol Service
     * Document as defined by RFC 5023.
     */
    public static final Relation SERVICE = new Relation("service");

    /**
     * When included on a versioned resource, this link points to a resource
     * containing the successor version in the version history. Some systems may
     * allow multiple of these link relations in order to support branching.
     */
    public static final Relation SUCCESSOR_VERSION = new Relation(
            "successor-version");

    /**
     * A URI that refers to a parent document in a hierarchy of documents.
     */
    public static final Relation UP = new Relation("up");

    /**
     * When included on a versioned resource, this link points to a resource
     * containing the version history for this resource.
     */
    public static final Relation VERSION_HISTORY = new Relation(
            "version-history");

    /**
     * Signifies that the IRI in the value of the href attribute identifies a
     * resource that is the source of the information provided in the containing
     * element.
     */
    public static final Relation VIA = new Relation("via");

    /**
     * When included on a versioned resource, this link points to a working copy
     * for this resource. Some systems may allow multiple of these link
     * relations.
     */
    public static final Relation WORKING_COPY = new Relation("working-copy");

    /**
     * When included on a working copy, this link points to the versioned
     * resource from which this working copy was obtained.
     */
    public static final Relation WORKING_COPY_OF = new Relation(
            "working-copy-of");

    /**
     * Return a String object representing the specified Relation.
     * 
     * @param rel
     *            The relation to be converted.
     * @return The String representation of the argument.
     */
    public static String toString(Relation rel) {
        return rel.name;
    }

    /**
     * Parses a relation name into the equivalent item.
     * 
     * @param rel
     *            The relation name to parse.
     * @return The equivalent item.
     */
    public static Relation valueOf(String rel) {
        Relation result = ALTERNATE;

        if (rel != null) {
            if (rel.equalsIgnoreCase("alternate")) {
                result = ALTERNATE;
            } else if (rel.equalsIgnoreCase("current")) {
                result = CURRENT;
            } else if (rel.equalsIgnoreCase("describedby")) {
                result = DESCRIBED_BY;
            } else if (rel.equalsIgnoreCase("edit")) {
                result = EDIT;
            } else if (rel.equalsIgnoreCase("edit-media")) {
                result = EDIT_MEDIA;
            } else if (rel.equalsIgnoreCase("enclosure")) {
                result = ENCLOSURE;
            } else if (rel.equalsIgnoreCase("first")) {
                result = FIRST;
            } else if (rel.equalsIgnoreCase("hub")) {
                result = HUB;
            } else if (rel.equalsIgnoreCase("last")) {
                result = LAST;
            } else if (rel.equalsIgnoreCase("latest-version")) {
                result = LAST_VERSION;
            } else if (rel.equalsIgnoreCase("license")) {
                result = LICENSE;
            } else if (rel.equalsIgnoreCase("next")) {
                result = NEXT;
            } else if (rel.equalsIgnoreCase("next-archive")) {
                result = NEXT_ARCHIVE;
            } else if (rel.equalsIgnoreCase("payment")) {
                result = PAYMENT;
            } else if (rel.equalsIgnoreCase("predecessor-version")) {
                result = PREDECESSOR_VERSION;
            } else if (rel.equalsIgnoreCase("previous")) {
                result = PREVIOUS;
            } else if (rel.equalsIgnoreCase("prev-archive")) {
                result = PREVIOUS_ARCHIVE;
            } else if (rel.equalsIgnoreCase("related")) {
                result = RELATED;
            } else if (rel.equalsIgnoreCase("replies")) {
                result = REPLIES;
            } else if (rel.equalsIgnoreCase("self")) {
                result = SELF;
            } else if (rel.equalsIgnoreCase("service")) {
                result = SERVICE;
            } else if (rel.equalsIgnoreCase("successor-version")) {
                result = SUCCESSOR_VERSION;
            } else if (rel.equalsIgnoreCase("up")) {
                result = UP;
            } else if (rel.equalsIgnoreCase("version-history")) {
                result = VERSION_HISTORY;
            } else if (rel.equalsIgnoreCase("via")) {
                result = VIA;
            } else if (rel.equalsIgnoreCase("working-copy")) {
                result = WORKING_COPY;
            } else if (rel.equalsIgnoreCase("working-copy-of")) {
                result = WORKING_COPY_OF;
            } else {
                result = new Relation(rel);
            }
        }

        return result;
    }

    /** The name of the relation. */
    private String name;

    /**
     * Constructor.
     * 
     * @param name
     *            The name of the relation.
     */
    public Relation(String name) {
        super();
        this.name = name;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object object) {
        return (object instanceof Relation)
                && name.equalsIgnoreCase(((Relation) object).getName());
    }

    /**
     * Returns the name of the relation.
     * 
     * @return The name of the relation.
     */
    public String getName() {
        return name;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(getName());
    }

    @Override
    public String toString() {
        return name;
    }

}
