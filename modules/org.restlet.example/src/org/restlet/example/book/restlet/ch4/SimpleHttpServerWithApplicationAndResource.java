package org.restlet.example.book.restlet.ch4;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Protocol;

public class SimpleHttpServerWithApplicationAndResource {
    public static void main(String[] args) {

        final Application application = new Application() {

            @Override
            public synchronized Restlet createRoot() {
                // Tiens le routeur recupere le contexte de l'application
                final Router router = new Router(getContext());
                router.attach("helloWorld", HelloWorldResource.class);
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
