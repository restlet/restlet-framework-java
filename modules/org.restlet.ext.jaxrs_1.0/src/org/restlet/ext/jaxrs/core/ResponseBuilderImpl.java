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
package org.restlet.ext.jaxrs.core;

import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.restlet.data.Dimension;
import org.restlet.ext.jaxrs.util.Converter;
import org.restlet.util.Engine;

/**
 * Implementation of the {@link ResponseBuilder}
 * 
 * @author Stephan Koops
 * 
 */
public class ResponseBuilderImpl extends ResponseBuilder {

    private MultivaluedMap<String, Object> metadata;

    private Map<String, NewCookie> newCookies;

    private ResponseImpl response;

    /**
     * Creates a new Response Builder
     */
    public ResponseBuilderImpl() {
    }

    /**
     * Create a Response instance from the current ResponseBuilder. The builder
     * is reset to a blank state equivalent to calling the ok method.
     * 
     * @return a Response instance
     * @see javax.ws.rs.core.Response.ResponseBuilder#build()
     */
    @Override
    public Response build() {
        if (this.response == null)
            return new ResponseImpl();
        Response r = this.response;
        if (this.newCookies != null) {
            MultivaluedMap<String, Object> metadata = getMetadata();
            for (NewCookie cookie : this.newCookies.values())
                metadata.putSingle(HttpHeaders.SET_COOKIE, cookie);
            this.newCookies = null;
        }
        this.response = null;
        this.metadata = null;
        return r;
    }

    /**
     * Set the cache control data on the ResponseBuilder.
     * 
     * @param cacheControl
     *                the cache control directives
     * @return the updated ResponseBuilder
     * @see javax.ws.rs.core.Response.ResponseBuilder#cacheControl(javax.ws.rs.core.CacheControl)
     */
    @Override
    public ResponseBuilder cacheControl(CacheControl cacheControl) {
        if (cacheControl == null)
            getMetadata().remove(HttpHeaders.CACHE_CONTROL);
        else
            getMetadata().putSingle(HttpHeaders.CACHE_CONTROL, cacheControl);
        return this;
    }

    /**
     * @see javax.ws.rs.core.Response.ResponseBuilder#clone()
     */
    @Override
    public ResponseBuilderImpl clone() {
        ResponseBuilderImpl newRb = new ResponseBuilderImpl();
        newRb.response = this.response.clone();
        newRb.newCookies = new HashMap<String, NewCookie>(this.newCookies);
        // metadatas are read from the response.
        return newRb;
    }

    /**
     * Set the content location on the ResponseBuilder.
     * 
     * 
     * @param location
     *                the content location
     * @return the updated ResponseBuilder
     * @see javax.ws.rs.core.Response.ResponseBuilder#contentLocation(java.net.URI)
     */
    @Override
    public ResponseBuilder contentLocation(URI location) {
        if (location == null)
            getMetadata().remove(HttpHeaders.CONTENT_LOCATION);
        else
            getMetadata().putSingle(HttpHeaders.CONTENT_LOCATION,
                    location.toASCIIString());
        return this;
    }

    /**
     * Add cookies to the ResponseBuilder. If more than one cookie with the same
     * is supplied, later ones overwrite earlier ones.
     * 
     * @param cookies
     *                new cookies that will accompany the response.
     * @return the updated ResponseBuilder
     * @see javax.ws.rs.core.Response.ResponseBuilder#cookie(javax.ws.rs.core.NewCookie)
     */
    @Override
    public ResponseBuilder cookie(NewCookie... cookies) {
        Map<String, NewCookie> newCookies = getNewCookies();
        for (NewCookie cookie : cookies) {
            if (cookie != null)
                newCookies.put(cookie.getName(), cookie);
        }
        return this;
    }

    /**
     * Set the language on the ResponseBuilder. <br>
     * This method is not required by the JAX-RS API bu is used from
     * {@link #variant(Variant)}.
     * 
     * @param encoding
     *                the encoding of the response entity
     * @return the updated ResponseBuilder
     */
    public ResponseBuilder encoding(String encoding) {
        if (encoding == null)
            getMetadata().remove(HttpHeaders.CONTENT_ENCODING);
        else
            getMetadata().putSingle(HttpHeaders.CONTENT_ENCODING, encoding);
        return this;
    }

