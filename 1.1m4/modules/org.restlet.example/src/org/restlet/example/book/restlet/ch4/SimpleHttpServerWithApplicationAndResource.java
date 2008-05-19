package org.restlet.example.book.restlet.ch4;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Protocol;

public class SimpleHttpServerWithApplicationAndResource {
    public static void main(String[] args) {

        Application application = new Application() {

            @Override
            public synchronized Restlet createRoot() {
                // Tiens le routeur recupere le contexte de l'application
                Router router = new Router(getContext());
                router.attach("helloWorld", HelloWorldResource.class);
                return router;
            }
        };

        Component component = new Component();
        component.getServers().add(Protocol.HTTP);
        component.getDefaultHost().attach(application);
        try {
            component.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
