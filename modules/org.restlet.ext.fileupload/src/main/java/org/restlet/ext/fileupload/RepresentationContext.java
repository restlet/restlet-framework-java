/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.fileupload;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.UploadContext;
import org.restlet.representation.Representation;

/**
 * Provides access to the representation information needed by the FileUpload
 * processor.
 * 
 * @author Jerome Louvel
 */
public class RepresentationContext implements UploadContext  {

    /** The representation to adapt. */
    private volatile Representation multipartForm;

    /**
     * Constructor.
     * 
     * @param multipartForm
     *            The multipart form to parse.
     */
    public RepresentationContext(Representation multipartForm) {
        this.multipartForm = multipartForm;
    }

	@Override
	public long contentLength() {
        return this.multipartForm.getSize();
	}

	@Override
    public String getCharacterEncoding() {
        if (this.multipartForm.getCharacterSet() != null) {
            return this.multipartForm.getCharacterSet().getName();
        }

        return null;
    }

	@Override
    public int getContentLength() {
        return (int) contentLength();
    }

	@Override
    public String getContentType() {
        if (this.multipartForm.getMediaType() != null) {
            return this.multipartForm.getMediaType().toString();
        }

        return null;
    }

	@Override
    public InputStream getInputStream() throws IOException {
        return this.multipartForm.getStream();
    }

}
