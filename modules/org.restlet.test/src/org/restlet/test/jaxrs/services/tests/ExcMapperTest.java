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
package org.restlet.test.jaxrs.services.tests;

import java.util.Collections;
import java.util.Set;

import org.restlet.data.MediaType;
import org.restlet.data.Response;
import org.restlet.test.jaxrs.services.providers.IllegalArgExcMapper;
import org.restlet.test.jaxrs.services.resources.ExcMapperTestResource;

/**
 * @author Stephan Koops
 * @see ExcMapperTestResource
 * @see IllegalArgExcMapper
 */
public class ExcMapperTest extends JaxRsTestCase {

    /**
     * @param accMediaType
     * @param expMediaType
     */
    private void check(MediaType accMediaType, MediaType expMediaType) {
        final Response response = get(accMediaType);
        sysOutEntityIfError(response);
        assertEquals(IllegalArgExcMapper.STATUS, response.getStatus().getCode());
        assertEqualMediaType(expMediaType, response);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Set<Class<?>> getProvClasses() {
        return (Set) Collections.singleton(IllegalArgExcMapper.class);
    }

    @Override
    protected Class<?> getRootResourceClass() {
        return ExcMapperTestResource.class;
    }

    public void testHtml() {
        check(MediaType.TEXT_HTML, MediaType.TEXT_HTML);
    }

    public void testImage() {
        check(MediaType.IMAGE_BMP, MediaType.TEXT_PLAIN);
    }

    public void testPlain() {
        check(MediaType.TEXT_PLAIN, MediaType.TEXT_PLAIN);
    }

    public void testXml() {
        check(MediaType.TEXT_XML, MediaType.TEXT_PLAIN);
    }
}