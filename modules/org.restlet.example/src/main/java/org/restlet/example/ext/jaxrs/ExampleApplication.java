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

package org.restlet.example.ext.jaxrs;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.example.ext.jaxrs.employees.EmployeesResource;

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