    /**
     * Set the entity on the ResponseBuilder.
     * 
     * 
     * @param entity
     *                the response entity
     * @return the updated ResponseBuilder
     * @see javax.ws.rs.core.Response.ResponseBuilder#entity(java.lang.Object)
     */
    @Override
    public ResponseBuilder entity(Object entity) {
        this.getResponse().setEntity(entity);
        return this;
    }

    MultivaluedMap<String, Object> getMetadata() {
        if (this.metadata == null)
            this.metadata = getResponse().getMetadata();
        return metadata;
    }

    Map<String, NewCookie> getNewCookies() {
        if (this.newCookies == null)
            this.newCookies = new HashMap<String, NewCookie>();
        return newCookies;
    }

    ResponseImpl getResponse() {
        if (response == null)
            this.response = new ResponseImpl();
        return response;
    }

    /**
     * Set the value of a specific header on the ResponseBuilder. If the value
     * is null, an previous set value for the given name will be removed.
     * 
     * @param name
     *                the name of the header
     * @param value
     *                the value of the header, the header will be serialized
     *                using its toString method
     * @return the updated ResponseBuilder
     * @see javax.ws.rs.core.Response.ResponseBuilder#header(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public ResponseBuilder header(String name, Object value) {
        if (name == null)
            throw new IllegalArgumentException(
                    "You must give a name of the header");
        if (name.equals(HttpHeaders.SET_COOKIE)) {
            if (value instanceof NewCookie)
                this.cookie((NewCookie) value);
            else if (value instanceof Cookie)
                this.cookie(new NewCookie((Cookie) value));
            else if (value instanceof CharSequence)
                this.cookie(NewCookie.parse(value.toString()));
            else
                throw new IllegalArgumentException(
                        "A Cookie must be of type NewCookie or String");
        } else {
            if (value == null)
                getMetadata().remove(name);
            else
                getMetadata().add(name, value);
        }
        return this;
    }

    /**
     * Set the language on the ResponseBuilder.
     * 
     * 
     * @param language
     *                the language of the response entity
     * @return the updated ResponseBuilder
     * @see javax.ws.rs.core.Response.ResponseBuilder#language(java.lang.String)
     */
    @Override
    public ResponseBuilder language(String language) {
        if (language == null)
            getMetadata().remove(HttpHeaders.CONTENT_LANGUAGE);
        else
            getMetadata().putSingle(HttpHeaders.CONTENT_LANGUAGE, language);
        return this;
    }

    /**
     * Set the last modified date on the ResponseBuilder.
     * 
     * 
     * @param lastModified
     *                the last modified date
     * @return the updated ResponseBuilder
     * @see javax.ws.rs.core.Response.ResponseBuilder#lastModified(java.util.Date)
     */
    @Override
    public ResponseBuilder lastModified(Date lastModified) {
        if (lastModified == null)
            getMetadata().remove(HttpHeaders.LAST_MODIFIED);
        else
            getMetadata().putSingle(HttpHeaders.LAST_MODIFIED, lastModified);
        return this;
    }

    /**
     * Set the location on the ResponseBuilder.
     * 
     * 
     * @param location
     *                the location
     * @return the updated ResponseBuilder
     * @see javax.ws.rs.core.Response.ResponseBuilder#location(java.net.URI)
     */
    @Override
    public ResponseBuilder location(URI location) {
        if (location == null)
            getMetadata().remove(HttpHeaders.LOCATION);
        else
            getMetadata().putSingle(HttpHeaders.LOCATION, location);
        return this;
    }

    /**
     * Set the status on the ResponseBuilder.
     * 
     * @param status
     *                the response status
     * @return the updated ResponseBuilder
     * @see javax.ws.rs.core.Response.ResponseBuilder#status(int)
     */
    @Override
    public ResponseBuilder status(int status) {
        if (this.response == null)
            this.response = new ResponseImpl(status);
        else
            this.response.setStatus(status);
        return this;
    }

