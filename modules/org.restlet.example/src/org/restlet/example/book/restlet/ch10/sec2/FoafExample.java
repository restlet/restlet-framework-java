package org.restlet.example.book.restlet.ch10.sec2;

import java.io.IOException;

import org.restlet.data.Reference;
import org.restlet.ext.rdf.Graph;
import org.restlet.ext.rdf.Literal;

public class FoafExample {

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        // FOAF ontology
        String FOAF_BASE = "http://xmlns.com/foaf/0.1/";
        Reference firstName = new Reference(FOAF_BASE + "firstName");
        Reference lastName = new Reference(FOAF_BASE + "lastName");
        Reference mbox = new Reference(FOAF_BASE + "mbox");
        Reference knows = new Reference(FOAF_BASE + "knows");

        // Linked Simpson resources
        Reference homerRef = new Reference(
        "http://www.rmep.org/accounts/chunkylover53/");
        Reference margeRef = new Reference(
        "http://www.rmep.org/accounts/bretzels34/");
        Reference bartRef = new Reference(
        "http://www.rmep.org/accounts/jojo10/");
        Reference lisaRef = new Reference(
        "http://www.rmep.org/accounts/lisa1984/");
        
        // Example RDF graph
        Graph example = new Graph();
        example.add(homerRef, firstName, new Literal("Homer"));
        example.add(homerRef, lastName, new Literal("Simpson"));
        example.add(homerRef, mbox, new Literal("mailto:homer@simpson.org"));
        example.add(homerRef, knows, margeRef);
        example.add(homerRef, knows, bartRef);
        example.add(homerRef, knows, lisaRef);

        // Serialization
        System.out.println("\nRDF/XML format:\n");
        example.getRdfXmlRepresentation().write(System.out);

        System.out.println("\nRDF/n3 format:\n");
        example.getRdfN3Representation().write(System.out);

        System.out.println("\nRDF/Turtle format:\n");
        example.getRdfTurtleRepresentation().write(System.out);

        System.out.println("\nRDF/NTriples format:\n");
        example.getRdfNTriplesRepresentation().write(System.out);
    }
}
