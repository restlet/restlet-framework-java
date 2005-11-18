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

package org.restlet;

import java.util.List;

import org.restlet.data.Representation;
import org.restlet.data.RepresentationMetadata;

/**
 * Intended conceptual target of a hypertext reference.<br/><br/>
 * "Any information that can be named can be a resource: a document or image, a temporal service (e.g. "today's weather in
 * Los Angeles"), a collection of other resources, a non-virtual object (e.g. a person), and so on. In other words, any
 * concept that might be the target of an author's hypertext reference must fit within the definition of a resource."<br/><br/>
 * "The only thing that is required to be static for a resource is the semantics of the mapping, since the semantics is what
 * distinguishes one resource from another." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_1_1">Source dissertation</a>
 */
public interface Resource
{
   /**
    * Returns the representation variants metadata.
    * @return The representation variants metadata.
    */
   public List<RepresentationMetadata> getVariantsMetadata();

   /**
    * Returns the representation matching the given metadata.
    * @param metadata   The metadata to match.
    * @return 				The matching representation.
    */
   public Representation getRepresentation(RepresentationMetadata metadata);
   
}




