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
 * 
 * Portions Copyright 2006 Lars Heuer (heuer[at]semagia.com)
 */

package com.noelios.restlet.filter;

import java.util.Iterator;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.Encoding;
import org.restlet.data.Representation;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.impl.data.DecoderRepresentation;

/**
 * Filter decompressing entities. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class DecoderFilter extends Filter
{
	/**
	 * Indicates if the request entity should be decoded.
	 */
	private boolean decodeRequest;

	/**
	 * Indicates if the response entity should be decoded.
	 */
	private boolean decodeResponse;

	/**
	 * Constructor to only decode request entities before handling.
	 * @param context The context.
	 */
	public DecoderFilter(Context context)
	{
		this(context, true, false);
	}

	/**
	 * Constructor.
	 * @param context The context.
	 * @param decodeRequest Indicates if the request entity should be decoded.
	 * @param decodeResponse Indicates if the response entity should be decoded.
	 */
	public DecoderFilter(Context context, boolean decodeRequest, boolean decodeResponse)
	{
		super(context);
		this.decodeRequest = decodeRequest;
		this.decodeResponse = decodeResponse;
	}

	/**
	 * Allows filtering before its handling by the target Restlet. Does nothing by default.
	 * @param request The request to filter.
	 * @param response The response to filter.
	 */
	public void beforeHandle(Request request, Response response)
	{
		// Check if decoding of the request entity is needed
		if (isDecodeRequest() && canDecode(request.getEntity()))
		{
			request.setEntity(decode(request.getEntity()));
		}
	}

	/**
	 * Allows filtering after its handling by the target Restlet. Does nothing by default.
	 * @param request The request to filter.
	 * @param response The response to filter.
	 */
	public void afterHandle(Request request, Response response)
	{
		// Check if decoding of the response entity is needed
		if (isDecodeResponse() && canDecode(response.getEntity()))
		{
			response.setEntity(decode(response.getEntity()));
		}
	}

	/**
	 * Indicates if a representation can be decoded.
	 * @param representation The representation to test.
	 * @return True if the call can be decoded.
	 */
	public boolean canDecode(Representation representation)
	{
		// Test the existance of the representation and that an encoding applies
		return (representation != null) && (representation.getEncoding() != null)
				&& !representation.getEncoding().equals(Encoding.IDENTITY);
	}

	/**
	 * Decodes a given representation if its encoding is supported by NRE.
	 * @param representation The representation to encode.
	 * @return The decoded representation or the original one if the encoding isn't supported by NRE.
	 */
	public Representation decode(Representation representation)
	{
		Representation result = representation;
		Encoding currentEncoding = null;

		for (Iterator<Encoding> iter = DecoderRepresentation.getSupportedEncodings()
				.iterator(); (result == representation) && iter.hasNext();)
		{
			currentEncoding = iter.next();

			if (!currentEncoding.equals(Encoding.IDENTITY)
					&& representation.getEncoding().equals(currentEncoding))
			{
				result = new DecoderRepresentation(representation);
			}
		}

		return result;
	}

	/**
	 * Indicates if the request entity should be decoded.
	 * @return True if the request entity should be decoded. 
	 */
	public boolean isDecodeRequest()
	{
		return this.decodeRequest;
	}

	/**
	 * Indicates if the request entity should be decoded.
	 * @param decodeRequest True if the request entity should be decoded. 
	 */
	public void setDecodeRequest(boolean decodeRequest)
	{
		this.decodeRequest = decodeRequest;
	}

	/**
	 * Indicates if the response entity should be decoded.
	 * @return True if the response entity should be decoded. 
	 */
	public boolean isDecodeResponse()
	{
		return this.decodeResponse;
	}

	/**
	 * Indicates if the response entity should be decoded.
	 * @param decodeResponse True if the response entity should be decoded. 
	 */
	public void setDecodeResponse(boolean decodeResponse)
	{
		this.decodeResponse = decodeResponse;
	}

}
