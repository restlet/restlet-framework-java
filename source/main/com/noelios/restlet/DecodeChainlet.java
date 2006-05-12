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

import org.restlet.AbstractChainlet;
import org.restlet.Call;
import org.restlet.component.Component;
import org.restlet.data.Encoding;
import org.restlet.data.Representation;

import com.noelios.restlet.data.DecoderRepresentation;

/**
 * Chainlet decoding the input or output representation of calls. It is mainly used to automatically 
 * decompress those representations. 
 */
public class DecodeChainlet extends AbstractChainlet
{
	/**
	 * Indicates if the input representation should be decoded.
	 */
	protected boolean decodeInput;
	
	/**
	 * Indicates if the output representation should be decoded.
	 */
	protected boolean decodeOutput;

	/**
	 * Constructor to only decode input representations before call handling.
	 * @param parent The parent component.
	 */
	public DecodeChainlet(Component parent)
	{
		this(parent, true, false);
	}

	/**
	 * Constructor.
	 * @param parent The parent component.
	 * @param decodeInput Indicates if the input representation should be decoded.
	 * @param decodeOutput Indicates if the output representation should be decoded.
	 */
	public DecodeChainlet(Component parent, boolean decodeInput, boolean decodeOutput)
	{
		super(parent);
		this.decodeInput = decodeInput;
		this.decodeOutput = decodeOutput;
	}
	
   /**
    * Handles a call.
    * @param call The call to handle.
    */
	public void handle(Call call)
	{
		// Check if decoding of the call input is needed
		if(isDecodeInput() && canDecode(call.getInput()))
		{
			call.setInput(decode(call.getInput()));
		}
		
		// Delegate the handling to the attached Restlet
		super.handle(call);
		
		// Check if decoding of the call output is needed
		if(isDecodeOutput() && canDecode(call.getOutput()))
		{
			call.setOutput(decode(call.getOutput()));
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
		return (representation != null) && (representation.getMetadata().getEncoding() != null);
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
		
		for(Iterator<Encoding> iter = DecoderRepresentation.getSupportedEncodings().iterator(); iter.hasNext(); )
		{
			currentEncoding = iter.next();
			
			if(representation.getMetadata().getEncoding().equals(currentEncoding))
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
