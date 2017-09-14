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

package org.restlet.test.ext.jaxrs;

import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;

import javax.ws.rs.core.Application;

import org.json.JSONObject;
import org.restlet.Component;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.ext.jackson.JacksonConverter;
import org.restlet.ext.jaxrs.JaxRsApplication;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.test.engine.connector.BaseConnectorsTestCase;

/**
 * @author Stephan Koops
 * @see JSONObject
 */
public class QueryTestCase extends BaseConnectorsTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Explicitly promote the Jackson converter
        Engine.getInstance().getRegisteredConverters()
                .add(0, new JacksonConverter());
        Engine.setLogLevel(Level.FINE);
    }

    @Override
    protected void call(String uri) throws Exception {
        Representation entity = new StringRepresentation("hi",
                MediaType.APPLICATION_JSON);
        ClientResource cr = new ClientResource(uri);
        Representation responseEntity = cr.post(entity);
        assertEquals(Status.SUCCESS_OK, cr.getStatus());
        assertEquals("hi", responseEntity.getText());

        entity = new StringRepresentation("hi", MediaType.APPLICATION_XML);

        try {
            cr.post(entity);
        } catch (ResourceException re) {
            assertEquals(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
                    re.getStatus());
        } finally {
            cr.release();
        }
    }

    @Override
    protected org.restlet.Application createApplication(Component component) {
        return new JaxRsApplication(new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(QueryResource.class);
            }
        });
    }
}
