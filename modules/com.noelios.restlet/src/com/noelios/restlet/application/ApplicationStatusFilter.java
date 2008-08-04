/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.application;

import org.restlet.Application;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;

import com.noelios.restlet.StatusFilter;

/**
 * Status filter that tries to obtain ouput representation from an application.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ApplicationStatusFilter extends StatusFilter {
    /** The application. */
    private Application application;

    /**
     * Constructor.
     * 
     * @param application
     *            The application.
     */
    public ApplicationStatusFilter(Application application) {
        super(application.getContext(), application.getStatusService()
                .isOverwrite(), application.getStatusService()
                .getContactEmail(), "/");
        this.application = application;
    }

    /**
     * Returns the application.
     * 
     * @return The application.
     */
    public Application getApplication() {
        return this.application;
    }

    @Override
    public Representation getRepresentation(Status status, Request request,
            Response response) {
        Representation result = getApplication().getStatusService()
                .getRepresentation(status, request, response);
        if (result == null)
            result = super.getRepresentation(status, request, response);
        return result;
    }

    @Override
    public Status getStatus(Throwable throwable, Request request,
            Response response) {
        Status result = getApplication().getStatusService().getStatus(
                throwable, request, response);
        if (result == null)
            result = super.getStatus(throwable, request, response);
        return result;
    }

}
