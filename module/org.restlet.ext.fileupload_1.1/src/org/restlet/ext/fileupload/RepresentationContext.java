/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet.ext.fileupload;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.RequestContext;
import org.restlet.resource.Representation;

/**
 * Provides access to the representation information needed by the FileUpload
 * processor.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class RepresentationContext implements RequestContext {
    /** The representation to adapt. */
    private Representation multipartForm;

    /**
     * Constructor.
     * 
     * @param multipartForm
     *            The multipart form to parse.
     */
    public RepresentationContext(Representation multipartForm) {
        this.multipartForm = multipartForm;
    }

    /**
     * Returns the character encoding for the form.
     * 
     * @return The character encoding for the form.
     */
    public String getCharacterEncoding() {
        if (this.multipartForm.getEncoding() != null) {
            return this.multipartForm.getEncoding().getName();
        } else {
            return null;
        }
    }

    /**
     * Returns the content length of the form.
     * 
     * @return The content length of the form.
     */
    public int getContentLength() {
        return (int) this.multipartForm.getSize();
    }

    /**
     * Returns the content type of the form.
     * 
     * @return The content type of the form.
     */
    public String getContentType() {
        if (this.multipartForm.getMediaType() != null) {
            return this.multipartForm.getMediaType().getName();
        } else {
            return null;
        }
    }

    /**
     * Returns the input stream.
     * 
     * @return The input stream.
     */
    public InputStream getInputStream() throws IOException {
        return this.multipartForm.getStream();
    }

}
