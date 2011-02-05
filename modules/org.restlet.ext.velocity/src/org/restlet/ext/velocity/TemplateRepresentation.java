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

package org.restlet.ext.velocity;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.util.Resolver;

/**
 * Velocity template representation. Useful for dynamic string-based
 * representations.
 * 
 * @see <a href="http://velocity.apache.org/">Velocity home page</a>
 * @author Jerome Louvel
 */
public class TemplateRepresentation extends WriterRepresentation {
    /**
     * Velocity context based on a Resolver.
     * 
     * @see Resolver
     */
    private class ResolverContext implements
            org.apache.velocity.context.Context {
        /** The inner resolver instance. */
        private final Resolver<? extends Object> resolver;

        /**
         * Constructor.
         * 
         * @param resolver
         *            The resolver.
         */
        public ResolverContext(Resolver<? extends Object> resolver) {
            super();
            this.resolver = resolver;
        }

        /**
         * Indicates whether the specified key is in the context.
         * 
         * @param key
         *            The key to look for.
         * @Return True if the key is in the context, false otherwise.
         */
        public boolean containsKey(Object key) {
            return this.resolver.resolve((String) key) != null;
        }

        /**
         * Gets the value corresponding to the provided key from the context.
         * 
         * @Param key The name of the desired value.
         * @Return The value corresponding to the provided key.
         */
        public Object get(String key) {
            return this.resolver.resolve(key);
        }

        /**
         * Returns null since a resolver does not know by advance the whole
         * values.
         * 
         * @Return null.
         */
        public Object[] getKeys() {
            return null;
        }

        /**
         * Returns null since a resolver as a data model cannot be updated.
         * 
         * @param key
         *            The name to key the provided value with.
         * @param value
         *            The corresponding value.
         * @return null.
         */
        public Object put(String key, Object value) {
            return null;
        }

        /**
         * Does nothing since resolver as a data model cannot be updated.
         * 
         * @param value
         *            The name of the value to remove.
         * @return null.
         */
        public Object remove(Object value) {
            return null;
        }

    }

    /** The template's data model. */
    private volatile org.apache.velocity.context.Context context;

    /** The Velocity engine. */
    private volatile VelocityEngine engine;

    /** The template. */
    private volatile Template template;

    /** The template name. */
    private volatile String templateName;

    /**
     * Constructor based on a Velocity 'encoded' representation.
     * 
     * @param templateRepresentation
     *            The representation to 'decode'.
     * @param dataModel
     *            The Velocity template's data model.
     * @param mediaType
     *            The representation's media type.
     * @throws IOException
     * @throws ParseErrorException
     * @throws ResourceNotFoundException
     */
    public TemplateRepresentation(Representation templateRepresentation,
            Map<String, Object> dataModel, MediaType mediaType)
            throws ResourceNotFoundException, ParseErrorException, IOException {
        super(mediaType);
        setDataModel(dataModel);
        this.engine = null;
        this.template = new Template();

        CharacterSet charSet = (templateRepresentation.getCharacterSet() == null) ? templateRepresentation
                .getCharacterSet()
                : CharacterSet.DEFAULT;
        this.template.setEncoding(charSet.getName());
        if (templateRepresentation.getModificationDate() != null) {
            this.template.setLastModified(templateRepresentation
                    .getModificationDate().getTime());
        }
        this.template.setName("org.restlet.resource.representation");
        this.template.setRuntimeServices(RuntimeSingleton.getRuntimeServices());
        this.template.setResourceLoader(new RepresentationResourceLoader(
                templateRepresentation));
        this.template.process();
        this.templateName = null;
    }

    /**
     * Constructor based on a Velocity 'encoded' representation.
     * 
     * @param templateRepresentation
     *            The representation to 'decode'.
     * @param mediaType
     *            The representation's media type.
     * @throws IOException
     * @throws ParseErrorException
     * @throws ResourceNotFoundException
     */
    public TemplateRepresentation(Representation templateRepresentation,
            MediaType mediaType) throws ResourceNotFoundException,
            ParseErrorException, IOException {
        super(mediaType);
        this.engine = null;
        this.template = new Template();

        CharacterSet charSet = (templateRepresentation.getCharacterSet() == null) ? templateRepresentation
                .getCharacterSet()
                : CharacterSet.DEFAULT;
        this.template.setEncoding(charSet.getName());
        this.template.setLastModified((templateRepresentation
                .getModificationDate() == null) ? new Date().getTime()
                : templateRepresentation.getModificationDate().getTime());
        this.template.setName("org.restlet.resource.representation");
        this.template.setRuntimeServices(RuntimeSingleton.getRuntimeServices());
        this.template.setResourceLoader(new RepresentationResourceLoader(
                templateRepresentation));
        this.template.process();
        this.templateName = null;
    }

