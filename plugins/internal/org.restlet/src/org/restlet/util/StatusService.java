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

package org.restlet.util;

import org.restlet.data.Reference;

/**
 * Service providing status representation setting.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class StatusService extends Service
{
	/** The email address to contact in case of error. */
	private String contactEmail;
	
	/** The home URI to propose in case of error. */
	private Reference homeRef;
	
	/** True if an existing entity should be overwritten. */
	private boolean overwrite;
	
	/**
	 * Constructor.
	 * @param enabled True if the service has been enabled.
	 */
	public StatusService(boolean enabled)
	{
		super(enabled);
		this.contactEmail = null;
		this.homeRef = null;
		this.overwrite = true;
	}
	
   /** 
    * Returns the email address to contact in case of error. 
    * This is typically used when creating the status representations.
    * @return The email address to contact in case of error.
    */
   public String getContactEmail()
   {
   	return this.contactEmail;
   }

   /** 
    * Returns the home URI to propose in case of error.
    * @return The home URI to propose in case of error.
    */
   public Reference getHomeRef()
   {
   	return this.homeRef;
   }

   /** 
    * Indicates if an existing entity should be overwritten. 
    * @return True if an existing entity should be overwritten.
    */
   public boolean isOverwrite()
   {
   	return this.overwrite;
   }

   /** 
    * Sets the email address to contact in case of error. 
    * This is typically used when creating the status representations.
    * @param contactEmail The email address to contact in case of error.
    */
   public void setContactEmail(String contactEmail)
   {
   	this.contactEmail = contactEmail;
   }

   /** 
    * Sets the home URI to propose in case of error.
    * @param homeRef The home URI to propose in case of error.
    */
   public void setHomeRef(Reference homeRef)
   {
   	this.homeRef = homeRef;
   }

   /** 
    * Indicates if an existing entity should be overwritten. 
    * @param overwrite True if an existing entity should be overwritten.
    */
   public void setOverwrite(boolean overwrite)
   {
   	this.overwrite = overwrite;
   }
  
}
