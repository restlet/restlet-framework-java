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

import org.restlet.Application;
import org.restlet.spi.Factory;

/**
 * Component attached to a virtual host and managed by a parent container. Applications are also guaranteed
 * to be portable between containers implementing the same Restlet API.  
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ApplicationDelegate extends Component 
{
	/**
	 * Constructor.
	 * @param wrappedApplication The wrapped application. 
	 */
	protected ApplicationDelegate(ApplicationDelegate wrappedApplication)
	{
		super(wrappedApplication);
	}
   
   /**
    * Constructor.
    * @param container The parent container.
    * @param application The delegated application.
    */
   public ApplicationDelegate(Container container, Application application)
   {
		this(Factory.getInstance().createApplicationDelegate(container, application));
   }

	/**
    * Returns the wrapped application.
    * @return The wrapped application.
    */
   protected ApplicationDelegate getWrappedApplicationDelegate()
   {
   	return (ApplicationDelegate)getWrappedComponent();
   }

   /**
    * Returns the application descriptor.
    * @return The application descriptor.
    */
   public Application getApplication()
   {
   	return getWrappedApplicationDelegate().getApplication();
   }
   
   /**
    * Indicates if the calls logging is enabled.
    * @return True if the calls logging is enabled.
    */
   public boolean isLoggingEnabled()
   {
   	return getWrappedApplicationDelegate().isLoggingEnabled();
   }
   
   /**
    * Indicates if the calls logging is enabled.
    * @param enabled True if the calls logging is enabled.
    */
   public void setLoggingEnabled(boolean enabled)
   {
   	getWrappedApplicationDelegate().setLoggingEnabled(enabled);
   }

   /**
    * Returns the name of the JDK's logger to use when logging calls.
    * @return The name of the JDK's logger to use when logging calls.
    */
   public String getLoggingName()
   {
   	return getWrappedApplicationDelegate().getLoggingName();
   }

   /**
    * Sets the name of the JDK's logger to use when logging calls.
    * @param name The name of the JDK's logger to use when logging calls.
    */
   public void setLoggingName(String name)
   {
   	getWrappedApplicationDelegate().setLoggingName(name);
   }

   /**
    * Returns the logging format used.
    * @return The logging format used, or null if the default one is used.
    */
   public String getLoggingFormat()
   {
   	return getWrappedApplicationDelegate().getLoggingFormat();
   }
   
   /**
    * Sets the format to use when logging calls. The default format matches the one of IIS 6.
    * 
    * ** ADD DETAILS ABOUT THE FORMAT SYNTAX AND AVAILABLE VARIABLES **
    * 
    * @param format The format to use when loggin calls.
    */
   public void setLoggingFormat(String format)
   {
   	getWrappedApplicationDelegate().setLoggingFormat(format);
   }
   
   /**
    * Indicates if status pages should be added.
    * @return True if status pages should be added.
    */
   public boolean isStatusEnabled()
   {
   	return getWrappedApplicationDelegate().isStatusEnabled();
   }
   
   /**
    * Indicates if status pages should be added.
    * @param enabled True status pages should be added.
    */
   public void setStatusEnabled(boolean enabled)
   {
   	getWrappedApplicationDelegate().setStatusEnabled(enabled);
   }
   
   /**
    * Indicates if status pages should overwrite existing output representations.
    * @return True if status pages should overwrite existing output representations.
    */
   public boolean isStatusOverwrite()
   {
   	return getWrappedApplicationDelegate().isStatusOverwrite();
   }
   
   /**
    * Indicates if status pages should overwrite existing output representations.
    * @param overwrite True if status pages should overwrite existing output representations.
    */
   public void setStatusOverwrite(boolean overwrite)
   {
   	getWrappedApplicationDelegate().setStatusOverwrite(overwrite);
   }

   /**
    * Returns the email to contact in case of issue with the application.
    * @return The email to contact in case of issue with the application.
    */
   public String getContactEmail()
   {
   	return getWrappedApplicationDelegate().getContactEmail();
   }

   /**
    * Sets the email to contact in case of issue with the application.
    * @param email The email to contact in case of issue with the application.
    */
   public void setContactEmail(String email)
   {
   	getWrappedApplicationDelegate().setContactEmail(email);
   }
   
}