    /**
     * Constructor.
     * 
     * @param templateName
     *            The Velocity template's name. The actual template is retrieved
     *            using the Velocity configuration.
     * @param dataModel
     *            The Velocity template's data model.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(String templateName,
            Map<String, Object> dataModel, MediaType mediaType) {
        super(mediaType);

        try {
            setDataModel(dataModel);
            this.engine = new VelocityEngine();
            this.template = null;
            this.templateName = templateName;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor.
     * 
     * @param templateName
     *            The Velocity template's name. The full path is resolved by the
     *            configuration.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(String templateName, MediaType mediaType) {
        this(templateName, new TreeMap<String, Object>(), mediaType);
    }

    /**
     * Constructor.
     * 
     * @param template
     *            The Velocity template.
     * @param dataModel
     *            The Velocity template's data model.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(Template template,
            Map<String, Object> dataModel, MediaType mediaType) {
        super(mediaType);
        setDataModel(dataModel);
        this.engine = null;
        this.template = template;
        this.templateName = null;
    }

    /**
     * Constructor.
     * 
     * @param template
     *            The Velocity template.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(Template template, MediaType mediaType) {
        super(mediaType);
        this.engine = null;
        this.template = template;
        this.templateName = null;
    }

    /**
     * Returns the Velocity context.
     * 
     * @return The Velocity context.
     */
    private org.apache.velocity.context.Context getContext() {
        return this.context;
    }

    /**
     * Returns the Velocity engine.
     * 
     * @return The Velocity engine.
     */
    public VelocityEngine getEngine() {
        return this.engine;
    }

    /**
     * Returns the Velocity template.
     * 
     * @return The Velocity template.
     */
    public Template getTemplate() {
        if (this.template == null) {
            if (this.templateName != null) {
                try {
                    getEngine().init();
                    this.template = getEngine().getTemplate(this.templateName);
                } catch (Exception e) {
                    final Context context = Context.getCurrent();

                    if (context != null) {
                        context.getLogger().log(Level.WARNING,
                                "Unable to get template", e);
                    }
                }
            }
        }

        return this.template;
    }

    /**
     * Sets the Velocity context.
     * 
     * @param context
     *            The Velocity context
     */
    private void setContext(org.apache.velocity.context.Context context) {
        this.context = context;
    }

    /**
     * Sets the template's data model.
     * 
     * @param dataModel
     *            The template's data model.
     */
    public void setDataModel(Map<String, Object> dataModel) {
        setContext(new VelocityContext(dataModel));
    }

    /**
     * Sets the template's data model from a request/response pair. This default
     * implementation uses a Resolver.
     * 
     * @see Resolver
     * @see Resolver#createResolver(Request, Response)
     * 
     * @param request
     *            The request where data are located.
     * @param response
     *            The response where data are located.
     */
    public void setDataModel(Request request, Response response) {
        setContext(new ResolverContext(Resolver.createResolver(request,
                response)));
    }

    /**
     * Sets the template's data model from a resolver.
     * 
     * @param resolver
     *            The resolver.
     */
    public void setDataModel(Resolver<Object> resolver) {
        setContext(new ResolverContext(resolver));
    }

    /**
     * Writes the datum as a stream of characters.
     * 
     * @param writer
     *            The writer to use when writing.
     */
    @Override
    public void write(Writer writer) throws IOException {
        try {
            // Load the template
            // Process the template
            getTemplate().merge(getContext(), writer);
        } catch (Exception e) {
            final Context context = Context.getCurrent();

            if (context != null) {
                context.getLogger().log(Level.WARNING,
                        "Unable to process the template", e);
            }

            e.printStackTrace();

            throw new IOException("Template processing error. "
                    + e.getMessage());
        }
    }

}
