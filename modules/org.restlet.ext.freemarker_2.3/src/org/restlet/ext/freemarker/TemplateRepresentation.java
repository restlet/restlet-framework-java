/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.freemarker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.Representation;
import org.restlet.util.Resolver;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateScalarModel;

/**
 * FreeMarker template representation. Useful for dynamic string-based
 * representations.
 * 
 * @see <a href="http://freemarker.org/">FreeMarker home page</a>
 * @author Jerome Louvel
 */
public class TemplateRepresentation extends OutputRepresentation {

    /**
     * Template Hash Model based on a Resolver instance.
     */
    private class ResolverHashModel implements TemplateHashModel {
        /** The inner resolver instance. */
        private final Resolver<? extends Object> resolver;

        /**
         * Constructor.
         * 
         * @param resolver
         *            The inner resolver.
         */
        public ResolverHashModel(Resolver<? extends Object> resolver) {
            super();
            this.resolver = resolver;
        }

        /**
         * Returns a scalar model based on the value returned by the resolver
         * according to the key.
         */
        public TemplateModel get(String key) throws TemplateModelException {
            return new ScalarModel(this.resolver.resolve(key));
        }

        /**
         * Returns false.
         * 
         * @Return False.
         */
        public boolean isEmpty() throws TemplateModelException {
            return false;
        }
    }

    /**
     * Data model that gives access to a Object value.
     * 
     */
    private class ScalarModel implements TemplateScalarModel {
        /** The inner value. */
        private final Object value;

        /**
         * Constructor.
         * 
         * @param value
         *            the provided value of this scalar model.
         */
        public ScalarModel(Object value) {
            super();
            this.value = value;
        }

        public String getAsString() throws TemplateModelException {
            return this.value.toString();
        }
    }

    /**
     * Returns a FreeMarker template from a representation and a configuration.
     * 
     * @param templateRepresentation
     *            The template representation.
     * @param config
     *            The FreeMarker configuration.
     * @return The template or null if not found.
     */
    private static Template getTemplate(Representation templateRepresentation,
            Configuration config) {
        try {
            // Instantiate the template with the character set of the template
            // representation if it has been set, otherwise use UTF-8.
            if (templateRepresentation.getCharacterSet() != null) {
                return new Template("template", templateRepresentation
                        .getReader(), config, templateRepresentation
                        .getCharacterSet().getName());
            } else {
                return new Template("template", templateRepresentation
                        .getReader(), config, CharacterSet.UTF_8.getName());
            }
        } catch (IOException e) {
            Context.getCurrentLogger().warning(
                    "Unable to get the template from the representation "
                            + templateRepresentation.getIdentifier()
                            + ". Error message: " + e.getMessage());
            return null;
        }
    }

    /**
     * Returns a FreeMarker template from its name and a configuration.
     * 
     * @param templateName
     *            The template name.
     * @param config
     *            The FreeMarker configuration.
     * @return The template or null if not found.
     */
    private static Template getTemplate(String templateName,
            Configuration config) {
        try {
            return config.getTemplate(templateName);
        } catch (IOException e) {
            Context.getCurrentLogger().warning(
                    "Unable to get the template " + templateName
                            + ". Error message: " + e.getMessage());
            return null;
        }
    }

    /** The template's data model. */
    private volatile Object dataModel;

    /** The template. */
    private volatile Template template;

    /**
     * Constructor.
     * 
     * @param templateRepresentation
     *            The FreeMarker template provided via a representation.
     * @param config
     *            The FreeMarker configuration.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(Representation templateRepresentation,
            Configuration config, MediaType mediaType) {
        this(getTemplate(templateRepresentation, config), mediaType);
    }

    /**
     * Constructor.
     * 
     * @param templateRepresentation
     *            The FreeMarker template provided via a representation.
     * @param config
     *            The FreeMarker configuration.
     * @param dataModel
     *            The template's data model.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(Representation templateRepresentation,
            Configuration config, Object dataModel, MediaType mediaType) {
        this(getTemplate(templateRepresentation, config), dataModel, mediaType);
    }

    /**
     * Constructor.
     * 
     * @param templateName
     *            The FreeMarker template's name. The full path is resolved by
     *            the configuration.
     * @param config
     *            The FreeMarker configuration.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(String templateName, Configuration config,
            MediaType mediaType) {
        this(getTemplate(templateName, config), mediaType);
    }

    /**
     * Constructor.
     * 
     * @param templateName
     *            The FreeMarker template's name. The full path is resolved by
     *            the configuration.
     * @param config
     *            The FreeMarker configuration.
     * @param dataModel
     *            The template's data model.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(String templateName, Configuration config,
            Object dataModel, MediaType mediaType) {
        this(getTemplate(templateName, config), dataModel, mediaType);
    }

    /**
     * Constructor.
     * 
     * @param template
     *            The FreeMarker template.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(Template template, MediaType mediaType) {
        super(mediaType);
        this.template = template;
    }

    /**
     * Constructor.
     * 
     * @param template
     *            The FreeMarker template.
     * @param dataModel
     *            The template's data model.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(Template template, Object dataModel,
            MediaType mediaType) {
        super(mediaType);
        this.template = template;
        this.dataModel = dataModel;
    }

    /**
     * Returns the template's data model.
     * 
     * @return The template's data model.
     */
    public Object getDataModel() {
        return this.dataModel;
    }

    /**
     * Sets the template's data model.
     * 
     * @param dataModel
     *            The template's data model.
     * @return The template's data model.
     */
    public Object setDataModel(Object dataModel) {
        this.dataModel = dataModel;
        return dataModel;
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
     * @return The template's data model.
     */
    public Object setDataModel(Request request, Response response) {
        this.dataModel = new ResolverHashModel(Resolver.createResolver(request,
                response));
        return this.dataModel;
    }

    /**
     * Sets the template's data model from a resolver.
     * 
     * @param resolver
     *            The resolver.
     * @return The template's data model.
     */
    public Object setDataModel(Resolver<Object> resolver) {
        this.dataModel = new ResolverHashModel(resolver);
        return this.dataModel;
    }

    /**
     * Writes the datum as a stream of bytes.
     * 
     * @param outputStream
     *            The stream to use when writing.
     */
    @Override
    public void write(OutputStream outputStream) throws IOException {
        Writer tmplWriter = null;

        try {
            if (getCharacterSet() != null) {
                tmplWriter = new BufferedWriter(new OutputStreamWriter(
                        outputStream, getCharacterSet().getName()));
            } else {
                tmplWriter = new BufferedWriter(new OutputStreamWriter(
                        outputStream, this.template.getEncoding()));
            }

            this.template.process(getDataModel(), tmplWriter);
            tmplWriter.flush();
        } catch (TemplateException te) {
            throw new IOException("Template processing error "
                    + te.getMessage());
        }
    }

}
