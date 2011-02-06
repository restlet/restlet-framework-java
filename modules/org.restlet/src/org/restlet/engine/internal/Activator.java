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

package org.restlet.engine.internal;

import java.net.URL;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.restlet.Client;
import org.restlet.Server;
import org.restlet.engine.Engine;

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
    private void registerHelper(Bundle bundle, List<?> helpers,
            Class<?> constructorClass, String descriptorPath) {
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
    private void registerHelper(final Bundle bundle, List<?> helpers,
            Class<?> constructorClass, URL descriptorUrl) {
        Engine.getInstance().registerHelpers(new ClassLoader() {
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
                .getRegisteredAuthenticators(), null,
                Engine.DESCRIPTOR_AUTHENTICATOR_PATH);

        // Register converter helpers
        registerHelper(bundle, Engine.getInstance().getRegisteredConverters(),
                null, Engine.DESCRIPTOR_CONVERTER_PATH);
    }

    /**
     * Starts the OSGi bundle by registering the engine with the bundle of the
     * Restlet API.
     * 
     * @param context
     *            The bundle context.
     */
    public void start(BundleContext context) throws Exception {
        org.restlet.engine.Engine.setInstance(new Engine(false));

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
        Engine.getInstance().registerDefaultConverters();
    }

    /**
     * Stops the OSGi bundle by unregistering the engine with the bundle of the
     * Restlet API.
     * 
     * @param context
     *            The bundle context.
     */
    public void stop(BundleContext context) throws Exception {
        org.restlet.engine.Engine.setInstance(null);
    }

}
