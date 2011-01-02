package org.restlet.example.book.restlet.ch08.sec5.website;

import java.util.Map;
import java.util.TreeMap;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.service.StatusService;

public class MailStatusService extends StatusService {

    @Override
    public Representation getRepresentation(Status status, Request request,
            Response response) {

        // Create the data model
        Map<String, String> dataModel = new TreeMap<String, String>();
        dataModel.put("applicationName", Application.getCurrent().getName());
        dataModel.put("statusName", response.getStatus().getName());
        dataModel.put("statusDescription", response.getStatus()
                .getDescription());

        // Load the FreeMarker template
        Representation mailFtl = new ClientResource(
                LocalReference.createClapReference(getClass().getPackage())
                        + "/MailStatus.ftl").get();

        // Wraps the bean with a FreeMarker representation
        return new TemplateRepresentation(mailFtl, dataModel,
                MediaType.TEXT_HTML);
    }

}
