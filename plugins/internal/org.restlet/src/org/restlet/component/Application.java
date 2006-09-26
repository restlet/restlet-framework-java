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

package org.restlet.component;

import java.util.List;

import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.spi.Factory;

/**
 * Component holding a user application, typically within a parent container. The role of an 
 * application is to standardize the configuration of an application for aspects such as call logging,
 * authentication, status pages, virtual hosting, etc.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Application extends Component 
{
	public Application(Context context)
	{
		super(Factory.getInstance().createApplication(context));
	}
	
	/**
	 * Constructor for wrappers.
	 * @param wrappedApplication The wrapped application.
	 */
	protected Application(Application wrappedApplication)
	{
		super(wrappedApplication);
	}

	/**
	 * Returns the wrapped application.
	 * @return The wrapped application.
	 */
	protected Application getWrappedApplication()
	{
		return (Application)getWrappedComponent();
	}
	
	/**
	 * Retuns the root application path
	 * @return
	 */
	public String getRootPath()
	{
		return null;
	}
	
	/**
	 * Returns the list of client connectors required for a proper behavior of the application. 
	 * @return The list of client connectors required.
	 */
	public List<Protocol> getRequiredClients()
	{
		return getWrappedApplication().getRequiredClients();
	}
	
	/**
	 * Returns the list of server connectors required for a proper behavior of the application. 
	 * @return The list of server connectors required.
	 */
	public List<Protocol> getRequiredServers()
	{
		return getWrappedApplication().getRequiredServers();
	}
}
