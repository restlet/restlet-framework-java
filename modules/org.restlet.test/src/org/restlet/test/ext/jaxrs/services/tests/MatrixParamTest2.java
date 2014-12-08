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

import javax.ws.rs.MatrixParam;
import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.test.ext.jaxrs.services.resources.MatrixParamTestService2;

/**
 * @author Stephan Koops
 * @see MatrixParamTestService2
 * @see MatrixParam
 */
public class MatrixParamTest2 extends JaxRsTestCase {

    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections
                        .singleton(MatrixParamTestService2.class);
            }
        };
    }

    public void testEncodedWithDefault() throws Exception {
        Response response = get("encodedWithDefault;m=1;m=2;x=3");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[1, 2]", response.getEntity().getText());

        response = get("encodedWithDefault;m=1;i=2;x=3");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[1]", response.getEntity().getText());

        response = get("encodedWithDefault;a=1;i=2;x=3");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("[default]", response.getEntity().getText());
    }

    public void testWithoutPath() throws Exception {
        Response response = get(";firstname=Angela;lastname=Merkel");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("Angela Merkel", response.getEntity().getText());

        response = get(";lastname=Merkel;firstname=Angela");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("Angela Merkel", response.getEntity().getText());

        response = get(";firstname=Goofy");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("Goofy null", response.getEntity().getText());

        response = get(";lastname=Goofy");
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("null Goofy", response.getEntity().getText());
    }
}
