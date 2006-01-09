/*
 * Copyright 2005-2006 Jérôme LOUVEL
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

package com.noelios.restlet.data;

import org.restlet.data.ChallengeScheme;

/**
 * Challenge scheme used to authenticate remote clients.
 */
public class ChallengeSchemeImpl implements ChallengeScheme
{
   /** The unique name of the scheme. */
   protected String name;

   /** The technical name of the scheme. */
   protected String technicalName;

   /**
    * Constructor.
    * @param name The unique name of the scheme.
    * @param technicalName The technical name of the scheme.
    */
   public ChallengeSchemeImpl(String name, String technicalName)
   {
      this.name = name;
      this.technicalName = technicalName;
   }

   /**
    * Returns the technical name of the scheme.
    * @return The technical name of the scheme.
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Returns the technical name of the scheme (ex: BASIC).
    * @return The technical name of the scheme (ex: BASIC).
    */
   public String getTechnicalName()
   {
      return this.technicalName;
   }

   /**
    * Indicates if the scheme is equal to a given one.
    * @param scheme The scheme to compare to.
    * @return True if the scheme is equal to a given one.
    */
   public boolean equals(ChallengeScheme scheme)
   {
      return scheme.getName().equalsIgnoreCase(getName());
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Authentication challenge scheme";
   }

}
