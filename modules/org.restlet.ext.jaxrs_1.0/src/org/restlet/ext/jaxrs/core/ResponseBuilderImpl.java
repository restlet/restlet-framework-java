/*
 * Copyright 2005-2007 Noelios Consulting.
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

import java.net.URI;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.ext.jaxrs.todo.NotYetImplementedException;

/**
 * @author Stephan Koops
 *
 */
public class ResponseBuilderImpl extends ResponseBuilder {

    /**
     * 
     */
    public ResponseBuilderImpl() {
        // TODO Auto-generated constructor stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#build()
     */
    @Override
    public Response build() {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#cacheControl(javax.ws.rs.core.CacheControl)
     */
    @Override
    public ResponseBuilder cacheControl(CacheControl cacheControl) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#contentLocation(java.net.URI)
     */
    @Override
    public ResponseBuilder contentLocation(URI location) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#cookie(javax.ws.rs.core.NewCookie)
     */
    @Override
    public ResponseBuilder cookie(NewCookie cookie) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#entity(java.lang.Object)
     */
    @Override
    public ResponseBuilder entity(Object entity) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#header(java.lang.String, java.lang.Object)
     */
    @Override
    public ResponseBuilder header(String name, Object value) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#language(java.lang.String)
     */
    @Override
    public ResponseBuilder language(String language) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#lastModified(java.util.Date)
     */
    @Override
    public ResponseBuilder lastModified(Date lastModified) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
   }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#location(java.net.URI)
     */
    @Override
    public ResponseBuilder location(URI location) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
 }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#status(int)
     */
    @Override
    public ResponseBuilder status(int status) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
 }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#tag(javax.ws.rs.core.EntityTag)
     */
    @Override
    public ResponseBuilder tag(EntityTag tag) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
 }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#tag(java.lang.String)
     */
    @Override
    public ResponseBuilder tag(String tag) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
 }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#type(javax.ws.rs.core.MediaType)
     */
    @Override
    public ResponseBuilder type(MediaType type) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
 }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#type(java.lang.String)
     */
    @Override
    public ResponseBuilder type(String type) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
 }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#variant(javax.ws.rs.core.Variant)
     */
    @Override
    public ResponseBuilder variant(Variant variant) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
 }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#variants(java.util.List)
     */
    @Override
    public ResponseBuilder variants(List<Variant> variants) {
        // TODO Auto-generated method stub
        throw new NotYetImplementedException();
    }
}