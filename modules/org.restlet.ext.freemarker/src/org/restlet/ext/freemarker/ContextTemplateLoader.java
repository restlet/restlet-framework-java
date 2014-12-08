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

package org.restlet.ext.freemarker;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;

/**
 * FreeMarker template loader based on a Context's client dispatcher. You can
 * set an instance on a FreeMarker configuration via the
 * {@link Configuration#setTemplateLoader(TemplateLoader)} method.
 * 
 * @author Jerome Louvel
 */
public class ContextTemplateLoader implements TemplateLoader {

    /** The base URI. */
    private final String baseUri;

    /** The Restlet context. */
    private final Context context;

    /**
     * Constructor.
     * 
     * @param context
     *            The Restlet context.
     * @param baseRef
     *            The base reference.
     */
    public ContextTemplateLoader(Context context, Reference baseRef) {
        this(context, baseRef.toString());
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The Restlet context.
     * @param baseUri
     *            The base URI.
     */
    public ContextTemplateLoader(Context context, String baseUri) {
        this.context = context;
        this.baseUri = baseUri;
    }

    /**
     * Close the template source.
     * 
     * @param templateSource
     *            The template source {@link Representation}.
     */
    public void closeTemplateSource(Object templateSource) throws IOException {
        if (templateSource instanceof Representation) {
            ((Representation) templateSource).release();
        }
    }

    /**
     * Finds the object that acts as the source of the template with the given
     * name.
     * 
     * @param name
     *            The template name.
     * @return The template source {@link Representation}.
     */
    public Object findTemplateSource(String name) throws IOException {
        String fullUri;

        if (getBaseUri().endsWith("/")) {
            fullUri = getBaseUri() + name;
        } else {
            fullUri = getBaseUri() + "/" + name;
        }

        return (getContext() == null) ? null : getContext()
                .getClientDispatcher().handle(new Request(Method.GET, fullUri))
                .getEntity();
    }

    /**
     * Returns the base URI.
     * 
     * @return The base URI.
     */
    private String getBaseUri() {
        return baseUri;
    }

    /**
     * Returns the Restlet context.
     * 
     * @return The Restlet context.
     */
    private Context getContext() {
        return context;
    }

    /**
     * Returns the modification time.
     * 
     * @param templateSource
     *            The template source {@link Representation}.
     * @return The modification time.
     */
    public long getLastModified(Object templateSource) {
        Date lastModified = ((Representation) templateSource)
                .getModificationDate();
        return (lastModified == null) ? -1L : lastModified.getTime();
    }

    /**
     * Returns the reader for the template source.
     * 
     * @param templateSource
     *            The template source {@link Representation}.
     * @param characterSet
     *            The reader character set.
     */
    public Reader getReader(Object templateSource, String characterSet)
            throws IOException {
        Representation r = (Representation) templateSource;
        return new InputStreamReader(r.getStream(), characterSet);
    }

}
