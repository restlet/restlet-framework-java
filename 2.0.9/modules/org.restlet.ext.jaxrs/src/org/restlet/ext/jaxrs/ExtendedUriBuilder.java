/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.jaxrs;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.restlet.Application;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.service.MetadataService;

/**
 * This {@link UriBuilder} extension provides special help for "file"
 * extensions. They will not be removed, if path will be changed (e.g. replaced,
 * removed or matrix parameters added). For further information see
 * {@link #extension(String)}, {@link #extensionLanguage(String)} and
 * {@link #extensionMedia(String)}. You could get an instance with an
 * {@link ExtendedUriInfo}.
 * 
 * @author Stephan Koops
 */
public class ExtendedUriBuilder extends AbstractUriBuilder {

    /**
     * Create a new instance representing a relative URI initialized from a URI
     * path.
     * 
     * @param path
     *            a URI path that will be used to initialize the UriBuilder, may
     *            contain URI template parameters.
     * @return a new UriBuilder
     * @throws IllegalArgumentException
     *             if path is null
     */
    public static ExtendedUriBuilder fromPath(String path)
            throws IllegalArgumentException {
        ExtendedUriBuilder b = newInstance();
        b.replacePath(path);
        return b;
    }

    /**
     * Create a new instance representing a relative URI initialized from a root
     * resource class.
     * 
     * @param resource
     *            a root resource whose {@link javax.ws.rs.Path} value will be
     *            used to initialize the UriBuilder.
     * @return a new UriBuilder
     * @throws IllegalArgumentException
     *             if resource is not annotated with {@link javax.ws.rs.Path} or
     *             resource is null.
     */
    public static ExtendedUriBuilder fromResource(Class<?> resource)
            throws IllegalArgumentException {
        ExtendedUriBuilder b = newInstance();
        b.path(resource);
        return b;
    }