    /**
     * Set the entity tag on the ResponseBuilder.
     * 
     * @param tag
     *                the entity tag
     * @return the updated ResponseBuilder
     * @see javax.ws.rs.core.Response.ResponseBuilder#tag(javax.ws.rs.core.EntityTag)
     */
    @Override
    public ResponseBuilder tag(EntityTag tag) {
        if (tag == null)
            getMetadata().remove(HttpHeaders.ETAG);
        else
            getMetadata().putSingle(HttpHeaders.ETAG, tag);
        return this;
    }

    /**
     * Set a strong entity tag on the ResponseBuilder. This is a shortcut for
     * <code>tag(new EntityTag(<i>value</i>))</code>.
     * 
     * @param tag
     *                the string content of a strong entity tag. The JAX-RS
     *                runtime will quote the supplied value when creating the
     *                header.
     * @return the updated ResponseBuilder
     * @see javax.ws.rs.core.Response.ResponseBuilder#tag(java.lang.String)
     */
    @Override
    public ResponseBuilder tag(String tag) {
        if (tag == null)
            getMetadata().remove(HttpHeaders.ETAG);
        else
            getMetadata().putSingle(HttpHeaders.ETAG, tag);
        return this;
    }

    /**
     * Set the response media type on the ResponseBuilder.
     * 
     * @see javax.ws.rs.core.Response.ResponseBuilder#type(javax.ws.rs.core.MediaType)
     */
    @Override
    public ResponseBuilder type(MediaType type) {
        if (type == null)
            getMetadata().remove(HttpHeaders.CONTENT_TYPE);
        else
            getMetadata().putSingle(HttpHeaders.CONTENT_TYPE, type);
        return this;
    }

    /**
     * Set the response media type on the ResponseBuilder.
     * 
     * @param type
     *                the media type of the response entity
     * @return the updated ResponseBuilder
     * @throws IllegalArgumentException
     *                 if type cannot be parsed
     * @see javax.ws.rs.core.Response.ResponseBuilder#type(java.lang.String)
     */
    @Override
    public ResponseBuilder type(String type) {
        return type(MediaType.parse(type));
    }

    /**
     * Set representation metadata on the ResponseBuilder.
     * 
     * @param variant
     *                metadata of the response entity
     * @return the updated ResponseBuilder
     * @see javax.ws.rs.core.Response.ResponseBuilder#variant(javax.ws.rs.core.Variant)
     */
    @Override
    public ResponseBuilder variant(Variant variant) {
        if (variant == null)
            throw new IllegalArgumentException("The variant must not be null");
        this.language(variant.getLanguage());
        this.encoding(variant.getEncoding());
        this.type(variant.getMediaType());
        return this;
    }

    /**
     * Add a Vary header that lists the available variants.
     * 
     * @param variants
     *                a list of available representation variants
     * @return the updated ResponseBuilder
     * @see javax.ws.rs.core.Response.ResponseBuilder#variants(java.util.List)
     */
    @Override
    public ResponseBuilder variants(List<Variant> variants) {

        Set<String> encodings = new HashSet<String>();
        Set<String> languages = new HashSet<String>();
        Set<MediaType> mediaTypes = new HashSet<MediaType>();
        Set<String> charsets = new HashSet<String>();
        for (Variant variant : variants) {
            String encoding = variant.getEncoding();
            if (encoding != null)
                encodings.add(encoding);
            String language = variant.getLanguage();
            if (language != null)
                languages.add(language);
            MediaType mediaType = variant.getMediaType();
            if (mediaType != null)
                mediaTypes.add(Converter.getMediaTypeWithoutParams(mediaType));
            String charset = Converter.getCharset(mediaType);
            if (charset != null)
                charsets.add(charset);
        }
        Set<Dimension> dimensions = new HashSet<Dimension>();
        if (encodings.size() > 1)
            dimensions.add(Dimension.ENCODING);
        if (languages.size() > 1)
            dimensions.add(Dimension.LANGUAGE);
        if (mediaTypes.size() > 1)
            dimensions.add(Dimension.MEDIA_TYPE);
        if (charsets.size() > 1)
            dimensions.add(Dimension.CHARACTER_SET);
        String vary = Engine.getInstance().formatDimensions(dimensions);
        if (vary != null)
            this.getMetadata().putSingle(HttpHeaders.VARY, vary);
        return this;
    }
}