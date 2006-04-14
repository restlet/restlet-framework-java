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

package com.noelios.restlet.test;

import org.restlet.AbstractRestlet;
import org.restlet.RestletCall;
import org.restlet.data.MediaTypes;

import com.noelios.restlet.data.StringRepresentation;

/**
 * Trace target. 
 */
public class TraceTarget extends AbstractRestlet
{
   /**
    * Handles a uniform call.
    * @param call The uniform call to handle.
    */
   public void handle(RestletCall call)
   {
      String output = "Hello World!" +
                      "\nYour IP address is " + call.getClientAddress() + 
                      "\nYour request URI is: " + call.getResourceRef().toString();
      call.setOutput(new StringRepresentation(output, MediaTypes.TEXT_PLAIN));
   }

}
