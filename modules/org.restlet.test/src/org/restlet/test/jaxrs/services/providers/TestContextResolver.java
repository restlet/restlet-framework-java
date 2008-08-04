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
package org.restlet.test.jaxrs.services.providers;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.restlet.test.jaxrs.services.others.Person;
import org.restlet.test.jaxrs.services.tests.ContextResolverTest;

/**
 * @author Stephan Koops
 * @see ContextResolverTest
 */
@Provider
public class TestContextResolver implements ContextResolver<BaseUriContext> {

    /**
     * @see javax.ws.rs.ext.ContextResolver#getContext(java.lang.Class)
     */
    public BaseUriContext getContext(Class<?> type) {
        if (Person.class.isAssignableFrom(type)) {
            return new BaseUriContext();
        }
        return null;
    }
}