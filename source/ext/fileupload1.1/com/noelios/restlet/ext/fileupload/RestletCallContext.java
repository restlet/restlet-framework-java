/*
 * Copyright 2005-2006 Jérôme LOUVEL
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

package com.noelios.restlet.ext.fileupload;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.fileupload.RequestContext;
import org.restlet.RestletCall;
import org.restlet.connector.ConnectorCall;

/**
 * Provides access to the call information needed by the FileUpload processor.  
 */
public class RestletCallContext implements RequestContext
{
	/** The call to adapt. */
	protected RestletCall call;
	
	/**
	 * Constructor.
	 * @param call The call to adapt.
	 */
	public RestletCallContext(RestletCall call)
	{	
		this.call = call;
	}
	
	/**
	 * Returns the character encoding for the form.
	 * @return The character encoding for the form.
	 */
	public String getCharacterEncoding()
	{
		return this.call.getInput().getMetadata().getEncoding().getName();
	}

	/**
	 * Returns the content length of the form.
	 * @return The content length of the form.
	 */
	public int getContentLength()
	{
		int result = -1;
		String contentLength = this.call.getConnectorCall().getRequestHeaderValue(ConnectorCall.HEADER_CONTENT_LENGTH);
		
		if((contentLength != null) && (!contentLength.equals("")))
		{
			result = Integer.parseInt(contentLength);
		}
		
		return result;
	}

	/**
	 * Returns the content type of the form.
	 * @return The content type of the form.
	 */
	public String getContentType()
	{
		return this.call.getInput().getMetadata().getMediaType().getName();
	}

	/**
	 * Returns the input stream.
	 * @return The input stream.
	 */
	public InputStream getInputStream() throws IOException
	{
		return this.call.getInput().getStream();
	}

}
