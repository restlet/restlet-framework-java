/**
 * Copyright 2005-2014 Restlet
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
 * Restlet is a registered trademark of Restlet
 */

package org.restlet.ext.osgi;

import org.osgi.framework.Bundle;
import org.osgi.service.log.LogService;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

/**
 * This class allows Restlet to lazily load resources in an OSGi environment.
 * This class may be used as the finder in a @see ResourceProvider.
 * 
 * @author Bryan Hunt
 * @author Wolfgang Werner
 */
public class DynamicFinder extends Finder {
    private Bundle bundle;

    private String className;

    private LogService logService;

    private Class<? extends ServerResource> targetClass;

    /**
     * @param bundle
     *            the bundle containing the resource - must not be null
     * @param className
     *            the class name of the resource - must not be null
     */
    public DynamicFinder(Bundle bundle, String className) {
        this(bundle, className, null);
    }

    /**
     * @param bundle
     *            the bundle containg the resource - must not be null
     * @param className
     *            the class name of the resource - must not be null
     * @param logService
     *            the OSGi log service for logging errors - may be null
     */
    public DynamicFinder(Bundle bundle, String className, LogService logService) {
        if (bundle == null)
            throw new IllegalArgumentException("bundle must not be null");

        if (className == null)
            throw new IllegalArgumentException("className must not be null");

        this.bundle = bundle;
        this.className = className;
        this.logService = logService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ServerResource> getTargetClass() {
        if (targetClass == null) {
            try {
                targetClass = (Class<? extends ServerResource>) bundle
                        .loadClass(className);
            } catch (ClassNotFoundException e) {
                if (logService != null)
                    logService.log(LogService.LOG_ERROR,
                            "Failed to load class: '" + className + "'", e);
            }
        }

        return targetClass;
    }
}
