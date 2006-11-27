/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
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
 * @author Jerome Louvel (contact@noelios.com)
 */
public class RestletFileUpload extends FileUpload {
	/**
	 * Constructs an uninitialised instance of this class. A factory must be
	 * configured, using <code>setFileItemFactory()</code>, before attempting
	 * to parse request entity.
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

}
