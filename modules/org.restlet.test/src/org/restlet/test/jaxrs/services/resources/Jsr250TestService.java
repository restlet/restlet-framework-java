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
package org.restlet.test.jaxrs.services.resources;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;

import org.restlet.test.jaxrs.services.providers.Jsr250TestProvider;
import org.restlet.test.jaxrs.services.tests.Jsr250Test;

/**
 * @author Stephan Koops
 * @see Jsr250Test
 * @see Jsr250TestProvider
 */
@ProduceMime("text/plain")
@Path("jsr250test")
public class Jsr250TestService {
    
    /**
     * This field is set after {@link #init()} was called.
     */
    private boolean initiated = false;

    /**
     * This static field contains the value of {@link #toString()} of the last
     * destroyed instance of this class, see {@link #preDeytroy()}.
     */
    public static String LastDestroyed;

    @PostConstruct
    private void init() {
        initiated = true;
    }

    @GET
    public Boolean get() {
        return initiated;
    }

    @PreDestroy
    private void preDeytroy() {
        LastDestroyed = this.toString();
    }
}
