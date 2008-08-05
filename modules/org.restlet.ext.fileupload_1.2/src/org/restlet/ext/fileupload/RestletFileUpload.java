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
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.fileupload;

import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.restlet.data.Request;
import org.restlet.resource.Representation;

/**
 * High level API for processing file uploads. This class handles multiple files
 * per single HTML widget, sent using multipart/mixed encoding type, as
 * specified by RFC 1867. Use parseRequest(Call) to acquire a list of FileItems
 * associated with a given HTML widget.How the data for individual parts is
 * stored is determined by the factory used to create them; a given part may be
 * in memory, on disk, or somewhere else.
 * 
 * @author Jerome Louvel
 */
public class RestletFileUpload extends FileUpload {
    /**
     * Constructs an uninitialised instance of this class. A factory must be
     * configured, using <code>setFileItemFactory()</code>, before attempting to
     * parse request entity.
     * 
     * @see RestletFileUpload#RestletFileUpload(FileItemFactory)
     */
    public RestletFileUpload() {
        super();
    }

    /**
     * Constructs an instance of this class which uses the supplied factory to
     * create <code>FileItem</code> instances.
     * 
     * @see RestletFileUpload#RestletFileUpload()
     */
    public RestletFileUpload(FileItemFactory fileItemFactory) {
        super(fileItemFactory);
    }

    /**
     * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
     * compliant <code>multipart/form-data</code> input representation.
     * 
     * @param multipartForm
     *            The multipart representation to be parsed.
     * @return A list of <code>FileItem</code> instances parsed, in the order
     *         that they were transmitted.
     * @throws FileUploadException
     *             if there are problems reading/parsing the request or storing
     *             files.
     */
    @SuppressWarnings("unchecked")
    public List<FileItem> parseRepresentation(Representation multipartForm)
            throws FileUploadException {
        return parseRequest(new RepresentationContext(multipartForm));
    }

    /**
     * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
     * compliant <code>multipart/form-data</code> input representation.
     * 
     * @param request
     *            The request containing the entity to be parsed.
     * @return A list of <code>FileItem</code> instances parsed, in the order
     *         that they were transmitted.
     * @throws FileUploadException
     *             if there are problems reading/parsing the request or storing
     *             files.
     */
    @SuppressWarnings("unchecked")
    public List<FileItem> parseRequest(Request request)
            throws FileUploadException {
        return parseRequest(new RepresentationContext(request.getEntity()));
    }

}
