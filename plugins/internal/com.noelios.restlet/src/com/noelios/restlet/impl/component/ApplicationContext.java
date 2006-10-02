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

package com.noelios.restlet.impl.component;

import org.restlet.component.Application;

/**
 * Context based on a parent container's context but dedicated to an application. This is important to allow
 * contextual access to application's resources.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ApplicationContext
{
	/** The parent application. */
	private Application application;
	
	/**
	 * Constructor.
	 * @param application The parent application.
	 */
	public ApplicationContext(Application application)
	{
		this.application = application;
	}

	/**
	 * @return the application
	 */
	public Application getApplication()
	{
		return application;
	}

	/**
	 * @param application the application to set
	 */
	public void setApplication(Application application)
	{
		this.application = application;
	}
	
}
