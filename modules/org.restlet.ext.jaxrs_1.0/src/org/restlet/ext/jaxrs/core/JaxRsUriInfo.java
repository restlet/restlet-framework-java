/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet.ext.jaxrs.core;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.restlet.data.Reference;
import org.restlet.ext.jaxrs.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.util.Util;

/**
 * Implementation of the JAX-RS interface {@link UriInfo}
 * @author Stephan Koops
 *
 */
public class JaxRsUriInfo implements UriInfo {
    private Reference reference;

    /**
     * 
     * @param reference The Restlet reference that will be wraped. 
     */
    public JaxRsUriInfo(Reference reference) {
        this.reference = reference;
    }

    /**
     * @see UriInfo#getAbsolutePath()
     */
    public URI getAbsolutePath() {
        try {
            return new URI(reference.getBaseRef().toString(false, false));
        } catch (URISyntaxException e) {
            throw Util.handleException(e);
        }
    }

    // TODO testen: siehe Javadoc UriInfo#getAbsolutePath()

    /**
     * @see UriInfo#getAbsolutePathBuilder()
     */
    public UriBuilder getAbsolutePathBuilder() {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see UriInfo#getBaseUri()
     */
    public URI getBaseUri() {
        try {
            return new URI(reference.getBaseRef().toString(false, false));
        } catch (URISyntaxException e) {
            throw Util.handleException(e);
        }
    }

    /**
     * @see UriInfo#getAbsolutePathBuilder()
     */
    public UriBuilder getBaseUriBuilder() {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see UriInfo#getPath()
     */
    public String getPath() {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see UriInfo#getPath(boolean)
     */
    public String getPath(boolean decode) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see UriInfo#getPathSegments()
     */
    public List<PathSegment> getPathSegments() {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see UriInfo#getPathSegments(boolean)
     */
    public List<PathSegment> getPathSegments(boolean decode) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see UriInfo#getQueryParameters()
     */
    public MultivaluedMap<String, String> getQueryParameters() {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see UriInfo#getQueryParameters(boolean)
     */
    public MultivaluedMap<String, String> getQueryParameters(boolean decode) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see UriInfo#getRequestUri()
     */
    public URI getRequestUri() {
        try {
            return new URI(reference.toString(false, false));
        } catch (URISyntaxException e) {
            throw Util.handleException(e);
        }
    }

    /**
     * @see UriInfo#getRequestUriBuilder()
     */
    public UriBuilder getRequestUriBuilder() {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see UriInfo#getTemplateParameters()
     */
    public MultivaluedMap<String, String> getTemplateParameters() {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see UriInfo#getTemplateParameters(boolean)
     */
    public MultivaluedMap<String, String> getTemplateParameters(boolean decode) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }
}