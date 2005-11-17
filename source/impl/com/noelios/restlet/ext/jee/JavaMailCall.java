/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.noelios.restlet.ext.jee;

import org.restlet.RestletException;
import org.restlet.data.Methods;
import org.restlet.data.Representation;

import com.noelios.restlet.CallImpl;
import com.noelios.restlet.data.ReferenceImpl;

/**
 * Call sending an email via a client mail connector.
 */
public class JavaMailCall extends CallImpl
{
   /**
    * Constructor.
    * @param smtpURI The SMTP server's URI (ex: smtp://localhost).
    * @param email	The email to send (valid XML email).
    */
   public JavaMailCall(String smtpURI, Representation email) throws RestletException
   {
      super(null, "Semalink", null, null, null, Methods.POST, new ReferenceImpl(smtpURI), null, email);
   }

}




