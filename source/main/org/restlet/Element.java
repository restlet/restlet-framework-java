/*
 * Copyright 2005-2006 Jerome LOUVEL
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
 * Element of a software architecture.<br/><br/> "A software architecture is defined by a configuration of
 * architectural elements--components, connectors, and data--constrained in their relationships in order to
 * achieve a desired set of architectural properties." R.Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/software_arch.htm#sec_1_2">Source
 * dissertation</a>
 */
public interface Element
{
   /**
    * Returns the name of this REST element.
    * @return The name of this REST element.
    */
   public String getName();

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription();
}
