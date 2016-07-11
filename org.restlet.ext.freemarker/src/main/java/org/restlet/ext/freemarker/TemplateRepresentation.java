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
import java.io.Writer;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.internal.ResolverHashModel;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.util.Resolver;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * FreeMarker template representation. Useful for dynamic string-based
 * representations.
 * 
 * @see <a href="http://freemarker.org/">FreeMarker home page</a>
 * @author Jerome Louvel
 */
public class TemplateRepresentation extends WriterRepresentation {

    /**
     * Returns a FreeMarker template from a representation and a configuration.
     * 
     * @param config
     *            The FreeMarker configuration.
     * @param templateRepresentation
     *            The template representation.
     * @return The template or null if not found.
     */
    public static Template getTemplate(Configuration config,
            Representation templateRepresentation) {
        try {
            // Instantiate the template with the character set of the template
            // representation if it has been set, otherwise use UTF-8.
            if (templateRepresentation.getCharacterSet() != null) {
                return new Template("template",
                        templateRepresentation.getReader(), config,
                        templateRepresentation.getCharacterSet().getName());
            }

            return new Template("template", templateRepresentation.getReader(),
                    config, CharacterSet.UTF_8.getName());
        } catch (IOException e) {
            Context.getCurrentLogger().warning(
                    "Unable to get the template from the representation "
                            + templateRepresentation.getLocationRef()
                            + ". Error message: " + e.getMessage());
            return null;
        }
    }

    /**
     * Returns a FreeMarker template from its name and a configuration.
     * 
     * @param config
     *            The FreeMarker configuration.
     * @param templateName
     *            The template name.
     * @return The template or null if not found.
     */
    public static Template getTemplate(Configuration config, String templateName) {
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

    /** The FreeMarker template. */
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
        this(getTemplate(config, templateRepresentation), mediaType);
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
        this(getTemplate(config, templateRepresentation), dataModel, mediaType);
    }

    /**
     * Constructor.
     * 
     * @param templateRepresentation
     *            The FreeMarker template provided via a representation.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(Representation templateRepresentation,
            MediaType mediaType) {
        this(templateRepresentation, new Configuration(), mediaType);
    }

    /**
     * Constructor. Uses a default FreeMarker configuration.
     * 
     * @param templateRepresentation
     *            The FreeMarker template provided via a representation.
     * @param dataModel
     *            The template's data model.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(Representation templateRepresentation,
            Object dataModel, MediaType mediaType) {
        this(templateRepresentation, new Configuration(), dataModel, mediaType);
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
        this(getTemplate(config, templateName), mediaType);
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
        this(getTemplate(config, templateName), dataModel, mediaType);
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
     * Returns the FreeMarker template.
     * 
     * @return The FreeMarker template.
     */
    public Template getTemplate() {
        return template;
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
     * Sets the FreeMarker template.
     * 
     * @param template
     *            The FreeMarker template.
     */
    public void setTemplate(Template template) {
        this.template = template;
    }

    /**
     * Writes the datum as a stream of characters.
     * 
     * @param writer
     *            The writer to use when writing.
     */
    @Override
    public void write(Writer writer) throws IOException {
        if (this.template != null) {
            try {
                this.template.process(getDataModel(), writer);
            } catch (TemplateException te) {
                throw new IOException("Template processing error "
                        + te.getMessage());
            }
        } else {
            Context.getCurrentLogger()
                    .warning(
                            "Unable to write the template representation. No template found.");
        }
    }

}
