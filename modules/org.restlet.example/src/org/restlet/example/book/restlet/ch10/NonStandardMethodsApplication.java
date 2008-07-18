package org.restlet.example.book.restlet.ch10;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;

public class NonStandardMethodsApplication extends Application {

    public NonStandardMethodsApplication(Context parentContext) {
        super(parentContext);
    }

    @Override
    public Restlet createRoot() {
        final Router router = new Router(getContext());
        router.attachDefault(NonStandardMethodsResource.class);
        return router;
    }
}
