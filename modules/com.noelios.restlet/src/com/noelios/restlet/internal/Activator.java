/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.internal;

import java.net.URL;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.restlet.Client;
import org.restlet.Server;

import com.noelios.restlet.Engine;

/**
 * OSGi activator. It registers the NRE into the Restlet API and also introspect
 * the bundles to find connector or authentication helpers.
 * 
 * @author Jerome Louvel
 */
public class Activator implements BundleActivator {

    /**
     * Registers the helpers for a given bundle.
     * 
     * @param bundle
     *            The bundle to inspect.
     * @param helpers
     *            The helpers list to update.
     * @param constructorClass
     *            The class to use as constructor parameter.
     * @param descriptorPath
     *            The descriptor file path.
     */
    @SuppressWarnings("unchecked")
    private void registerHelper(Bundle bundle, List helpers,
            Class constructorClass, String descriptorPath) {
        // Discover server helpers
        URL configUrl = bundle.getEntry(descriptorPath);

        if (configUrl == null) {
            configUrl = bundle.getEntry("/src/" + descriptorPath);
        }

        if (configUrl != null) {
            registerHelper(bundle, helpers, constructorClass, configUrl);
        }
    }

    /**
     * Registers the helpers for a given bundle.
     * 
     * @param bundle
     *            The bundle to inspect.
     * @param helpers
     *            The helpers list to update.
     * @param constructorClass
     *            The class to use as constructor parameter.
     * @param descriptorUrl
     *            The descriptor URL to inspect.
     */
    @SuppressWarnings("unchecked")
    private void registerHelper(final Bundle bundle, List helpers,
            Class constructorClass, URL descriptorUrl) {
        Engine.getInstance().registerHelper(new ClassLoader() {
            @Override
            public Class<?> loadClass(String name)
                    throws ClassNotFoundException {
                return bundle.loadClass(name);
            }
        }, descriptorUrl, helpers, constructorClass);
    }

    /**
     * Registers the helpers for a given bundle.
     * 
     * @param bundle
     *            The bundle to inspect.
     */
    private void registerHelpers(Bundle bundle) {
        // Register server helpers
        registerHelper(bundle, Engine.getInstance().getRegisteredServers(),
                Server.class, Engine.DESCRIPTOR_SERVER_PATH);

        // Register client helpers
        registerHelper(bundle, Engine.getInstance().getRegisteredClients(),
                Client.class, Engine.DESCRIPTOR_CLIENT_PATH);

        // Register authentication helpers
        registerHelper(bundle, Engine.getInstance()
                .getRegisteredAuthentications(), null,
                Engine.DESCRIPTOR_AUTHENTICATION_PATH);
    }

    /**
     * Starts the OSGi bundle by registering the engine with the bundle of the
     * Restlet API.
     * 
     * @param context
     *            The bundle context.
     */
    public void start(BundleContext context) throws Exception {
        org.restlet.util.Engine.setInstance(new Engine(false));

        // Discover helpers in installed bundles and start
        // the bundle if necessary
        for (final Bundle bundle : context.getBundles()) {
            registerHelpers(bundle);
        }

        // Listen to installed bundles
        context.addBundleListener(new BundleListener() {
            public void bundleChanged(BundleEvent event) {
                switch (event.getType()) {
                case BundleEvent.INSTALLED:
                    registerHelpers(event.getBundle());
                    break;

                case BundleEvent.UNINSTALLED:
                    break;
                }
            }
        });

        Engine.getInstance().registerDefaultConnectors();
        Engine.getInstance().registerDefaultAuthentications();
    }

    /**
     * Stops the OSGi bundle by deregistering the engine with the bundle of the
     * Restlet API.
     * 
     * @param context
     *            The bundle context.
     */
    public void stop(BundleContext context) throws Exception {
        org.restlet.util.Engine.setInstance(null);
    }

}
