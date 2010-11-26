package org.restlet.example.book.restlet.ch08.sec1.sub3;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

public class MergeSitesServicesServer {

    public static void main(String[] args) throws Exception {
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);
        component.getClients().add(Protocol.FILE);

        Application app = new Application() {
            @Override
            public Restlet createInboundRoot() {
                Router router = new Router(getContext());

                // Serve static files (images, etc.)
                String rootUri = "file:///"
                        + System.getProperty("java.io.tmpdir");
                Directory directory = new Directory(getContext(), rootUri);
                directory.setListingAllowed(true);
                router.attach("/static", directory);

                // Attach the hello web service
                router.attach("/hello", HelloServerResource.class);

                return router;
            }
        };

        component.getDefaultHost().attach(app);
        component.start();
    }
}
