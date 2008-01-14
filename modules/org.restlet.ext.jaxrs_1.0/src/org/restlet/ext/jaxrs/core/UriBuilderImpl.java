/*
 * Copyright 2005-2008 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */
package org.restlet.ext.jaxrs.core;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.restlet.ext.jaxrs.todo.NotYetImplementedException;

/**
 * @author Stephan Koops
 *
 */
public class UriBuilderImpl extends UriBuilder {

    /**
     * 
     */
    public UriBuilderImpl() {
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#build()
     */
    @Override
    public URI build() throws UriBuilderException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#build(java.util.Map)
     */
    @Override
    public URI build(Map<String, String> values)
            throws IllegalArgumentException, UriBuilderException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#build(java.lang.String[])
     */
    @Override
    public URI build(String... values) throws IllegalArgumentException,
            UriBuilderException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#clone()
     */
    @Override
    public UriBuilder clone() {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#encode(boolean)
     */
    @Override
    public UriBuilder encode(boolean enable) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#fragment(java.lang.String)
     */
    @Override
    public UriBuilder fragment(String fragment) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#host(java.lang.String)
     */
    @Override
    public UriBuilder host(String host) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#matrixParam(java.lang.String, java.lang.String)
     */
    @Override
    public UriBuilder matrixParam(String name, String value)
            throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#path(java.lang.String[])
     */
    @Override
    public UriBuilder path(String... segments) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#path(java.lang.Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public UriBuilder path(Class resource) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#path(java.lang.reflect.Method[])
     */
    @Override
    public UriBuilder path(Method... methods) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#path(java.lang.Class, java.lang.String)
     */
    @Override
    @SuppressWarnings("unchecked")
    public UriBuilder path(Class resource, String method)
            throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#port(int)
     */
    @Override
    public UriBuilder port(int port) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#queryParam(java.lang.String, java.lang.String)
     */
    @Override
    public UriBuilder queryParam(String name, String value)
            throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#replaceMatrixParams(java.lang.String)
     */
    @Override
    public UriBuilder replaceMatrixParams(String matrix)
            throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#replacePath(java.lang.String)
     */
    @Override
    public UriBuilder replacePath(String path) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#replaceQueryParams(java.lang.String)
     */
    @Override
    public UriBuilder replaceQueryParams(String query)
            throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#scheme(java.lang.String)
     */
    @Override
    public UriBuilder scheme(String scheme) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#schemeSpecificPart(java.lang.String)
     */
    @Override
    public UriBuilder schemeSpecificPart(String ssp)
            throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#uri(java.net.URI)
     */
    @Override
    public UriBuilder uri(URI uri) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#userInfo(java.lang.String)
     */
    @Override
    public UriBuilder userInfo(String ui) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }
}