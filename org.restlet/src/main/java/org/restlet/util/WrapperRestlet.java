/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.util;

import java.util.logging.Logger;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;

/**
 * Restlet wrapper. Useful for developers who need to wrap a Restlet instance.
 *
 * @author Thierry Boileau
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka wrapper) pattern</a>
 */
public class WrapperRestlet extends Restlet {

    /** The wrapped Restlet instance. */
    private Restlet wrappedRestlet;

    /**
     * Constructor.
     *
     * @param wrappedRestlet The wrapped Restlet instance.
     */
    public WrapperRestlet(Restlet wrappedRestlet) {
        super();
        this.wrappedRestlet = wrappedRestlet;
    }

    @Override
    public org.restlet.Application getApplication() {
        return wrappedRestlet.getApplication();
    }

    @Override
    public String getAuthor() {
        return wrappedRestlet.getAuthor();
    }

    @Override
    public Context getContext() {
        return wrappedRestlet.getContext();
    }

    @Override
    public String getDescription() {
        return wrappedRestlet.getDescription();
    }

    @Override
    public Logger getLogger() {
        return wrappedRestlet.getLogger();
    }

    @Override
    public String getName() {
        return wrappedRestlet.getName();
    }

    @Override
    public String getOwner() {
        return wrappedRestlet.getOwner();
    }

    @Override
    public void handle(Request request, Response response) {
        wrappedRestlet.handle(request, response);
    }

    @Override
    public boolean isStarted() {
        return wrappedRestlet.isStarted();
    }

    @Override
    public boolean isStopped() {
        return wrappedRestlet.isStopped();
    }

    @Override
    public void setAuthor(String author) {
        wrappedRestlet.setAuthor(author);
    }

    @Override
    public void setContext(Context context) {
        wrappedRestlet.setContext(context);
    }

    @Override
    public void setDescription(String description) {
        wrappedRestlet.setDescription(description);
    }

    @Override
    public void setName(String name) {
        wrappedRestlet.setName(name);
    }

    @Override
    public void setOwner(String owner) {
        wrappedRestlet.setOwner(owner);
    }

    @Override
    public synchronized void start() throws Exception {
        wrappedRestlet.start();
    }

    @Override
    public synchronized void stop() throws Exception {
        wrappedRestlet.stop();
    }
}
