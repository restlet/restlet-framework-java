package org.restlet.example.book.restlet.misc;

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

                String tmpDirUri = "file:///"
                        + System.getProperty("java.io.tmpdir");
                // Serve static files (images, etc)
                Directory directory = new Directory(getContext(), tmpDirUri);
                directory.setListingAllowed(true);
                router.attach("/static", directory);

                // Attach Resources
                router.attach("/hello", MergeSitesServicesServerResource.class);

                return router;
            }
        };

        component.getDefaultHost().attach(app);
        component.start();
    }
}
