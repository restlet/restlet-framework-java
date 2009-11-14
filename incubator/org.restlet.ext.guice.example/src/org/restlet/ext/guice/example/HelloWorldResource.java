package org.restlet.ext.guice.example;

import com.google.inject.*;
import com.google.inject.name.Named;

import java.util.concurrent.atomic.AtomicInteger;

import org.restlet.*;
import org.restlet.data.*;
import org.restlet.representation.*;
import org.restlet.resource.*;

/**
 * Resource which has only one representation.
 * 
 */
public class HelloWorldResource extends Resource {

    static final String HELLO_MSG = "hello.message";

    private static final AtomicInteger count = new AtomicInteger();

    @Inject
    public HelloWorldResource(@Named(HELLO_MSG) String msg, Request request,
            Response response, Context context) {
        super(context, request, response);
        this.msg = msg;
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        String text = String.format("%d: %s", count.incrementAndGet(), msg);
        Representation representation = new StringRepresentation(text,
                MediaType.TEXT_PLAIN);
        return representation;
    }

    private final String msg;
}
