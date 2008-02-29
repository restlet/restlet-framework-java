package org.restlet.test;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class HelloWorldApplication extends Application {

    public HelloWorldApplication(Context parentContext) {
        super(parentContext);
    }

    @Override
    public synchronized Restlet createRoot() {
        return new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                response.setEntity("hello, world", MediaType.TEXT_PLAIN);
            }
        };
    }
}
