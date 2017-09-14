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

package org.restlet.example.book.restlet.ch10.sec3.client;

import java.util.Set;

import org.restlet.data.Reference;
import org.restlet.example.book.restlet.ch10.sec3.FoafConstants;
import org.restlet.ext.rdf.Couple;
import org.restlet.ext.rdf.Literal;
import org.restlet.ext.rdf.RdfClientResource;

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

        if (literals != null) {
            for (Couple<Reference, Literal> literal : literals) {
                System.out.println(literal.getFirst().getLastSegment() + ": "
                        + literal.getSecond());
            }
        }

        System.out.println("--------------------------------------------");

        if (maxDepth > 0) {
            Set<RdfClientResource> knows = foafProfile
                    .getLinked(FoafConstants.KNOWS);

            if (knows != null) {
                for (RdfClientResource know : knows) {
                    displayFoafProfile(know, maxDepth - 1);
                }
            }
        }
    }
}
