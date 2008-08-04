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
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 *
 */
public class FreemarkerResource extends Resource {

    // List of items
    private final List<String> items;

    public FreemarkerResource(Context context, Request request,
            Response response) {
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
        Representation representation = null;

        try {
            // Parent's application
            final DynamicApplication application = (DynamicApplication) getApplication();

            final Map<String, Object> map = new TreeMap<String, Object>();
            map.put("items", this.items);
            representation = new TemplateRepresentation("items.ftl",
                    application.getFmc(), map, MediaType.TEXT_PLAIN);
        } catch (final Exception e) {
            System.out.println(e.getMessage());
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
        }

        return representation;
    }
}
