/*
 * Copyright © 2005 Noelios Conseil.  All Rights Reserved.
 */

package org.restlet.data;

/**
 * Abstract resource representation.
 */
public abstract class AbstractRepresentation extends DefaultRepresentationMetadata implements Representation
{
   /**
    * Constructor.
    * @param mediaType The media type.
    */
   public AbstractRepresentation(MediaType mediaType)
   {
      super(mediaType);
   }

   /**
    * Returns the metadata.
    * @return The metadata.
    */
   public RepresentationMetadata getMetadata()
   {
      return this;
   }
   
   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Resource representation";
   }

   /**
    * Returns the metadata name like "text/html" or "compress" or "iso-8851-1".
    * @return The metadata name like "text/html" or "compress" or "iso-8851-1".
    */
   public String getName()
   {
      return "representation";
   }

}




