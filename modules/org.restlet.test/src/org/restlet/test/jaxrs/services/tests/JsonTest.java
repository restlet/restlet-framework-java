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
package org.restlet.test.jaxrs.services.tests;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.ApplicationConfig;

import junit.framework.AssertionFailedError;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.ext.jaxrs.internal.provider.JsonProvider;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.test.jaxrs.services.others.Person;
import org.restlet.test.jaxrs.services.resources.JsonTestService;

/**
 * @author Stephan Koops
 * @see JsonTestService
 * @see JsonProvider
 * @see JSONObject
 */
public class JsonTest extends JaxRsTestCase {

    public static void main(String[] args) throws Exception {
        new JsonTest().runServerUntilKeyPressed();
    }

    /**
     * @param response
     * @throws JSONException
     * @throws IOException
     */
    private void checkJsonResponse(Response response) throws JSONException,
            IOException {
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final JSONObject jsonObject = new JSONObject(response.getEntity()
                .getText());
        assertEquals("Angela", jsonObject.get("firstname"));
        assertEquals("Merkel", jsonObject.get("lastname"));
    }

    @Override
    protected ApplicationConfig getAppConfig() {
        final ApplicationConfig appConfig = new ApplicationConfig() {
            @SuppressWarnings("all")
            public Map<String, javax.ws.rs.core.MediaType> getExtensionMappings() {
                return getMediaTypeMappings();
            }

            @SuppressWarnings("all")
            public Map<String, javax.ws.rs.core.MediaType> getMediaTypeMappings() {
                final Map<String, javax.ws.rs.core.MediaType> mediaMap = new HashMap<String, javax.ws.rs.core.MediaType>();
                mediaMap.put("xml", APPLICATION_XML_TYPE);
                mediaMap.put("json", APPLICATION_JSON_TYPE);
                return mediaMap;
            }

            @Override
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getResourceClasses() {
                return (Set) Collections.singleton(getRootResourceClass());
            }
        };
        return appConfig;
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return JsonTestService.class;
    }

    public void testGetJsonObject() throws Exception {
        final Response response = get("JSONObject");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final String entity = response.getEntity().getText();
        try {
            assertEquals("{\"name1\":\"value1\",\"name2\":\"value2\"}", entity);
        } catch (final AssertionFailedError afe) {
            assertEquals("{\"name2\":\"value2\",\"name1\":\"value1\"}", entity);
        }
    }

    public void testGetPersonJson() throws Exception {
        Response response = get("person?firstname=Angela&lastname=Merkel",
                MediaType.APPLICATION_JSON);
        checkJsonResponse(response);

        response = get("person.json?firstname=Angela&lastname=Merkel",
                MediaType.TEXT_XML);
        checkJsonResponse(response);

        response = get("person.json?firstname=Angela&lastname=Merkel",
                MediaType.IMAGE_GIF);
        checkJsonResponse(response);
    }

    /**
     * This test using JAXB, but shows that you can serialize Objects by JAXb
     * and by JSON.
     * 
     * @param xmlMediaType
     * @throws IOException
     */
    private void testGetPersonXml(MediaType xmlMediaType) throws IOException {
        final Response response = get(
                "person?firstname=Angela&lastname=Merkel", xmlMediaType);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final JaxbRepresentation<Person> jaxbReprs = new JaxbRepresentation<Person>(
                response.getEntity(), Person.class);
        final Person person = jaxbReprs.getObject();
        assertEquals("Angela", person.getFirstname());
        assertEquals("Merkel", person.getLastname());
    }

    public void testGetPersonXmlA() throws Exception {
        testGetPersonXml(MediaType.APPLICATION_XML);
    }

    public void testGetPersonXmlT() throws Exception {
        testGetPersonXml(MediaType.TEXT_XML);
    }

    public void testPost() throws Exception {
        final Representation entity = new StringRepresentation("{name:value}",
                MediaType.APPLICATION_JSON);
        final Response response = post("JSONObject", entity);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("value", response.getEntity().getText());
    }

    /**
     * @see JsonTestService#getString()
     */
    public void testString() throws Exception {
        final Response response = get("String");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final Representation entity = response.getEntity();
        assertEquals("{name:value}", entity.getText());
    }
}