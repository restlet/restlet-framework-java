/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.engine.application;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.ChainHelper;
import org.restlet.routing.Filter;
import org.restlet.service.Service;

// [excludes gwt]
/**
 * Application implementation.
 * 
 * @author Jerome Louvel
 */
public class ApplicationHelper extends ChainHelper<Application> {
    /**
     * Constructor.
     * 
     * @param application
     *            The application to help.
     */
    public ApplicationHelper(Application application) {
        super(application);
    }

    /**
     * In addition to the default behavior, it saves the current application
     * instance into the current thread.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        // Save the current application
        Application.setCurrent(getHelped());

        // Actually handle call
        super.handle(request, response);
    }

    /** Start hook. */
    @Override
    public synchronized void start() throws Exception {
        // Attach the service inbound filters
        Filter inboundFilter = null;

        for (Service service : getHelped().getServices()) {
            if (service.isEnabled()) {
                inboundFilter = service.createInboundFilter(getContext());

                if (inboundFilter != null) {
                    addFilter(inboundFilter);
                }
            }
        }

        // Attach the Application's server root Restlet
        setNext(getHelped().getInboundRoot());
    }

    @Override
    public synchronized void stop() throws Exception {
        clear();
    }

    @Override
    public void update() throws Exception {
    }

}
