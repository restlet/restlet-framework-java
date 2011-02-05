/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.example.book.rest.ch3;

import java.util.ArrayList;
import java.util.List;

import org.restlet.Response;
import org.restlet.ext.xml.DomRepresentation;
import org.w3c.dom.Node;

/**
 * Amazon S3 client application. Returns a list of buckets.
 * 
 * @author Jerome Louvel
 */
public class S3App extends S3Authorized {

    public static void main(String... args) {
        for (S3Bucket bucket : new S3App().getBuckets()) {
            System.out.println(bucket.getName() + " : " + bucket.getUri());
        }
    }

    public List<S3Bucket> getBuckets() {
        List<S3Bucket> result = new ArrayList<S3Bucket>();

        // Fetch a resource: an XML document with our list of buckets
        Response response = authorizedGet(HOST);
        DomRepresentation document = new DomRepresentation(response.getEntity());

        if (response.getStatus().isSuccess()) {
            // Use XPath to find the bucket names
            for (Node node : document.getNodes("//Bucket/Name")) {
                result.add(new S3Bucket(node.getTextContent()));
            }
        } else {
            System.out.println("Unable to access to your S3 buckets : "
                    + response.getStatus());
        }

        return result;
    }
}
