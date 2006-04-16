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

package org.restlet.data;

/**
 * Modifier of a representation's media type.<br>
 * Useful to apply compression without losing the identity of the underlying media type.
 * @see org.restlet.data.Encodings
 */
public interface Encoding extends Metadata
{
   /**
    * Indicates if the encoding equal to a given one.
    * @param encoding The encoding to compare to.
    * @return True if the encoding is equal to a given one.
    */
   public boolean equals(Encoding encoding);

}
