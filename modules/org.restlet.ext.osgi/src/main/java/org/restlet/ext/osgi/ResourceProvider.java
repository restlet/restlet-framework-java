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

package org.restlet.ext.osgi;

/**
 * This is an OSGi service interface for registering Restlet resources with an
 * application. Users are expected to register an instance as an OSGi service.
 * It is recommended that you extend the {@link BaseResourceProvider}
 * implementation. You may provide your own implementation of
 * {@link ResourceProvider} if you need complete control. Resources are
 * registered with an application according to the application alias. If an
 * application is not found that corresponds to the specified alias, the
 * resource will be cached until the application is registered. If your
 * resources are not being registered, check there is not a typo in the alias in
 * both the resource provider and application provider.
 * 
 * It is recommended that you use or extend {@link ResourceBuilder}
 * 
 * @author Bryan Hunt
 * @author Wolfgang Werner
 */
public interface ResourceProvider extends RestletProvider {
    /**
     * 
     * @return the matching mode to be used for template routes. Defaults to
     *         Template.MODE_EQUALS.
     */
    int getMatchingMode();

    /**
     * 
     * @return the paths to the resource relative to the application alias. The
     *         paths must start with '/'.
     */
    String[] getPaths();
}
