package org.restlet.example.book.restlet.ch10;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Router;

public class NonStandardMethodsApplication extends Application {

    @Override
    public Restlet createRoot() {
        final Router router = new Router(getContext());
        router.attachDefault(NonStandardMethodsResource.class);
        return router;
    }
}
