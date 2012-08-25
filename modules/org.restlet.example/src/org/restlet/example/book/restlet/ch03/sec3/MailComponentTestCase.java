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
