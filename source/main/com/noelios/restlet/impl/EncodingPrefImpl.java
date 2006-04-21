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

package com.noelios.restlet.impl;

import java.util.List;

import org.restlet.data.Encoding;
import org.restlet.data.EncodingPref;
import org.restlet.data.Parameter;

/**
 * Encoding preference.
 */
public class EncodingPrefImpl extends PreferenceImpl implements EncodingPref
{
   /**
    * Constructor.
    * @param encoding The associated encoding.
    */
   public EncodingPrefImpl(Encoding encoding)
   {
      super(encoding, 1F, null);
   }

   /**
    * Constructor.
    * @param encoding The associated encoding.
    * @param quality The quality/preference level.
    */
   public EncodingPrefImpl(Encoding encoding, float quality)
   {
      super(encoding, quality, null);
   }

   /**
    * Constructor.
    * @param encoding The associated encoding.
    * @param quality The quality/preference level.
    * @param parameters The list of parameters.
    */
   public EncodingPrefImpl(Encoding encoding, float quality, List<Parameter> parameters)
   {
      super(encoding, quality, parameters);
   }

   /**
    * Returns the encoding associated with this preference.
    * @return The encoding associated with this preference.
    */
   public Encoding getEncoding()
   {
      return (Encoding)getMetadata();
   }
}
