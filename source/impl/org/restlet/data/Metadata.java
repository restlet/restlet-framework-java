/*
 * Copyright © 2005 Jérôme LOUVEL.  All Rights Reserved.
 */

package org.restlet.data;

/**
 * Description of data contained in a resource representation.<br/><br/>
 * "A representation consists of data, metadata describing the data, and, on occasion, metadata to describe the metadata
 * (usually for the purpose of verifying message integrity). Metadata is in the form of name-value pairs, where the name
 * corresponds to a standard that defines the value's structure and semantics. Response messages may include both
 * representation metadata and resource metadata: information about the resource that is not specific to the supplied
 * representation." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_1_2">Source dissertation</a>
 */
public interface Metadata extends Data
{
   /**
    * Returns the metadata name like "text/html" or "compress" or "iso-8851-1".
    * @return The metadata name like "text/html" or "compress" or "iso-8851-1".
    */
   public String getName();

}