    /**
     * Create a new instance initialized from an existing URI.
     * 
     * @param uri
     *            a URI that will be used to initialize the UriBuilder, may not
     *            contain URI parameters.
     * @return a new UriBuilder
     * @throws IllegalArgumentException
     *             if uri is not a valid URI or is null
     */
    public static ExtendedUriBuilder fromUri(String uri)
            throws IllegalArgumentException {
        URI u;
        try {
            u = URI.create(uri);
        } catch (NullPointerException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
        return fromUri(u);
    }

    /**
     * Create a new instance initialized from an existing URI.
     * 
     * @param uri
     *            a URI that will be used to initialize the UriBuilder.
     * @return a new UriBuilder
     * @throws IllegalArgumentException
     *             if uri is null
     */
    public static ExtendedUriBuilder fromUri(URI uri)
            throws IllegalArgumentException {
        ExtendedUriBuilder b = newInstance();
        b.uri(uri);
        return b;
    }

    /**
     * Creates a new instance of UriBuilder.
     * 
     * @return a new instance of UriBuilder
     */
    public static ExtendedUriBuilder newInstance() {
        return new ExtendedUriBuilder();
    }

    /** The extension for the language */
    private String extensionLanguage;

    /** The extension for the media type */
    private String extensionMedia;

    /**
     * Other extensions as language and media type
     * 
     * @see #extensionLanguage
     * @see #extensionMedia
     */
    private String extensionOthers;

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder clone() {
        ExtendedUriBuilder clone = new ExtendedUriBuilder();
        super.copyInto(clone);
        clone.extension(this.getExtension());
        return clone;
    }

    /**
     * Set the extension that will be appended to the final path segment at
     * build time. An initial "." will be appended if necessary. If the final
     * path segment already contains an extension, it will be retained and the
     * supplied extension will be used as a new extension.
     * 
     * <p>
     * Note that the extension will be appended to the path component, matrix
     * and query parameters will follow any appended extension.
     * </p>
     * 
     * @param extensions
     *            the extensions to append at build time, a null value will
     *            result in no extension being appended.
     * @return the updated UriBuilder
     */
    public ExtendedUriBuilder extension(String extensions) {
        if (extensions == null) {
            this.extensionLanguage = null;
            this.extensionMedia = null;
            this.extensionOthers = null;
            return this;
        }
        MetadataService metadataService;
        metadataService = Application.getCurrent().getMetadataService();
        StringTokenizer stt = new StringTokenizer(extensions, ".");
        while (stt.hasMoreTokens()) {
            String extension = stt.nextToken();
            Metadata metadata = metadataService.getMetadata(extension);
            if (metadata instanceof Language) {
                this.extensionLanguage = extension;
            } else if (metadata instanceof MediaType) {
                this.extensionMedia = extension;
            } else {
                if (extensionOthers == null)
                    this.extensionOthers = extension;
                else
                    this.extensionOthers += "." + extension;
            }
        }
        return this;
    }

    /**
     * Appends an extension for the language ("de", "en", "fr" or whatever).
     * <code>null</code> resets the language extension.
     * 
     * @param language
     * @return this {@link ExtendedUriBuilder}
     */
    public ExtendedUriBuilder extensionLanguage(String language) {
        if (Util.startsWith(language, '.')) {
            this.extensionLanguage = language.substring(1);
        } else {
            this.extensionLanguage = language;
        }
        return this;
    }

    /**
     * Appends an extension for the media type ("html", "pdf", "gif" or
     * whatever). <code>null</code> resets the media type extension.
     * 
     * @param mediaExtension
     * @return this ExtendedUriBuilder
     */
    public ExtendedUriBuilder extensionMedia(String mediaExtension) {
        if (Util.startsWith(mediaExtension, '.')) {
            this.extensionMedia = mediaExtension.substring(1);
        } else {
            this.extensionMedia = mediaExtension;
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder fragment(String fragment) {
        super.fragment(fragment);
        return this;
    }

    /**
     * Returns the extension of the current uri, if available.
     * 
     * @return the extension for content negotiation, including a "." at the
     *         start, or <code>null</code> if no extension is available. Never
     *         returns "" or ".".
     */
    @Override
    public String getExtension() {
        StringBuilder stb = new StringBuilder();
        if (extensionOthers != null) {
            stb.append('.');
            stb.append(extensionOthers);
        }
        if (extensionLanguage != null) {
            stb.append('.');
            stb.append(extensionLanguage);
        }
        if (extensionMedia != null) {
            stb.append('.');
            stb.append(extensionMedia);
        }
        if (stb.length() == 0)
            return null;
        return stb.toString();
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder host(String host) throws IllegalArgumentException {
        super.host(host);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder matrixParam(String name, Object... values)
            throws IllegalArgumentException {
        super.matrixParam(name, values);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("rawtypes")
    public ExtendedUriBuilder path(Class resource)
            throws IllegalArgumentException {
        super.path(resource);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("rawtypes")
    public ExtendedUriBuilder path(Class resource, String methodName)
            throws IllegalArgumentException {
        super.path(resource, methodName);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder path(Method method)
            throws IllegalArgumentException {
        super.path(method);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder path(String pathToAppend)
            throws IllegalArgumentException {
        super.path(pathToAppend);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder port(int port) throws IllegalArgumentException {
        super.port(port);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder port(String port) throws IllegalArgumentException {
        super.port(port);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder queryParam(String name, Object... values)
            throws IllegalArgumentException {
        super.queryParam(name, values);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder replaceMatrix(String matrix)
            throws IllegalArgumentException {
        super.replaceMatrix(matrix);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder replaceMatrixParam(String name, Object... values)
            throws IllegalArgumentException {
        super.replaceMatrixParam(name, values);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder replacePath(String newPath) {
        if (newPath == null) {
            super.replacePath(newPath);
            this.extension(null);
            return this;
        }
        int beginLastSegment = newPath.lastIndexOf('/');
        int beginLastMatrix = newPath.indexOf(';', beginLastSegment);
        int endExt = beginLastMatrix > 0 ? beginLastMatrix : newPath.length();
        int beginExtensions = Util.indexOfBetween(newPath, '.',
                beginLastSegment, endExt);
        if (beginExtensions < 0) {
            super.replacePath(newPath);
            return this;
        }
        String extensions = newPath.substring(beginExtensions, endExt);
        StringBuilder pathStb = new StringBuilder();
        pathStb.append(newPath, 0, beginExtensions);
        pathStb.append(newPath, endExt, newPath.length());
        this.extension(extensions);
        super.replacePath(pathStb);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder replaceQuery(String query)
            throws IllegalArgumentException {
        super.replaceQuery(query);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder replaceQueryParam(String name, Object... values)
            throws IllegalArgumentException {
        super.replaceQueryParam(name, values);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder scheme(String scheme)
            throws IllegalArgumentException {
        super.scheme(scheme);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder schemeSpecificPart(String ssp)
            throws IllegalArgumentException {
        super.schemeSpecificPart(ssp);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder segment(String... segments)
            throws IllegalArgumentException {
        super.segment(segments);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder uri(URI uri) throws IllegalArgumentException {
        super.uri(uri);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public ExtendedUriBuilder userInfo(String ui) {
        super.userInfo(ui);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public URI build(Object... values) throws IllegalArgumentException,
            UriBuilderException {
        return super.build(values);
    }

    /** {@inheritDoc} */
    @Override
    public URI buildFromEncoded(Object... values)
            throws IllegalArgumentException, UriBuilderException {
        return super.buildFromEncoded(values);
    }

    /** {@inheritDoc} */
    @Override
    public URI buildFromEncodedMap(Map<String, ? extends Object> values)
            throws IllegalArgumentException, UriBuilderException {
        return super.buildFromEncodedMap(values);
    }

    /**
     * @see javax.ws.rs.core.UriBuilder#buildFromMap(java.util.Map)
     */
    @Override
    public URI buildFromMap(Map<String, ? extends Object> values)
            throws IllegalArgumentException, UriBuilderException {
        return super.buildFromMap(values);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return super.toString();
    }
}