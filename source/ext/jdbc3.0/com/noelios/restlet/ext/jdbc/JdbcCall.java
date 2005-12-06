/*
 * Copyright 2005 Jérôme LOUVEL
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

package com.noelios.restlet.ext.jdbc;

import org.restlet.data.Methods;
import org.restlet.data.Representation;

import com.noelios.restlet.UniformCallImpl;
import com.noelios.restlet.data.ReferenceImpl;

/**
 * Call sending a request via a JDBC client connector.
 */
public class JdbcCall extends UniformCallImpl
{
   /**
    * Constructor.
    * @param jdbcURI The database's JDBC URI (ex: jdbc:mysql://[hostname]/[database]).
    * @param request The request to send (valid XML request).
    * @see com.noelios.restlet.ext.jdbc.JdbcClient
    */
   public JdbcCall(String jdbcURI, Representation request)
   {
      super(null, "Semalink", null, null, null, Methods.POST, new ReferenceImpl(jdbcURI), null, request);
   }

}
