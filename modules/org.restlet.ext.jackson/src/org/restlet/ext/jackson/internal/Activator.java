package org.restlet.ext.jackson.internal;

import org.codehaus.stax2.osgi.Stax2InputFactoryProvider;
import org.codehaus.stax2.osgi.Stax2OutputFactoryProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.restlet.Context;

import java.util.logging.Level;

/**
 * Register Stax2IntputFactoryProvider and Stax2OutputFactoryProvider in OSGI context.
 * In a no-OSGI context, these factories are retrieved with java service loader.
 *
 * <p>Note: Stax2 implementation is provided by woodstox library which
 * is a dependency of Jackson.</p>
 *
 * @author Manuel Boillod
 */
public class Activator implements BundleActivator {

    @Override
    public void start(final BundleContext bundleContext) throws Exception {
        ServiceReference inputFactoryServiceReference = bundleContext.getServiceReference("org.codehaus.stax2.osgi.Stax2InputFactoryProvider");
        if (inputFactoryServiceReference != null) {
            Stax2InputFactoryProvider inputFactoryProvider = (Stax2InputFactoryProvider)bundleContext.getService(inputFactoryServiceReference);
            registerInputFactory(inputFactoryProvider);
        }

        ServiceReference outputFactoryServiceReference = bundleContext.getServiceReference("org.codehaus.stax2.osgi.Stax2OutputFactoryProvider");
        if (outputFactoryServiceReference != null) {
            Stax2OutputFactoryProvider outputFactoryProvider = (Stax2OutputFactoryProvider)bundleContext.getService(outputFactoryServiceReference);
            registerOutputFactory(outputFactoryProvider);
        }

        // Listen to installed service of type Stax2InputFactoryProvider
        bundleContext.addServiceListener(new ServiceListener() {
            @Override
            public void serviceChanged(ServiceEvent serviceEvent) {
                switch (serviceEvent.getType()) {
                    case ServiceEvent.REGISTERED:
                    case ServiceEvent.MODIFIED:
                        ServiceReference serviceReference = serviceEvent.getServiceReference();
                        Stax2InputFactoryProvider factoryProvider = (Stax2InputFactoryProvider)bundleContext.getService(serviceReference);
                        registerInputFactory(factoryProvider);
                        break;

                    case ServiceEvent.UNREGISTERING:
                        unregisterInputFactory();
                        break;
                }
            }
        }, "(objectclass=org.codehaus.stax2.osgi.Stax2InputFactoryProvider)");

        // Listen to installed service of type Stax2OutputFactoryProvider
        bundleContext.addServiceListener(new ServiceListener() {
            @Override
            public void serviceChanged(ServiceEvent serviceEvent) {
                switch (serviceEvent.getType()) {
                    case ServiceEvent.REGISTERED:
                    case ServiceEvent.MODIFIED:
                        ServiceReference serviceReference = serviceEvent.getServiceReference();
                        Stax2OutputFactoryProvider factoryProvider = (Stax2OutputFactoryProvider)bundleContext.getService(serviceReference);
                        registerOutputFactory(factoryProvider);
                        break;

                    case ServiceEvent.UNREGISTERING:
                        unregisterOutputFactory();
                        break;
                }
            }
        }, "(objectclass=org.codehaus.stax2.osgi.Stax2OutputFactoryProvider)");
    }

    private void registerInputFactory(Stax2InputFactoryProvider inputFactoryProvider) {
        Context.getCurrentLogger().log(Level.INFO, "Register Stax2InputFactoryProvider");
        XmlFactoryProvider.inputFactoryProvider = inputFactoryProvider;
    }

    private void unregisterInputFactory() {
        Context.getCurrentLogger().log(Level.INFO, "Unregister Stax2InputFactoryProvider");
        XmlFactoryProvider.inputFactoryProvider = null;
    }

    private void registerOutputFactory(Stax2OutputFactoryProvider outputFactoryProvider) {
        Context.getCurrentLogger().log(Level.INFO, "Register Stax2OutputFactoryProvider");
        XmlFactoryProvider.outputFactoryProvider = outputFactoryProvider;

    }

    private void unregisterOutputFactory() {
        Context.getCurrentLogger().log(Level.INFO, "Unregister Stax2OutputFactoryProvider");
        XmlFactoryProvider.outputFactoryProvider = null;
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {

    }
}
