/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
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
 * @deprecated Will be removed in next minor release in favor or Jetty extension.
 */
@Deprecated
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
