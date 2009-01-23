package org.restlet.util;

import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Restlet wrapper. Useful for application developer who need to wrap a Restlet
 * instance.
 * 
 * @author Thierry Boileau
 * @see <a href="http://c2.com/cgi/wiki?DecoratorPattern">The decorator (aka
 *      wrapper) pattern</a>
 */
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
