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

import java.util.Collection;
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

    /** The context. */
    private volatile Context context;

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     */
    public ServiceList(Context context) {
        super(new CopyOnWriteArrayList<Service>());
        this.context = context;
    }

    @Override
    public void add(int index, Service service) {
        service.setContext(getContext());
        super.add(index, service);
    }

    @Override
    public boolean add(Service service) {
        service.setContext(getContext());
        return super.add(service);
    }

    @Override
    public boolean addAll(Collection<? extends Service> services) {
        if (services != null) {
            for (Service service : services) {
                service.setContext(getContext());
            }
        }

        return super.addAll(services);
    }

    @Override
    public boolean addAll(int index, Collection<? extends Service> services) {
        if (services != null) {
            for (Service service : services) {
                service.setContext(getContext());
            }
        }

        return super.addAll(index, services);
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
     * Returns the context.
     * 
     * @return The context.
     */
    public Context getContext() {
        return this.context;
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
     * Sets the context. By default, it also updates the context of already
     * registered services.
     * 
     * @param context
     *            The context.
     */
    public void setContext(Context context) {
        this.context = context;

        for (Service service : this) {
            service.setContext(context);
        }
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
