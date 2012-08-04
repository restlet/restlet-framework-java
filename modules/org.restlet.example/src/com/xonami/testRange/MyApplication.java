package com.xonami.testRange;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

public class MyApplication extends Application {

    public static void main(String[] args) throws Exception {
        Component c = new Component();
        c.getServers().add(Protocol.HTTP, 8182);

        c.getDefaultHost().attach("/test", new MyApplication());

        c.start();
    }

    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());
        router.attach("/res", new SecureFilter(getContext(),
                MyServerResource.class, false, true));
        return router;
    }

}
