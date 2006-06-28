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

import java.util.List;

/**
 * Media type preference.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class MediaTypePref extends Preference
{
   /**
    * Constructor.
    * @param mediaType The associated media type.
    */
   public MediaTypePref(MediaType mediaType)
   {
      super(mediaType, 1F, null);
   }

   /**
    * Constructor.
    * @param mediaType The associated media type.
    * @param quality The quality/preference level.
    */
   public MediaTypePref(MediaType mediaType, float quality)
   {
      super(mediaType, quality, null);
   }

   /**
    * Constructor.
    * @param mediaType The associated media type.
    * @param quality The quality/preference level.
    * @param parameters The list of parameters.
    */
   public MediaTypePref(MediaType mediaType, float quality, List<Parameter> parameters)
   {
      super(mediaType, quality, parameters);
   }

   /**
    * Returns the media type associated with this preference.
    * @return The media type associated with this preference.
    */
   public MediaType getMediaType()
   {
      return (MediaType)getMetadata();
   }
}
