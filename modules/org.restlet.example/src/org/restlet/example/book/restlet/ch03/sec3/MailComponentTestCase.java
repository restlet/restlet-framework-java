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

package org.restlet.example.book.restlet.ch03.sec3;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.example.book.restlet.ch03.sec3.server.MailServerComponent;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

/**
 * Concurent test case with TestNG
 * 
 * @author Jerome Louvel
 */
public class MailComponentTestCase {

    private final MailServerComponent component;

    public MailComponentTestCase() throws Exception {
        component = new MailServerComponent();
    }

    @BeforeSuite
    public void beforeSuite() throws Exception {
        component.start();
    }

    @Test(threadPoolSize = 10, invocationCount = 100, timeOut = 5000)
    public void makeCall() {
        Request request = new Request(Method.GET, "http://localhost:8111/");
        Response response = component.handle(request);

        Assert.assertTrue(response.getStatus().isSuccess());
        Assert.assertEquals("Welcome to the RESTful Mail Server application !",
                response.getEntityAsText());
    }

    @AfterSuite
    public void afterSuite() throws Exception {
        component.stop();
    }

}
