package org.restlet.example.book.restlet.misc;

import java.util.HashMap;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class TemplateResource extends ServerResource {

    @Get
    public Representation get() {

        // Creates the template as aStringRepresentation
        Representation template = new StringRepresentation("hello, ${who}");

        // Sets up the data model
        Map<String, String> dataModel = new HashMap<String, String>();
        dataModel.put("who", "world");

        // Generates the templated representation with the template and the data
        // model.
        return new TemplateRepresentation(template, null, dataModel,
                MediaType.TEXT_PLAIN);
    }
}
