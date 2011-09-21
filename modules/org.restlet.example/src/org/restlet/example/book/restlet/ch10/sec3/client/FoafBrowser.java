package org.restlet.example.book.restlet.ch10.sec3.client;

import java.util.Set;

import org.restlet.data.Reference;
import org.restlet.example.book.restlet.ch10.sec3.FoafConstants;
import org.restlet.ext.rdf.Literal;
import org.restlet.ext.rdf.RdfClientResource;
import org.restlet.util.Couple;

public class FoafBrowser {

    public static void main(String[] args) {
        displayFoafProfile("http://localhost:8111/accounts/chunkylover53/");
    }

    public static void displayFoafProfile(String uri) {
        displayFoafProfile(new RdfClientResource(uri), 1);
    }

    public static void displayFoafProfile(RdfClientResource foafProfile,
            int maxDepth) {
        Set<Couple<Reference, Literal>> literals = foafProfile.getLiterals();

        for (Couple<Reference, Literal> literal : literals) {
            System.out.println(literal.getFirst().getLastSegment() + ": "
                    + literal.getSecond());
        }

        System.out.println("--------------------------------------------");

        if (maxDepth > 0) {
            Set<RdfClientResource> knows = foafProfile
                    .getLinked(FoafConstants.KNOWS);

            for (RdfClientResource know : knows) {
                displayFoafProfile(know, maxDepth - 1);
            }
        }
    }
}
