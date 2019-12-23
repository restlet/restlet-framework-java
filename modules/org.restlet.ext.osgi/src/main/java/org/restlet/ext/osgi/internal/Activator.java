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

package org.restlet.ext.osgi.internal;

import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.restlet.ext.osgi.ObapClientHelper;

/**
 * OSGi activator. It registers the installed bundles in order to cope with
 * futur calls made using the OBAP protocol.
 * 
 * @author Thierry Boileau
 * @See {@link ObapClientHelper}
 */
public class Activator implements BundleActivator {

    private static Logger logger = Logger.getLogger("org.restlet.ext.osgi");

    @Override
    public void start(BundleContext context) throws Exception {
        for (Bundle bundle : context.getBundles()) {
            if (!ObapClientHelper.register(bundle)) {
                logger.warning("OBAP client helper can't register this bundle: "
                        + bundle.getBundleId()
                        + " at location "
                        + bundle.getLocation());
            }
        }

        // Listen to installed bundles
        context.addBundleListener(new BundleListener() {
            public void bundleChanged(BundleEvent event) {
                switch (event.getType()) {
                case BundleEvent.INSTALLED:
                    if (!ObapClientHelper.register(event.getBundle())) {
                        logger.warning("OBAP client helper can't register this bundle: "
                                + event.getBundle().getBundleId()
                                + " at location "
                                + event.getBundle().getLocation());
                    }
                    break;
                }
            }
        });
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        ObapClientHelper.clear();
    }

}