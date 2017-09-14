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

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.StringRepresentation;
import org.restlet.test.ext.jaxrs.services.providers.BooleanEntityProvider;
import org.restlet.test.ext.jaxrs.services.providers.CharacterEntityProvider;
import org.restlet.test.ext.jaxrs.services.providers.IntegerEntityProvider;
import org.restlet.test.ext.jaxrs.services.resources.PrimitiveWrapperEntityResource;
import org.restlet.test.ext.jaxrs.util.TestUtils;

/**
 * @author Stephan Koops
 * @see PrimitiveWrapperEntityResource
 */
public class PrimitiveWrapperEntityTest extends JaxRsTestCase {

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected Application getApplication() {
        final Application appConfig = new Application() {
            @Override
            public Set<Object> getSingletons() {
                return (Set) TestUtils.createSet(new IntegerEntityProvider(),
                        new CharacterEntityProvider(),
                        new BooleanEntityProvider());
            }

            @Override
            public Set<Class<?>> getClasses() {
                return (Set) Collections
                        .singleton(PrimitiveWrapperEntityResource.class);
            }
        };
        return appConfig;
    }

    public void test1() throws Exception {
        final Response response = put("intReturnInt", new StringRepresentation(
                "47"));
        sysOutEntityIfError(response);
        assertEquals(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
                response.getStatus());
    }

    /**
     * @see PrimitiveWrapperEntityResource#charReturnCharacter(char)
     */
    public void test2() throws Exception {
        final Response response = put("charReturnCharacter",
                new StringRepresentation("x"));
        sysOutEntityIfError(response);
        assertEquals(Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
                response.getStatus());
    }

    /**
     * @see PrimitiveWrapperEntityResource#BooleanReturnboolean(Boolean)
     */
    public void test3() throws Exception {
        Response response = put("BooleanReturnboolean",
                new StringRepresentation("true"));
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("true", response.getEntity().getText());

        response = put("BooleanReturnboolean", null);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        if (response.getEntity() != null) {
            assertEquals("false", response.getEntity().getText());
        }
    }

    public void test4() throws Exception {
        Response response = put("integerReturnInteger",
                new StringRepresentation("47"));
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("47", response.getEntity().getText());

        response = put("integerReturnInteger", null);
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
        assertEmptyEntity(response);
    }

    public void test5() throws Exception {
        Response response = put("byteArrayReturnByteArray",
                new StringRepresentation("test", (MediaType) null));
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("test", response.getEntity().getText());
    }
}
