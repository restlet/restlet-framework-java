/*
 * Copyright © 2005 Jérôme LOUVEL.  All Rights Reserved.
 */

package org.restlet.data;

/**
 * Character set used to encode characters representations.
 * @see org.restlet.data.CharacterSets
 */
public interface CharacterSet extends Metadata
{
   /**
    * Indicates if the character set is equal to a given one.
    * @param characterSet  The character set to compare to.
    * @return              True if the character set is equal to a given one.
    */
   public boolean equals(CharacterSet characterSet);
   
}




