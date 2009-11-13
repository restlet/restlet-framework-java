package org.restlet.ext.guice.examples;

import org.restlet.Component;
import org.restlet.data.Protocol;

public class Main {
    public static void main(String[] args) throws Exception {
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);
        component.getDefaultHost().attach(new FirstStepsApplication());
        component.start();
    }
}
