package org.restlet.example.book.restlet.ch08.sec4.sub5;

import org.restlet.Component;
import org.restlet.data.Protocol;

public class CachingServer {

    public static void main(String[] args) throws Exception {
        Component component = new Component();
        component.getDefaultHost().attachDefault(
                CachingServerResource.class);
        component.getServers().add(Protocol.HTTP, 8111);
        component.start();
    }

}
