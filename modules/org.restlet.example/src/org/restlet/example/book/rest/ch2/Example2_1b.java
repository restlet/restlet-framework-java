/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.book.rest.ch2;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;
import org.w3c.dom.Node;

/**
 * Searching the web with Yahoo!'s web service using XML. This version is
 * namespace aware.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Example2_1b {
    static final String BASE_URI = "http://api.search.yahoo.com/WebSearchService/V1/webSearch";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("You need to pass a term to search");
        } else {
            // Fetch a resource: an XML document full of search results
            final String term = Reference.encode(args[0]);
            final String uri = BASE_URI + "?appid=restbook&query=" + term;
            final Response response = new Client(Protocol.HTTP).get(uri);
            final DomRepresentation document = response.getEntityAsDom();

            // Associate the namespace with the prefix y
            document.setNamespaceAware(true);
            document.putNamespace("y", "urn:yahoo:srch");

            // Use XPath to find the interesting parts of the data structure
            final String expr = "/y:ResultSet/y:Result/y:Title/text()";
            for (final Node node : document.getNodes(expr)) {
                System.out.println(node.getTextContent());
            }
        }
    }
}
