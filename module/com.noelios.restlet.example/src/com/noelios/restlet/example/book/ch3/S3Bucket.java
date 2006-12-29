/*
 * Copyright 2005-2006 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.example.book.ch3;

import java.util.ArrayList;
import java.util.List;

import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.SaxRepresentation;
import org.w3c.dom.Node;

/**
 * Amazon S3 bucket.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class S3Bucket {

    private String name;

    public S3Bucket(String name) {
        this.name = name;
    }

    /**
     * Stores this bucket on S3. Analagous to ActiveRecord::Base#save, which
     * stores an object in the database.
     */
    public void save() {

    }

    /**
     * Deletes this bucket. Fails with status code ("Conflict") unless the
     * bucket is empty.
     */
    public void delete() {

    }

    /**
     * Get the objects in this bucket: all of them, or some subset.
     * 
     * If S3 decides not to return the whole bucket/subset, the second return
     * value will be set to true. To get the rest of the objects, you'll need to
     * manipulate the subset options.
     * 
     * Subset options are :Prefix, :Marker, :Delimiter, :MaxKeys. For details,
     * see the S3 docs on "Listing Keys".
     * 
     * @return The objects in this nucket.
     */
    public List<S3Object> getObjects() {
        List<S3Object> result = new ArrayList<S3Object>();

        StringBuilder uri = new StringBuilder().append(getUri());
        String suffix = "?";

        // options.each do |param, value|
        // if GET_OPTIONS.member? :param
        // uri << suffix << param.to_s << '=' << value
        // suffix = '&'
        // end
        // end

        // there_are_more = REXML::XPath.first(doc, "//IsTruncated").text ==
        // "true"
        // objects = []
        // REXML::XPath.each(doc, "//Contents/Key") do |e|
        // objects << Object.new(self, e.text) if e.text
        // end
        // return objects, there_are_more
        // end
        // GET_OPTIONS = [:Prefix, :Marker, :Delimiter, :MaxKeys]

        // Create a authenticated request
        Request request = new Request(Method.GET, uri.toString());
        request.setChallengeResponse(new ChallengeResponse(
                ChallengeScheme.HTTP_AWS, S3App.PUBLIC_KEY, S3App.PRIVATE_KEY));

        // Make the request and parse the document.
        Response response = new Client(Protocol.HTTPS).handle(request);
        DomRepresentation document = response.getEntityAsDom();
        for (Node node : document.getNodes("//Bucket/Name")) {
            // result.add(new S3Bucket(node.getTextContent()));
        }

        return result;
    }

    public String getUri() {
        return S3App.HOST + getName();
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
