/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
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

/**
 * Velocity template representation. Useful for dynamic string-based
 * representations.
 * 
 * @see <a href="http://velocity.apache.org/">Velocity home page</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class TemplateRepresentation extends OutputRepresentation {
    /** The template's name. */
    private String templateName;

    /** The Velocity engine. */
    private VelocityEngine engine;

    /** The template's data model. */
    private Map<String, Object> dataModel;

    /**
     * Constructor.
     * 
     * @param templateName
     *            The Velocity template's name. The full path is resolved by
     *            the configuration.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(String templateName, MediaType mediaType) {
        this(templateName, new TreeMap<String, Object>(), mediaType);
    }

    /**
     * Constructor.
     * 
     * @param templateName
     *            The Velocity template's name. The full path is resolved by
     *            the configuration.
     * @param dataModel
     *            The Velocity template's data model.
     * @param mediaType
     *            The representation's media type.
     */
    public TemplateRepresentation(String templateName,
            Map<String, Object> dataModel, MediaType mediaType) {
        super(mediaType);
        this.engine = new VelocityEngine();
        this.dataModel = dataModel;
        this.templateName = templateName;
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
     * Returns the template's data model.
     * 
     * @return The template's data model.
     */
    public Map<String, Object> getDataModel() {
        return this.dataModel;
    }

    /**
     * Sets the template's data model.
     * 
     * @param dataModel
     *            The template's data model.
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
     *            The stream to use when writing.
     */
	@Override
    public void write(OutputStream outputStream) throws IOException {
        Writer tmplWriter = null;

        try {
            // Initialize the engine
            getEngine().init();

            // Create the context
            VelocityContext context = new VelocityContext(getDataModel());

            // Load the template
            Template template = engine.getTemplate(templateName);
            if (getCharacterSet() != null) {
                tmplWriter = new BufferedWriter(new OutputStreamWriter(
                        outputStream, getCharacterSet().getName()));
            } else {
                tmplWriter = new BufferedWriter(new OutputStreamWriter(
                        outputStream, template.getEncoding()));
            }

            // Process the template
            template.merge(context, tmplWriter);
            tmplWriter.flush();
        } catch (Exception e) {
            throw new IOException("Template processing error. "
                    + e.getMessage());
        }
    }

}
