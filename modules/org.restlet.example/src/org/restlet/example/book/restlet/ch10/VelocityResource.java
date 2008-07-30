/*
 * Copyright 2005-2008 Noelios Consulting.
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

package org.restlet.example.book.restlet.ch10;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.velocity.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 *
 */
public class VelocityResource extends Resource {

    // List of items
    private final List<String> items;

    public VelocityResource(Context context, Request request, Response response) {
        super(context, request, response);
        // This resource is able to generate one kind of representations, then
        // turn off content negotiation.
        setNegotiateContent(false);
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));

        // Collect data
        this.items = new ArrayList<String>();
        this.items.add("item 1");
        this.items.add("item 2");
        this.items.add("item 3");
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        TemplateRepresentation representation = null;

        try {
            final Map<String, Object> map = new TreeMap<String, Object>();
            map.put("items", this.items);
            representation = new TemplateRepresentation("items.vtl", map,
                    MediaType.TEXT_PLAIN);
            representation
                    .getEngine()
                    .addProperty(
                            "file.resource.loader.path",
                            "D:\\alaska\\forge\\build\\swc\\nre\\trunk\\books\\apress\\manuscript\\sample\\");
        } catch (final Exception e) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
        }

        return representation;
    }
}
