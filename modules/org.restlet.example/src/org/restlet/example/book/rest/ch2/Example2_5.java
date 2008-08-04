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
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Getting your list of recent bookmarks on del.icio.us.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Example2_5 {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err
                    .println("You need to pass your del.icio.us user name and password");
        } else {
            // Create a authenticated request
            Request request = new Request(Method.GET,
                    "https://api.del.icio.us/v1/posts/recent");
            request.setChallengeResponse(new ChallengeResponse(
                    ChallengeScheme.HTTP_BASIC, args[0], args[1]));

            // Fetch a resource: an XML document with your recent posts
            Response response = new Client(Protocol.HTTPS).handle(request);
            DomRepresentation document = response.getEntityAsDom();

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
