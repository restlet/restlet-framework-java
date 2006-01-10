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

package com.noelios.restlet.util;

import java.util.Date;

/**
 * Date manipulation utilities.
 */
public class DateUtils
{
   /**
    * Compares two date with a precision set to the second.
    * @param baseDate The base date
    * @param afterDate The date supposed to be after.
    * @return True if the afterDate is indeed after the baseDate.
    */
   public static boolean after(Date baseDate, Date afterDate)
   {
      long baseTime = baseDate.getTime() / 1000;
      long afterTime = afterDate.getTime() / 1000;
      return baseTime < afterTime;
   }

   /**
    * Compares two date with a precision set to the second.
    * @param baseDate The base date
    * @param beforeDate The date supposed to be before.
    * @return True if the beforeDate is indeed before the baseDate.
    */
   public static boolean before(Date baseDate, Date beforeDate)
   {
      long baseTime = baseDate.getTime() / 1000;
      long beforeTime = beforeDate.getTime() / 1000;
      return beforeTime < baseTime;
   }

   /**
    * Compares two date with a precision set to the second.
    * @param baseDate The base date
    * @param otherDate The other date supposed to be equals.
    * @return True if both dates are equals.
    */
   public static boolean equals(Date baseDate, Date otherDate)
   {
      long baseTime = baseDate.getTime() / 1000;
      long otherTime = otherDate.getTime() / 1000;
      return otherTime == baseTime;
   }
   
}
