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

package org.restlet.ext.rdf;

import java.io.IOException;

import org.restlet.data.Reference;
import org.restlet.ext.rdf.internal.RdfN3ContentHandler;
import org.restlet.representation.Representation;

public class RdfN3Representation extends RdfRepresentation {

    /** Predicate "implies" . */
    public static Reference PREDICATE_IMPLIES = new Reference(
            "http://www.w3.org/2000/10/swap/log#implies");

    /** Predicate "same as". */
    public static Reference PREDICATE_SAME = new Reference(
            "http://www.w3.org/2002/07/owl#sameAs");

    /** Predicate "is as". */
    public static Reference PREDICATE_TYPE = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

    /** Object "nil". */
    public static Reference OBJECT_NIL = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#nil");

    /** Rdf syntax. */
    public static Reference RDF_SYNTAX = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

    /** Rdf schema. */
    public static Reference RDF_SCHEMA = new Reference(
            "http://www.w3.org/2000/01/rdf-schema#");

    public RdfN3Representation(Representation rdfRepresentation, Graph linkSet)
            throws IOException {
        super(rdfRepresentation, linkSet);
        new RdfN3ContentHandler(linkSet, rdfRepresentation);
    }
}
