/**
 * Copyright 2005-2019 Talend
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.ext.servlet;

import java.util.HashSet;

import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;
import org.restlet.ext.osgi.ApplicationProvider;

/**
 * @author Bryan Hunt
 * @author Wolfgang Werner
 * 
 */
public class RestletServletService {
    private HashSet<ApplicationProvider> applicationProviders = new HashSet<ApplicationProvider>();

    private HttpService httpService;

    private LogService logService;

    public void bindApplicationProvider(ApplicationProvider applicationProvider) {
        applicationProviders.add(applicationProvider);

        if (httpService != null)
            registerServlet(applicationProvider);
    }

    public void bindHttpService(HttpService httpService) {
        this.httpService = httpService;

        for (ApplicationProvider applicationProvider : applicationProviders)
            registerServlet(applicationProvider);
    }

    public void bindLogService(LogService logService) {
        this.logService = logService;
    }

    private void registerServlet(ApplicationProvider applicationProvider) {
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
            ApplicationProvider applicationProvider) {
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
            for (ApplicationProvider applicationProvider : applicationProviders) {
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
