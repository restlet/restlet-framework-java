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
package org.restlet.test;

import java.util.Date;

import org.restlet.data.ParameterList;

import com.noelios.restlet.connector.HttpCall;

/**
 * Dummy connector call. 
 * All methods throw an UnsupportedOperationException.
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://www.semagia.com/">Semagia</a>
 * @version $Rev:$ - $Date:$
 */
public class DummyConnectorCall implements HttpCall
{
	/* (non-Javadoc)
	 * @see org.restlet.connector.ConnectorCall#addRequestHeader(java.lang.String, java.lang.String)
	 */
	public void addRequestHeader(String arg0, String arg1)
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ConnectorCall#addResponseHeader(java.lang.String, java.lang.String)
	 */
	public void addResponseHeader(String arg0, String arg1)
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ConnectorCall#isConfidential()
	 */
	public boolean isConfidential()
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ConnectorCall#getRequestAddress()
	 */
	public String getRequestAddress()
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ConnectorCall#getRequestMethod()
	 */
	public String getRequestMethod()
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ConnectorCall#getRequestUri()
	 */
	public String getRequestUri()
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ConnectorCall#getRequestHeaders()
	 */
	public ParameterList getRequestHeaders()
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ConnectorCall#getResponseAddress()
	 */
	public String getResponseAddress()
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ConnectorCall#getResponseStatusCode()
	 */
	public int getResponseStatusCode()
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ConnectorCall#getResponseReasonPhrase()
	 */
	public String getResponseReasonPhrase()
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ConnectorCall#getResponseHeaders()
	 */
	public ParameterList getResponseHeaders()
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ConnectorCall#parseDate(java.lang.String, boolean)
	 */
	public Date parseDate(String arg0, boolean arg1)
	{
		throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.restlet.connector.ConnectorCall#formatDate(java.util.Date, boolean)
	 */
	public String formatDate(Date arg0, boolean arg1)
	{
		throw new UnsupportedOperationException();
	}

}
