package org.restlet.example.book.restlet.ch10;

import org.restlet.Application;
import org.restlet.Directory;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.LocalReference;

public class XmlApplication extends Application {

    @Override
    public Restlet createRoot() {
        final Router router = new Router(getContext());
        router.attach("/dom", DomResource.class);
        final Directory dir = new Directory(
                getContext(),
                LocalReference
                        .createFileReference("D:\\workspace\\restlet-1.1\\BouquinApress\\src\\chapter7"));
        router.attach("/xml", dir);
        return router;
    }
}
