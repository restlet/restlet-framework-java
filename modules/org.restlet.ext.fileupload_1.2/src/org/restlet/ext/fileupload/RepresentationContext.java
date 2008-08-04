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
        if (!this.multipartForm.getEncodings().isEmpty()) {
            return this.multipartForm.getEncodings().get(0).getName();
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
            return this.multipartForm.getMediaType().toString();
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
