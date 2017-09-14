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

package org.restlet.test.ext.jaxrs.services.tests;

import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.test.ext.jaxrs.services.resources.Issue594Resources;

/**
 * @author Stephan Koops
 * @see Issue594Resources
 */
public class Issue594Test extends JaxRsTestCase {

    @Override
    protected Application getApplication() {
        return new Application() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Class<?>> getClasses() {
                return (Set) Collections.singleton(Issue594Resources.class);
            }
        };
    }

    /**
     * This tests, if a sub resource class of a sub resource class of a root
     * resource class is accessable.
     * 
     * @throws Exception
     */
    public void testGetRoot() throws Exception {
        final Response response = get();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        final Representation entity = response.getEntity();
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
        assertEquals("root", entity.getText());
    }

    public void testGetRepository() throws Exception {
        final Response response = get("PRJ");
        final Representation entity = response.getEntity();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
        assertEquals("project=PRJ", entity.getText());
    }

    public void testGetProject() throws Exception {
        final Response response = get("PRJ/REPO");
        final Representation entity = response.getEntity();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
        assertEquals("project=PRJ\nrepository=REPO", entity.getText());
    }

    public void testGetSchemaDir() throws Exception {
        final Response response = get("PRJ/REPO/schema");
        final Representation entity = response.getEntity();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
        assertEquals("project=PRJ\nrepository=REPO\nschema", entity.getText());
    }

    public void testGetSchema() throws Exception {
        final Response response = get("PRJ/REPO/schema/SCM");
        final Representation entity = response.getEntity();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
        assertEquals("project=PRJ\nrepository=REPO\nschema\nschema=SCM",
                entity.getText());
    }

    public void testFooBarSimple() throws Exception {
        final Response response = get("/foo/bar/schema/simple");
        final Representation entity = response.getEntity();
        sysOutEntityIfError(response);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEqualMediaType(MediaType.TEXT_PLAIN, entity.getMediaType());
        assertEquals("project=foo\nrepository=bar\nschema\nschema=simple",
                entity.getText());
    }
}
