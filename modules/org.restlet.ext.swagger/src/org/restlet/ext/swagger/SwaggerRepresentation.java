/**
 * Copyright 2005-2013 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.swagger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;

import com.wordnik.swagger.core.Documentation;

/**
 * Root of a Swagger description document.
 * 
 * @author Thierry Boileau
 */
public class SwaggerRepresentation extends Representation {

    /** The Swagger documentation. */
    private Documentation documentation;

    /**
     * Constructor.
     */
    public SwaggerRepresentation() {
        super(MediaType.APPLICATION_JSON);
    }

    /**
     * Constructor.
     * 
     * @param documentation
     *            The Swagger documentation.
     */
    public SwaggerRepresentation(Documentation documentation) {
        super(MediaType.APPLICATION_JSON);
        this.documentation = documentation;
    }

    /**
     * Constructor.
     * 
     * @param xmlRepresentation
     *            The XML WADL document.
     * @throws IOException
     */
    public SwaggerRepresentation(Representation xmlRepresentation)
            throws IOException {
        super(MediaType.APPLICATION_JSON);
        setMediaType(MediaType.APPLICATION_WADL);
    }

    /**
     * Returns the Swagger documentation.
     * 
     * @return The Swagger documentation.
     */
    public Documentation getDocumentation() {
        return this.documentation;
    }

    /**
     * Returns an HTML representation.
     * 
     * @return An HTML representation.
     */
    public Representation getHtmlRepresentation() {
        // TODO lack generation of HTML swagger page.
        return null;
    }

    /**
     * Sets the Swagger documentation.
     * 
     * @param documentation
     *            The Swagger documentation.
     */
    public void setDocumentation(Documentation documentation) {
        this.documentation = documentation;
    }

    @Override
    public ReadableByteChannel getChannel() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Reader getReader() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public InputStream getStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void write(Writer writer) throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void write(WritableByteChannel writableChannel) throws IOException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        // TODO Auto-generated method stub
        
    }

}
