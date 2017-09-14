/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.example.book.restlet.ch07.sec1.sub4;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        Map<String, String> dataModel = new ConcurrentHashMap<String, String>();
        dataModel.put("applicationName", Application.getCurrent().getName());
        dataModel.put("statusDescription", response.getStatus()
                .getDescription());
        dataModel.put("statusReasonPhrase", response.getStatus()
                .getReasonPhrase());

        // Load the FreeMarker template
        Representation mailFtl = new ClientResource(
                LocalReference.createClapReference(getClass().getPackage())
                        + "/MailStatus.ftl").get();

        // Wraps the bean with a FreeMarker representation
        return new TemplateRepresentation(mailFtl, dataModel,
                MediaType.TEXT_HTML);
    }

}
