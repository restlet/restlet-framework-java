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

package org.restlet.ext.freemarker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.logging.Logger;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.Representation;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * FreeMarker template representation. Useful for dynamic string-based
 * representations.
 * 
 * @see <a href="http://freemarker.org/">FreeMarker home page</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TemplateRepresentation extends OutputRepresentation {

    /** The template. */
    private Template template;

    /** The template's data model. */
    private Object dataModel;

    /** The logger instance to use. */
    private static Logger logger = Logger
            .getLogger(TemplateRepresentation.class.getName());

    /**
     * Constructor.
     * 
     * @param templateName
     *                The FreeMarker template's name. The full path is resolved
     *                by the configuration.
     * @param config
     *                The FreeMarker configuration.
     * @param dataModel
     *                The template's data model.
     * @param mediaType
     *                The representation's media type.
     */
    public TemplateRepresentation(String templateName, Configuration config,
            Object dataModel, MediaType mediaType) {
        this(getTemplate(templateName, config), dataModel, mediaType);
    }

    /**
     * Constructor.
     * 
     * @param templateRepresentation
     *                The FreeMarker template provided via a representation.
     * @param config
     *                The FreeMarker configuration.
     * @param dataModel
     *                The template's data model.
     * @param mediaType
     *                The representation's media type.
     */
    public TemplateRepresentation(Representation templateRepresentation,
            Configuration config, Object dataModel, MediaType mediaType) {
        this(getTemplate(templateRepresentation, config), dataModel, mediaType);
    }

    /**
     * Constructor.
     * 
     * @param template
     *                The FreeMarker template.
     * @param dataModel
     *                The template's data model.
     * @param mediaType
     *                The representation's media type.
     */
    public TemplateRepresentation(Template template, Object dataModel,
            MediaType mediaType) {
        super(mediaType);
        this.template = template;
        this.dataModel = dataModel;
    }

    /**
     * Returns a FreeMarker template from its name and a configuration.
     * 
     * @param templateName
     *                The template name.
     * @param config
     *                The FreeMarker configuration.
     * @return The template or null if not found.
     */
    private static Template getTemplate(String templateName,
            Configuration config) {
        try {
            return config.getTemplate(templateName);
        } catch (IOException e) {
            logger.warning("Unable to get the template " + templateName + ". Error message: "
                    + e.getMessage());
            return null;
        }
    }

    /**
     * Returns a FreeMarker template from a representation and a configuration.
     * 
     * @param templateRepresentation
     *                The template representation.
     * @param config
     *                The FreeMarker configuration.
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
            logger
                    .warning("Unable to get the template from the representation "
                            + templateRepresentation.getIdentifier()
                            + ". Error message: "
                            + e.getMessage());
            return null;
        }
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
     *                The template's data model.
     * @return The template's data model.
     */
    public Object setDataModel(Object dataModel) {
        this.dataModel = dataModel;
        return dataModel;
    }

    /**
     * Writes the datum as a stream of bytes.
     * 
     * @param outputStream
     *                The stream to use when writing.
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
