package org.restlet.ext.wadl;

import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Restlet wrapper. Useful for application developer who need to provide the
 * WADL documentation for a Restlet instance.
 * 
 */
public abstract class WadlWrapper extends Restlet implements WadlDescribable {

    /** The wrapped Restlet instance. */
    private Restlet wrappedRestlet;

    /**
     * Constructor.
     * 
     * @param wrappedRestlet
     *            The wrapped Restlet instance.
     */
    public WadlWrapper(Restlet wrappedRestlet) {
        super();
        this.wrappedRestlet = wrappedRestlet;
    }

    @Override
    public Application getApplication() {
        return wrappedRestlet.getApplication();
    }

    @Override
    public Context getContext() {
        return wrappedRestlet.getContext();
    }

    @Override
    public Logger getLogger() {
        return wrappedRestlet.getLogger();
    }

    /**
     * Provides the data available about the wrapped Restlet.
     * 
     * @return The ResourceInfo object of the wrapped Restlet.
     */
    public abstract ResourceInfo getResourceInfo();

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
    public void setContext(Context context) {
        wrappedRestlet.setContext(context);
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
