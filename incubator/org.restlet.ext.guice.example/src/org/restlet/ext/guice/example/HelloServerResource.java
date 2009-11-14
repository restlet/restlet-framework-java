package org.restlet.ext.guice.example;

import java.util.concurrent.atomic.AtomicInteger;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Resource which has only one representation.
 * 
 */
public class HelloServerResource extends ServerResource {

    static final String HELLO_MSG = "HelloServerResource.message";

    private static final AtomicInteger count = new AtomicInteger();

    @Inject
    public HelloServerResource(@Named(HELLO_MSG) String msg) {
        this.msg = msg;
    }

    @Get
    public Representation asString() {
        String text = String.format("%d: %s", count.incrementAndGet(), msg);
        Representation representation = new StringRepresentation(text,
                MediaType.TEXT_PLAIN);
        return representation;
    }

    private final String msg;
}
