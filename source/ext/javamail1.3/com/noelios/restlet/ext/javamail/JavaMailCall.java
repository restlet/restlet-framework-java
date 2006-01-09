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

package com.noelios.restlet.ext.javamail;

import org.restlet.RestletException;
import org.restlet.data.Methods;
import org.restlet.data.Representation;

import com.noelios.restlet.UniformCallImpl;
import com.noelios.restlet.data.ReferenceImpl;

/**
 * Call sending an email via a client mail connector.
 */
public class JavaMailCall extends UniformCallImpl
{
   /**
    * Constructor.
    * @param smtpURI The SMTP server's URI (ex: smtp://localhost).
    * @param email The email to send (valid XML email).
    * @throws RestletException
    */
   public JavaMailCall(String smtpURI, Representation email) throws RestletException
   {
      super(null, "Semalink", null, null, null, Methods.POST, new ReferenceImpl(smtpURI), null, email);
   }

}
