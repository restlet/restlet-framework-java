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
