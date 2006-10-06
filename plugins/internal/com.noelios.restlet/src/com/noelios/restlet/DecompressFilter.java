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

package com.noelios.restlet;

import java.util.Iterator;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.Representation;

import com.noelios.restlet.data.DecoderRepresentation;

/**
 * Filter decompressing input or output representations. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class DecompressFilter extends Filter
{
	/**
	 * Indicates if the input representation should be decoded.
	 */
	private boolean decodeInput;
	
	/**
	 * Indicates if the output representation should be decoded.
	 */
	private boolean decodeOutput;

	/**
	 * Constructor to only decode input representations before call handling.
	 * @param context The context.
	 */
	public DecompressFilter(Context context)
	{
		this(context, true, false);
	}

	/**
	 * Constructor.
	 * @param context The context.
	 * @param decodeInput Indicates if the input representation should be decoded.
	 * @param decodeOutput Indicates if the output representation should be decoded.
	 */
	public DecompressFilter(Context context, boolean decodeInput, boolean decodeOutput)
	{
		super(context);
		this.decodeInput = decodeInput;
		this.decodeOutput = decodeOutput;
	}

   /**
    * Allows filtering before its handling by the target Restlet. Does nothing by default.
    * @param request The request to filter.
    * @param response The response to filter.
    */
   public void beforeHandle(Request request, Response response)
   {
		// Check if decoding of the call input is needed
		if(isDecodeInput() && canDecode(request.getInput()))
		{
			request.setInput(decode(request.getInput()));
		}
   }

   /**
    * Allows filtering after its handling by the target Restlet. Does nothing by default.
    * @param request The request to filter.
    * @param response The response to filter.
    */
   public void afterHandle(Request request, Response response)
   {
		// Check if decoding of the call output is needed
		if(isDecodeOutput() && canDecode(response.getOutput()))
		{
			response.setOutput(decode(response.getOutput()));
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
		return (representation != null) && (representation.getEncoding() != null) && 
				 !representation.getEncoding().equals(Encoding.IDENTITY);
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
		
		for(Iterator<Encoding> iter = DecoderRepresentation.getSupportedEncodings().iterator(); 
			 (result == representation) && iter.hasNext(); )
		{
			currentEncoding = iter.next();
			
			if(!currentEncoding.equals(Encoding.IDENTITY) && 
				representation.getEncoding().equals(currentEncoding))
			{
				result = new DecoderRepresentation(representation);
			}
		}
				
		return result;
	}
	
	/**
	 * Indicates if the input representation should be decoded.
	 * @return True if the input representation should be decoded. 
	 */
	public boolean isDecodeInput()
	{
		return this.decodeInput;
	}

	/**
	 * Indicates if the input representation should be decoded.
	 * @param decodeInput True if the input representation should be decoded. 
	 */
	public void setDecodeInput(boolean decodeInput)
	{
		this.decodeInput = decodeInput;
	}

	/**
	 * Indicates if the output representation should be decoded.
	 * @return True if the output representation should be decoded. 
	 */
	public boolean isDecodeOutput()
	{
		return this.decodeOutput;
	}

	/**
	 * Indicates if the output representation should be decoded.
	 * @param decodeOutput True if the output representation should be decoded. 
	 */
	public void setDecodeOutput(boolean decodeOutput)
	{
		this.decodeOutput = decodeOutput;
	}

}
