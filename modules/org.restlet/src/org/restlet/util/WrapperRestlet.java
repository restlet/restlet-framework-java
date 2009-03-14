/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
 
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
