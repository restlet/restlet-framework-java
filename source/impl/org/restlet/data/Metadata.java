/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.restlet.data;

/**
 * Description of data contained in a resource representation.<br/><br/> "A representation consists of data,
 * metadata describing the data, and, on occasion, metadata to describe the metadata (usually for the purpose
 * of verifying message integrity). Metadata is in the form of name-value pairs, where the name corresponds to
 * a standard that defines the value's structure and semantics. Response messages may include both
 * representation metadata and resource metadata: information about the resource that is not specific to the
 * supplied representation." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_1_2">Source
 * dissertation</a>
 */
public interface Metadata extends Data
{
   /**
    * Returns the metadata name like "text/html" or "compress" or "iso-8851-1".
    * @return The metadata name like "text/html" or "compress" or "iso-8851-1".
    */
   public String getName();

}
