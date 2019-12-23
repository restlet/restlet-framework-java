/**
 * Copyright 2005-2019 Talend
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
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.ext.fileupload;

import java.io.IOException;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.restlet.Request;
import org.restlet.representation.Representation;

/**
 * High level API for processing file uploads. This class handles multiple files
 * per single HTML widget, sent using the "multipart/mixed" encoding type, as
 * specified by RFC 1867. Use {@link #parseRequest(Request)} method to acquire a
 * list of FileItems associated with a given HTML widget.<br>
 * <br>
 * How the data for individual parts is stored is determined by the factory used
 * to create them; a given part may be in memory, on disk, or somewhere else.<br>
 * <br>
 * In addition, it is possible to use <a
 * href="http://commons.apache.org/fileupload/streaming.html> FileUpload's
 * streaming API</a> to prevent the intermediary storage step. For this, use the
 * {@link #getItemIterator(org.apache.commons.fileupload.RequestContext)}
 * method.
 * 
 * @author Jerome Louvel
 */
public class RestletFileUpload extends
// [ifndef gae,jee] line
        FileUpload
// [ifdef gae,jee] line uncomment
// org.apache.commons.fileupload.servlet.ServletFileUpload
{
    /**
     * Constructs an uninitialized instance of this class. A factory must be
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
     * compliant <code>multipart/form-data</code> input representation. Note
     * that this will not result in the writing of the parts on the disk but
     * will instead allow you to use stream access.
     * 
     * @param multipartForm
     *            The input representation.
     * @return An iterator to instances of FileItemStream parsed from the
     *         request.
     * @throws FileUploadException
     * @throws IOException
     * @see <a
     *      href="http://commons.apache.org/fileupload/streaming.html">FileUpload
     *      Streaming API</a>
     */
    public FileItemIterator getItemIterator(Representation multipartForm)
            throws FileUploadException, IOException {
        return getItemIterator(new RepresentationContext(multipartForm));
    }

    // [ifndef gae] method
    /**
     * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
     * compliant <code>multipart/form-data</code> input representation. Note
     * that this will result in the writing of the parts on the disk.
     * 
     * @param multipartForm
     *            The multipart representation to be parsed.
     * @return A list of <code>FileItem</code> instances parsed, in the order
     *         that they were transmitted.
     * @throws FileUploadException
     *             if there are problems reading/parsing the request or storing
     *             files.
     */
    public List<FileItem> parseRepresentation(Representation multipartForm)
            throws FileUploadException {
        return parseRequest(new RepresentationContext(multipartForm));
    }

    // [ifndef gae] method
    /**
     * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
     * compliant <code>multipart/form-data</code> input representation. Note
     * that this will result in the writing of the parts on the disk.
     * 
     * @param request
     *            The request containing the entity to be parsed.
     * @return A list of <code>FileItem</code> instances parsed, in the order
     *         that they were transmitted.
     * @throws FileUploadException
     *             if there are problems reading/parsing the request or storing
     *             files.
     */
    public List<FileItem> parseRequest(Request request)
            throws FileUploadException {
        // [ifndef jee] instruction
        return parseRequest(new RepresentationContext(request.getEntity()));
        // [ifdef jee] instruction uncomment
        // return
        // parseRequest(org.restlet.ext.servlet.ServletUtils.getRequest(request));
    }

}
