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

package org.restlet.test.ext.jaxrs.services.tests;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import junit.framework.AssertionFailedError;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.ext.jaxb.JaxbRepresentation;
import org.restlet.ext.jaxrs.JaxRsApplication;
import org.restlet.ext.jaxrs.internal.provider.JsonProvider;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.test.ext.jaxrs.services.others.Person;
import org.restlet.test.ext.jaxrs.services.resources.JsonTestService;

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
        String text = response.getEntity().getText();
        JSONObject jsonObject = new JSONObject(text);
        assertEquals("Angela", jsonObject.get("firstname"));
        assertEquals("Merkel", jsonObject.get("lastname"));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // Explicitly promote the Jackson converter
        Engine.getInstance().getRegisteredConverters()
                .add(0, new JacksonConverter());
    }

    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(JsonTestService.class);
            }
        };
    }

    @Override
    protected void modifyApplication(JaxRsApplication app) {
        app.getTunnelService().setExtensionsTunnel(true);
    }

    public void testGetJsonObject() throws Exception {
        final Response response = get("JSONObject");
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        String entity = response.getEntity().getText();
        try {
            assertEquals("{\"name1\":\"value1\",\"name2\":\"value2\"}", entity);
        } catch (AssertionFailedError afe) {
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
