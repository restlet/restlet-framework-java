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

package org.restlet;

/**
 * Filter holding an attached handler and providing filtering services such as call logging and setting of
 * status representations.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Holder extends Filter
{
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
	
	/**
	 * Constructor.
	 * @param wrappedHolder The wrapped application delegate. 
	 */
	public Holder(Holder wrappedHolder)
	{
		super(wrappedHolder);
	}
   
   /**
    * Constructor.
    * @param context The context.
    * @param next The attached handler.
    */
   public Holder(Context context, Handler next)
   {
   	super(context, next);
		this.loggingEnabled = true;
		this.loggingName = "org.restlet.application.calls." + hashCode();
		this.loggingFormat = null;
		this.statusEnabled = true;
		this.statusOverwrite = false;
		this.contactEmail = null;
   }

	/**
    * Returns the wrapped application.
    * @return The wrapped application.
    */
   private Holder getWrappedHolder()
   {
		return (Holder)getWrappedHandler();
   }
   
   /**
    * Indicates if the calls logging is enabled.
    * @return True if the calls logging is enabled.
    */
   public boolean isLoggingEnabled()
   {
   	return (getWrappedHolder() != null) ? getWrappedHolder().isLoggingEnabled() : this.loggingEnabled;
   }
   
   /**
    * Indicates if the calls logging is enabled.
    * @param enabled True if the calls logging is enabled.
    */
   public void setLoggingEnabled(boolean enabled)
   {
   	if(getWrappedHolder() != null)
   	{
   		getWrappedHolder().setLoggingEnabled(enabled);
   	}
   	else
   	{
   		this.loggingEnabled = enabled;
   	}
   }

   /**
    * Returns the name of the JDK's logger to use when logging calls.
    * @return The name of the JDK's logger to use when logging calls.
    */
   public String getLoggingName()
   {
   	return (getWrappedHolder() != null) ? getWrappedHolder().getLoggingName() : this.loggingName;
   }

   /**
    * Sets the name of the JDK's logger to use when logging calls.
    * @param name The name of the JDK's logger to use when logging calls.
    */
   public void setLoggingName(String name)
   {
   	if(getWrappedHolder() != null)
   	{
   		getWrappedHolder().setLoggingName(name);
   	}
   	else
   	{
   		this.loggingName = name;
   	}
   }

   /**
    * Returns the logging format used.
    * @return The logging format used, or null if the default one is used.
    */
   public String getLoggingFormat()
   {
   	return (getWrappedHolder() != null) ? getWrappedHolder().getLoggingFormat() : this.loggingFormat;
   }
   
   /**
    * Sets the format to use when logging calls. The default format matches the one of IIS 6.
    * ** ADD DETAILS ABOUT THE FORMAT SYNTAX AND AVAILABLE VARIABLES **
    * @param format The format to use when loggin calls.
    */
   public void setLoggingFormat(String format)
   {
   	if(getWrappedHolder() != null)
   	{
   		getWrappedHolder().setLoggingFormat(format);
   	}
   	else
   	{
   		this.loggingFormat = format;
   	}
   }
   
   /**
    * Indicates if status pages should be added.
    * @return True if status pages should be added.
    */
   public boolean isStatusEnabled()
   {
   	return (getWrappedHolder() != null) ? getWrappedHolder().isStatusEnabled() : this.statusEnabled;
   }
   
   /**
    * Indicates if status pages should be added.
    * @param enabled True status pages should be added.
    */
   public void setStatusEnabled(boolean enabled)
   {
   	if(getWrappedHolder() != null)
   	{
   		getWrappedHolder().setStatusEnabled(enabled);
   	}
   	else
   	{
   		this.statusEnabled = enabled;
   	}
   }
   
   /**
    * Indicates if status pages should overwrite existing output representations.
    * @return True if status pages should overwrite existing output representations.
    */
   public boolean isStatusOverwrite()
   {
   	return (getWrappedHolder() != null) ? getWrappedHolder().isStatusOverwrite() : this.statusOverwrite;
   }
   
   /**
    * Indicates if status pages should overwrite existing output representations.
    * @param overwrite True if status pages should overwrite existing output representations.
    */
   public void setStatusOverwrite(boolean overwrite)
   {
   	if(getWrappedHolder() != null)
   	{
   		getWrappedHolder().setStatusOverwrite(overwrite);
   	}
   	else
   	{
   		this.statusOverwrite = overwrite;
   	}
   }

   /**
    * Returns the email to contact in case of issue with the application.
    * @return The email to contact in case of issue with the application.
    */
   public String getContactEmail()
   {
   	return (getWrappedHolder() != null) ? getWrappedHolder().getContactEmail() : this.contactEmail;
   }

   /**
    * Sets the email to contact in case of issue with the application.
    * @param email The email to contact in case of issue with the application.
    */
   public void setContactEmail(String email)
   {
   	if(getWrappedHolder() != null)
   	{
   		getWrappedHolder().setContactEmail(email);
   	}
   	else
   	{
   		this.contactEmail = email;
   	}
   }
   
}
