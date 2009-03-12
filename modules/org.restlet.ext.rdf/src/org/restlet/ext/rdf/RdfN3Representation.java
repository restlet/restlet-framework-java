/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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
import org.restlet.representation.StringRepresentation;

public class RdfN3Representation extends RdfRepresentation {

    /** Common predicate for the "implies" predicate. */
    public static Reference PREDICATE_IMPLIES = new Reference(
            "http://www.w3.org/2000/10/swap/log#implies");

    /** Common predicate for the "same as" predicate. */
    public static Reference PREDICATE_SAME = new Reference(
            "http://www.w3.org/2002/07/owl#sameAs");

    /** Common predicate for the "is a" predicate. */
    public static Reference PREDICATE_TYPE = new Reference(
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");

    public static void main(String[] args) throws IOException {
        StringRepresentation rep = new StringRepresentation(
                "@base    <tru   c>.\n" + "#Directive base.\n"
                        + "@prefix machin <http://www . \nexample .com>.\n\n"
                        + "@keywords toto tutu titi.");
        /*
         * 
         * + " language _:toto <http://rdf.com>. " +
         * "machin <http://rdf.com> \"chaine\"." +
         * "truc <http://www.multiligne.com> \"\"\"cha\nine\"\"\"");
         */
        RdfN3Representation n3Rep = new RdfN3Representation(rep, new Graph());

    }

    public RdfN3Representation(Representation rdfRepresentation, Graph linkSet)
            throws IOException {
        super(rdfRepresentation, linkSet);
        new RdfN3ContentHandler(linkSet, rdfRepresentation);
    }
}
