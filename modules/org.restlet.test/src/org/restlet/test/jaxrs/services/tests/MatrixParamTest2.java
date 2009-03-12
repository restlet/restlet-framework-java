/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

package org.restlet.test.jaxrs.services.tests;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.MatrixParam;
import javax.ws.rs.core.Application;

import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.test.jaxrs.services.resources.MatrixParamTestService2;

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
            @SuppressWarnings("unchecked")
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(MatrixParamTestService2.class);
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