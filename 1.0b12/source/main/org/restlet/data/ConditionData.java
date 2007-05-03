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

package org.restlet.data;

import java.util.Date;
import java.util.List;

/**
 * Set of conditions applying to an uniform call. This is equivalent to the HTTP conditional headers.  
 */
public class ConditionData implements ControlData
{
   /** The "if-modified-since" condition */
   protected Date modifiedSince;
   
   /** The "if-unmodified-since" condition */
   protected Date unmodifiedSince;
   
   /** The "if-match" condition */
   protected List<Tag> match;
   
   /** The "if-none-match" condition */
   protected List<Tag> noneMatch;
   
   /**
    * Constructor.
    */
   public ConditionData()
   {
      this.modifiedSince = null;
      this.unmodifiedSince = null;
      this.match = null;
      this.noneMatch = null;
   }
   
   /**
    * Returns the "if-modified-since" condition.
    * @return The "if-modified-since" condition.
    */
   public Date getModifiedSince()
   {
      return this.modifiedSince;
   }
   
   /**
    * Sets the "if-modified-since" condition.
    * @param date The "if-modified-since" condition.
    */
   public void setModifiedSince(Date date)
   {
      this.modifiedSince = date;
   }

   /**
    * Returns the "if-unmodified-since" condition.
    * @return The "if-unmodified-since" condition.
    */
   public Date getUnmodifiedSince()
   {
      return this.unmodifiedSince;
   }

   /**
    * Sets the "if-unmodified-since" condition.
    * @param date The "if-unmodified-since" condition.
    */
   public void setUnmodifiedSince(Date date)
   {
      this.modifiedSince = date;
   }

   /**
    * Returns the "if-match" condition.
    * @return The "if-match" condition.
    */
   public List<Tag> getMatch()
   {
      return this.match;
   }
   
   /**
    * Sets the "if-match" condition.
    * @param tags The "if-match" condition.
    */
   public void setMatch(List<Tag> tags)
   {
      this.match = tags;
   }

   /**
    * Returns the "if-none-match" condition.
    * @return The "if-none-match" condition.
    */
   public List<Tag> getNoneMatch()
   {
      return this.noneMatch;
   }
   
   /**
    * Sets the "if-none-match" condition.
    * @param tags The "if-none-match" condition.
    */
   public void setNoneMatch(List<Tag> tags)
   {
      this.noneMatch = tags;
   }

}
