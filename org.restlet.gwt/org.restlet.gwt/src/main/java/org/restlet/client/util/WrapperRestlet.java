/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.client.util;

import java.util.logging.Logger;

import org.restlet.client.Context;
import org.restlet.client.Request;
import org.restlet.client.Response;
import org.restlet.client.Restlet;

/**
 * Restlet wrapper. Useful for application developer who need to wrap a Restlet
 * instance.
 * 
 * @author Thierry Boileau
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka
 *      wrapper) pattern</a>
 * @deprecated Will be removed in the next 2.7/3.0 release.
 */
@Deprecated
public class WrapperRestlet extends Restlet {

    /** The wrapped Restlet instance. */
    private Restlet wrappedRestlet;

    /**
     * Constructor.
     * 
     * @param wrappedRestlet
     *            The wrapped Restlet instance.
     */
    public WrapperRestlet(Restlet wrappedRestlet) {
        super();
        this.wrappedRestlet = wrappedRestlet;
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
