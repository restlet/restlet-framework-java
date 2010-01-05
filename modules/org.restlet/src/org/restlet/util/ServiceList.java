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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.service.Service;

/**
 * Modifiable list of services.
 * 
 * @author Jerome Louvel
 */
public final class ServiceList extends WrapperList<Service> {

    /**
     * Constructor.
     */
    public ServiceList() {
        super(new CopyOnWriteArrayList<Service>());
    }

    /**
     * Returns a service matching a given service class.
     * 
     * @param <T>
     *            The service type.
     * @param clazz
     *            The service class to match.
     * @return The matched service instance.
     */
    @SuppressWarnings("unchecked")
    public <T extends Service> T get(Class<T> clazz) {
        for (Service service : this) {
            if (clazz.isAssignableFrom(service.getClass())) {
                return (T) service;
            }
        }

        return null;
    }

    /**
     * Sets the list of services.
     * 
     * @param services
     *            The list of services.
     */
    public synchronized void set(List<Service> services) {
        clear();

        if (services != null) {
            addAll(services);
        }
    }

    /**
     * Replaces or adds a service. The replacement is based on the service
     * class.
     * 
     * @param newService
     *            The new service to set.
     */
    public synchronized void set(Service newService) {
        List<Service> services = new CopyOnWriteArrayList<Service>();
        Service service;
        boolean replaced = false;

        for (int i = 0; (i < size()); i++) {
            service = get(i);

            if (service != null) {
                if (service.getClass().isAssignableFrom(newService.getClass())) {
                    try {
                        service.stop();
                    } catch (Exception e) {
                        Context.getCurrentLogger().log(Level.WARNING,
                                "Unable to stop service replaced", e);
                    }

                    services.add(newService);
                    replaced = true;
                } else {
                    services.add(service);
                }
            }
        }

        if (!replaced) {
            services.add(newService);
        }

        set(services);
    }

    /**
     * Starts each service.
     * 
     * @throws Exception
     */
    public void start() throws Exception {
        for (Service service : this) {
            service.start();
        }
    }

    /**
     * Stops each service.
     * 
     * @throws Exception
     */
    public void stop() throws Exception {
        for (Service service : this) {
            service.stop();
        }
    }

}
