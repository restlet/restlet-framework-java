/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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
package org.restlet.example.jaxrs;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.example.jaxrs.employees.EmployeesResource;

/**
 * An {@link ApplicationConfig} contains the root resource classes and the
 * providers for an JAX-RS application.<br>
 * This example application configuration contains two root resource classes (
 * {@link EasyRootResource} and {@link EmployeesResource}, see
 * {@link #getResourceClasses()}) and no provider (default, would be returned by
 * {@link ApplicationConfig#getProviderClasses()}.
 * 
 * @author Stephan Koops
 * @see EasyRootResource
 * @see EmployeesResource
 * @see ExampleServer
 * @see GuardedExample
 */
public class ExampleApplication extends Application {

    /**
     * creates a new Application configuration for this example.
     */
    public ExampleApplication() {
    }

    /**
     * @see javax.ws.rs.core.ApplicationConfig#getResourceClasses()
     */
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> rrcs = new HashSet<Class<?>>();
        rrcs.add(EasyRootResource.class);
        rrcs.add(EmployeesResource.class);
        return rrcs;
    }
}