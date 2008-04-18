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

public class FreemarkerResource extends Resource {

    // List of items
    private List<String> items;

    public FreemarkerResource(Context context, Request request,
            Response response) {
        super(context, request, response);
        // This resource is able to generate one kind of representations, then
        // turn off content negotiation.
        this.setNegotiateContent(false);
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
            DynamicApplication application = (DynamicApplication) getApplication();

            Map<String, Object> map = new TreeMap<String, Object>();
            map.put("items", items);
            representation = new TemplateRepresentation("items.ftl",
                    application.getFmc(), map, MediaType.TEXT_PLAIN);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e);
        }

        return representation;
    }
}
