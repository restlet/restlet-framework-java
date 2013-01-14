/**
 * Copyright 2005-2013 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.servlet;

import java.util.HashSet;

import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;
import org.restlet.ext.osgi.IApplicationProvider;

/**
 * @author Bryan Hunt
 * @author Wolfgang Werner
 * 
 */
public class RestletServletService {
    private HashSet<IApplicationProvider> applicationProviders = new HashSet<IApplicationProvider>();

    private HttpService httpService;

    private LogService logService;

    public void bindApplicationProvider(IApplicationProvider applicationProvider) {
        applicationProviders.add(applicationProvider);

        if (httpService != null)
            registerServlet(applicationProvider);
    }

    public void bindHttpService(HttpService httpService) {
        this.httpService = httpService;

        for (IApplicationProvider applicationProvider : applicationProviders)
            registerServlet(applicationProvider);
    }

    public void bindLogService(LogService logService) {
        this.logService = logService;
    }

    private void registerServlet(IApplicationProvider applicationProvider) {
        ApplicationServlet servlet = new ApplicationServlet(applicationProvider);

        try {
            httpService.registerServlet(applicationProvider.getAlias(),
                    servlet, applicationProvider.getInitParms(),
                    applicationProvider.getContext());
        } catch (Exception e) {
            if (logService != null)
                logService.log(LogService.LOG_ERROR,
                        "Failed to register the application servlet at alias: '"
                                + applicationProvider.getAlias() + "'", e);
        }
    }

    public void unbindApplicationProvider(
            IApplicationProvider applicationProvider) {
        applicationProviders.remove(applicationProvider);

        if (httpService != null) {
            try {
                httpService.unregister(applicationProvider.getAlias());
            } catch (Throwable t) {
            }
        }
    }

    public void unbindHttpService(HttpService httpService) {
        if (this.httpService == httpService) {
            for (IApplicationProvider applicationProvider : applicationProviders) {
                try {
                    httpService.unregister(applicationProvider.getAlias());
                } catch (IllegalArgumentException e) {
                }
            }

            httpService = null;
        }
    }

    public void unbindLogService(LogService logService) {
        if (this.logService == logService)
            this.logService = null;
    }
}
