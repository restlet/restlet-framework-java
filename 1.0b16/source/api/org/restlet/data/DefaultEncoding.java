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


/**
 * Modifier of a representation's media type. Useful to apply compression without losing the 
 * identity of the underlying media type.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class DefaultEncoding extends DefaultMetadata implements Encoding
{
   /**
    * Constructor.
    * @param name The name.
    */
   public DefaultEncoding(String name)
   {
      this(name, "Encoding applied to a representation");
   }

   /**
    * Constructor.
    * @param name The name.
    * @param description The description. 
    */
   public DefaultEncoding(String name, String description)
   {
      super(name, description);
   }

   /**
    * Indicates if the encoding is equal to a given one.
    * @param encoding The encoding to compare to.
    * @return True if the encoding is equal to a given one.
    */
   public boolean equals(Encoding encoding)
   {
      return getName().equalsIgnoreCase(encoding.getName());
   }

}
