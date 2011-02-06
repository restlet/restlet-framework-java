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
