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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.UniformInterface;
import org.restlet.component.ApplicationDelegate;
import org.restlet.component.Container;
import org.restlet.data.Status;

import com.noelios.restlet.impl.LogFilter;
import com.noelios.restlet.impl.StatusFilter;

/**
 * Application implementation.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ApplicationDelegateImpl extends ApplicationDelegate
{
	/** Obtain a suitable logger. */
	private static Logger logger = Logger.getLogger(ApplicationDelegateImpl.class
			.getCanonicalName());

	/** The context. */
	private Context context;

	/** Indicates if the instance was started. */
	private boolean started;

	/** The application descriptor. */
	private Application application;

	/** Indicates if the calls logging is enabled. */
	private boolean loggingEnabled;

	/** The logging name to use. */
	private String loggingName;

	/** The logging format to use (or null for the default). */
	private String loggingFormat;

	/** Indicates if status pages should be added. */
	private boolean statusEnabled;

	/** Indicates if the status pages should overwrite existing output representations. */
	private boolean statusOverwrite;

	/** The email to contact in case of issue with the application. */
	private String contactEmail;

	/** The root handler. */
	private UniformInterface root;

	/**
	 * Constructor.
	 * @param container The parent container.
	 * @param application The application descriptor.
	 */
	public ApplicationDelegateImpl(Container container, Application application)
	{
		super(null);
		this.context = new ApplicationContext(application,
				"org.restlet.application.context." + hashCode());
		this.started = false;
		this.application = application;
		this.loggingEnabled = true;
		this.loggingName = "org.restlet.application.calls." + hashCode();
		this.loggingFormat = null;
		this.statusEnabled = true;
		this.statusOverwrite = false;
		this.contactEmail = null;
		this.root = null;
	}

	/**
	 * Returns the context.
	 * @return The context.
	 */
	public Context getContext()
	{
		return this.context;
	}

	/**
	 * Sets the context.
	 * @param context The context.
	 */
	public void setContext(Context context)
	{
		this.context = context;
	}

	/** Start hook. */
	public void start() throws Exception
   {
		this.root = null;
   	Filter lastFilter = null;

   	// Logging of calls
   	if(isLoggingEnabled())
   	{
   		lastFilter = new LogFilter(getContext(), getLoggingName(), getLoggingFormat());
   		this.root = lastFilter;
   	}
   	
   	// Addition of status pages
   	if(isStatusEnabled())
   	{
   		Filter statusFilter = new StatusFilter(getContext(), isStatusOverwrite(), getContactEmail(), "/");
   		if(lastFilter != null) lastFilter.setNext(statusFilter);
   		if(this.root == null) this.root = statusFilter;
   		lastFilter = statusFilter;
   	}

   	// Creation the application root
   	UniformInterface applicationRoot = getApplication().createRoot(getContext());
   	if(this.root == null) 
   	{
   		this.root = applicationRoot;
   	}
   	else
   	{
   		lastFilter.setNext(applicationRoot);
   	}

   	// Start completed
   	this.started = true;
   }

	/** Stop hook. */
	public void stop() throws Exception
	{
		this.root = null;
		this.started = false;
	}

	/**
	 * Indicates if the Restlet is started.
	 * @return True if the Restlet is started.
	 */
	public boolean isStarted()
	{
		return this.started;
	}

	/**
	 * Indicates if the Restlet is stopped.
	 * @return True if the Restlet is stopped.
	 */
	public boolean isStopped()
	{
		return !this.started;
	}

	/**
	 * Handles a direct call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void handle(Request request, Response response)
	{
		try
		{
			if (!isStarted()) start();

			if (getRoot() != null)
			{
				getRoot().handle(request, response);
			}
			else
			{
				response.setStatus(Status.SERVER_ERROR_INTERNAL);
				logger.log(Level.SEVERE, "No root handler defined.");
			}
		}
		catch (Exception e)
		{
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			logger.log(Level.SEVERE, "Unexpected exception while handling a call", e);
		}

	}

	/**
	 * Returns the application descriptor.
	 * @return The application descriptor.
	 */
	public Application getApplication()
	{
		return this.application;
	}

	/**
	 * Indicates if the calls logging is enabled.
	 * @return True if the calls logging is enabled.
	 */
	public boolean isLoggingEnabled()
	{
		return this.loggingEnabled;
	}

	/**
	 * Indicates if the calls logging is enabled.
	 * @param enabled True if the calls logging is enabled.
	 */
	public void setLoggingEnabled(boolean enabled)
	{
		this.loggingEnabled = enabled;
	}

	/**
	 * Returns the name of the JDK's logger to use when logging calls.
	 * @return The name of the JDK's logger to use when logging calls.
	 */
	public String getLoggingName()
	{
		return this.loggingName;
	}

	/**
	 * Sets the name of the JDK's logger to use when logging calls.
	 * @param name The name of the JDK's logger to use when logging calls.
	 */
	public void setLoggingName(String name)
	{
		this.loggingName = name;
	}

	/**
	 * Returns the logging format used.
	 * @param format The logging format used, or null if the default one is used.
	 */
	public String getLoggingFormat()
	{
		return this.loggingFormat;
	}

	/**
	 * Sets the format to use when loggin calls. The default format matches the one of IIS 6.
	 * 
	 * ** ADD DETAILS ABOUT THE FORMAT SYNTAX AND AVAILABLE VARIABLES **
	 * 
	 * @param format The format to use when loggin calls.
	 */
	public void setLoggingFormat(String format)
	{
		this.loggingFormat = format;
	}

	/**
	 * Indicates if status pages should be added.
	 * @return True if status pages should be added.
	 */
	public boolean isStatusEnabled()
	{
		return this.statusEnabled;
	}

	/**
	 * Indicates if status pages should be added.
	 * @param enabled True status pages should be added.
	 */
	public void setStatusEnabled(boolean enabled)
	{
		this.statusEnabled = enabled;
	}

	/**
	 * Indicates if status pages should overwrite existing output representations.
	 * @return True if status pages should overwrite existing output representations.
	 */
	public boolean isStatusOverwrite()
	{
		return this.statusOverwrite;
	}

	/**
	 * Indicates if status pages should overwrite existing output representations.
	 * @param overwrite True if status pages should overwrite existing output representations.
	 */
	public void setStatusOverwrite(boolean overwrite)
	{
		this.statusOverwrite = overwrite;
	}

	/**
	 * Returns the root handler.
	 * @return the root handler.
	 */
	public UniformInterface getRoot()
	{
		return this.root;
	}

	/**
	 * Sets the root handler.
	 * @param root The root to set.
	 */
	public void setRoot(UniformInterface root)
	{
		this.root = root;
	}

	/**
	 * Returns the email to contact in case of issue with the application.
	 * @return The email to contact in case of issue with the application.
	 */
	public String getContactEmail()
	{
		return this.contactEmail;
	}

	/**
	 * Sets the email to contact in case of issue with the application.
	 * @param email The email to contact in case of issue with the application.
	 */
	public void setContactEmail(String email)
	{
		this.contactEmail = email;
	}

}
