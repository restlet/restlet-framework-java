/*
 * Copyright 2005-2008 Noelios Technologies.
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
package org.restlet.example.jaxrs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.ApplicationConfig;
import javax.ws.rs.core.MediaType;

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
public class ExampleAppConfig extends ApplicationConfig {

    /**
     * creates a new Application configuration for this example.
     */
    public ExampleAppConfig() {
    }

    @Override
    public Map<String, MediaType> getMediaTypeMappings() {
        final Map<String, MediaType> map = new HashMap<String, MediaType>();
        map.put("html", MediaType.TEXT_HTML_TYPE);
        map.put("xml", MediaType.APPLICATION_XML_TYPE);
        map.put("json", MediaType.APPLICATION_JSON_TYPE);
        return map;
    }

    /**
     * @see javax.ws.rs.core.ApplicationConfig#getResourceClasses()
     */
    @Override
    public Set<Class<?>> getResourceClasses() {
        final Set<Class<?>> rrcs = new HashSet<Class<?>>();
        rrcs.add(EasyRootResource.class);
        rrcs.add(EmployeesResource.class);
        return rrcs;
    }
}