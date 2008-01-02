/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.jaxrs.ext;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;

import org.restlet.ext.jaxrs.todo.NotYetImplementedException;

/**
 * 
 * @author Stephan Koops
 *
 */
public class RuntimeDelegate extends javax.ws.rs.ext.RuntimeDelegate
{
	/**
	 * @see javax.ws.rs.ext.RuntimeDelegate#createHeaderDelegate(Class)
	 */
	@Override
	public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type)
	{
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	/**
	 * @see javax.ws.rs.ext.RuntimeDelegate#createResponseBuilder()
	 */
	@Override
	public ResponseBuilder createResponseBuilder()
	{
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	/**
	 * @see javax.ws.rs.ext.RuntimeDelegate#createUriBuilder()
	 */
	@Override
	public UriBuilder createUriBuilder()
	{
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}

	/**
	 * @see javax.ws.rs.ext.RuntimeDelegate#createVariantListBuilder()
	 */
	@Override
	public VariantListBuilder createVariantListBuilder()
	{
		// TODO Auto-generated method stub
		throw new NotYetImplementedException();
	}
}