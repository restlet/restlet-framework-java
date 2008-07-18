package org.restlet.example.book.restlet.ch4;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Protocol;

public class SimpleHttpServerWithResource {
    public static void main(String[] args) {

        final Application application = new Application() {

            @Override
            public synchronized Restlet createRoot() {
                final Router router = new Router(getContext());

                // TODO attach a "hello, world" resource.
                return router;
            }
        };

        final Component component = new Component();
        component.getServers().add(Protocol.HTTP);
        component.getDefaultHost().attach(application);
        try {
            component.start();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
