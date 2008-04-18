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

package org.restlet.ext.velocity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.TreeMap;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;
import org.restlet.resource.Representation;

/**
 * Velocity template representation. Useful for dynamic string-based
 * representations.
 * 
 * @see <a href="http://velocity.apache.org/">Velocity home page</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TemplateRepresentation extends OutputRepresentation {
    /** The template's data model. */
    private volatile Map<String, Object> dataModel;

    /** The Velocity engine. */
    private volatile VelocityEngine engine;

    /** The template. */
    private volatile Template template;

    public TemplateRepresentation(Representation templateRepresentation,
            Map<String, Object> dataModel, MediaType mediaType) {
        super(mediaType);
        this.dataModel = dataModel;
        this.engine = null;
        this.template = new Template();
        this.template.setEncoding(templateRepresentation.getEncodings().get(0)
                .getName());
        this.template.setLastModified(templateRepresentation
                .getModificationDate().getTime());
        this.template.setName("org.restlet.resource.representation");
        this.template.setResourceLoader(new RepresentationResourceLoader(
                templateRepresentation));
    }

    /**
     * Constructor.
     * 
     * @param templateName
     *                The Velocity template's name. The actual template is
     *                retrieved using the Velocity configuration.
     * @param dataModel
     *                The Velocity template's data model.
     * @param mediaType
     *                The representation's media type.
     */
    public TemplateRepresentation(String templateName,
            Map<String, Object> dataModel, MediaType mediaType) {
        super(mediaType);

        try {
            this.dataModel = dataModel;
            this.engine = new VelocityEngine();
            getEngine().init();
            this.template = getEngine().getTemplate(templateName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor.
     * 
     * @param templateName
     *                The Velocity template's name. The full path is resolved by
     *                the configuration.
     * @param mediaType
     *                The representation's media type.
     */
    public TemplateRepresentation(String templateName, MediaType mediaType) {
        this(templateName, new TreeMap<String, Object>(), mediaType);
    }

    /**
     * Constructor.
     * 
     * @param template
     *                The Velocity template.
     * @param dataModel
     *                The Velocity template's data model.
     * @param mediaType
     *                The representation's media type.
     */
    public TemplateRepresentation(Template template,
            Map<String, Object> dataModel, MediaType mediaType) {
        super(mediaType);
        this.dataModel = dataModel;
        this.template = template;
    }

    /**
     * Returns the template's data model.
     * 
     * @return The template's data model.
     */
    public Map<String, Object> getDataModel() {
        return this.dataModel;
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
        return this.template;
    }

    /**
     * Sets the template's data model.
     * 
     * @param dataModel
     *                The template's data model.
     * @return The template's data model.
     */
    public Map<String, Object> setDataModel(Map<String, Object> dataModel) {
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
            // Create the context
            VelocityContext context = new VelocityContext(getDataModel());

            // Load the template
            if (getCharacterSet() != null) {
                tmplWriter = new BufferedWriter(new OutputStreamWriter(
                        outputStream, getCharacterSet().getName()));
            } else {
                tmplWriter = new BufferedWriter(new OutputStreamWriter(
                        outputStream, getTemplate().getEncoding()));
            }

            // Process the template
            getTemplate().merge(context, tmplWriter);
            tmplWriter.flush();
        } catch (Exception e) {
            throw new IOException("Template processing error. "
                    + e.getMessage());
        }
    }

}
