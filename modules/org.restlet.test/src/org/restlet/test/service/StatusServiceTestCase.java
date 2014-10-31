/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
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

package org.restlet.test.service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.ConnegService;
import org.restlet.service.ConverterService;
import org.restlet.service.MetadataService;
import org.restlet.service.StatusService;
import org.restlet.test.RestletTestCase;
import org.restlet.test.resource.MyException01;
import org.restlet.test.resource.MyException02;

/**
 * Unit tests for the status service.
 * 
 * @author Jerome Louvel
 */
public class StatusServiceTestCase extends RestletTestCase {

    public void testAnnotation() {
        StatusService ss = new StatusService();
        Status status = ss.toStatus(new MyException01(new Date()), null, null);
        assertEquals(400, status.getCode());
    }

    public void testRepresentation() throws IOException {
        Status status = new Status(400, new MyException01(new Date()));

        ConverterService converterService = new ConverterService();
        ConnegService connegService = new ConnegService();
        MetadataService metadataService = new MetadataService();
        StatusService ss = new StatusService(true, converterService,
                metadataService, connegService);

        Request request = new Request();
        Representation representation = ss.toRepresentation(status,
                request, new Response(request));

        // verify
        Status expectedStatus = Status.CLIENT_ERROR_BAD_REQUEST;
        HashMap<String, Object> expectedRepresentationMap = new LinkedHashMap<String, Object>();
        expectedRepresentationMap.put("code", expectedStatus.getCode());
        expectedRepresentationMap.put("reasonPhrase",
                expectedStatus.getReasonPhrase());
        expectedRepresentationMap.put("description",
                expectedStatus.getDescription());
        String expectedJsonRepresentation = new JacksonRepresentation<HashMap<String, Object>>(
                expectedRepresentationMap).getText();

        Status.CLIENT_ERROR_BAD_REQUEST.getCode();
        assertEquals(MediaType.APPLICATION_JSON, representation.getMediaType());
        assertEquals(expectedJsonRepresentation, representation.getText());
    }

    public void testSerializedException()
            throws IOException {
        Status status = new Status(400, new MyException02("test message"));

        ConverterService converterService = new ConverterService();
        ConnegService connegService = new ConnegService();
        MetadataService metadataService = new MetadataService();
        StatusService ss = new StatusService(true, converterService,
                metadataService, connegService);

        Request request = new Request();
        Representation representation = ss.toRepresentation(status,
                request, new Response(request));

        // verify
        HashMap<String, Object> expectedRepresentationMap = new LinkedHashMap<String, Object>();
        expectedRepresentationMap.put("customProperty", "test message");
        String expectedJsonRepresentation = new JacksonRepresentation<HashMap<String, Object>>(
                expectedRepresentationMap).getText();

        Status.CLIENT_ERROR_BAD_REQUEST.getCode();
        assertEquals(MediaType.APPLICATION_JSON, representation.getMediaType());
        assertEquals(expectedJsonRepresentation, representation.getText());
    }

}
