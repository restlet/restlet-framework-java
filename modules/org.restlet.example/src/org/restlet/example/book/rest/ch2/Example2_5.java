/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.example.book.rest.ch2;

import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Getting your list of recent bookmarks on del.icio.us.
 * 
 * @author Jerome Louvel
 */
public class Example2_5 {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err
                    .println("You need to pass your del.icio.us user name and password");
        } else {
            // Create a authenticated request
            ClientResource resource = new ClientResource(
                    "https://api.del.icio.us/v1/posts/recent");
            resource.setChallengeResponse(new ChallengeResponse(
                    ChallengeScheme.HTTP_BASIC, args[0], args[1]));

            // Fetch a resource: an XML document with your recent posts
            Representation entity = resource.get();
            DomRepresentation document = new DomRepresentation(entity);

            // Use XPath to find the interesting parts of the data structure
            for (Node node : document.getNodes("/posts/post")) {
                NamedNodeMap attrs = node.getAttributes();
                String desc = attrs.getNamedItem("description").getNodeValue();
                String href = attrs.getNamedItem("href").getNodeValue();
                System.out.println(desc + ": " + href);
            }
        }
    }
}
