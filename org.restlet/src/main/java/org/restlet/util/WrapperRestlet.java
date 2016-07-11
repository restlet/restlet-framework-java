/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.util;

import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;

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

    // [ifndef gwt] method
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
