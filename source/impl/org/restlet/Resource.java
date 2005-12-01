/*
 * Copyright 2005 Jérôme LOUVEL
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

import java.util.List;

import org.restlet.data.Representation;
import org.restlet.data.RepresentationMetadata;

/**
 * Intended conceptual target of a hypertext reference.<br/><br/> "Any information that can be named can be
 * a resource: a document or image, a temporal service (e.g. "today's weather in Los Angeles"), a collection
 * of other resources, a non-virtual object (e.g. a person), and so on. In other words, any concept that might
 * be the target of an author's hypertext reference must fit within the definition of a resource."<br/><br/>
 * "The only thing that is required to be static for a resource is the semantics of the mapping, since the
 * semantics is what distinguishes one resource from another." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/rest_arch_style.htm#sec_5_2_1_1">Source
 * dissertation</a>
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
    * @param metadata The metadata to match.
    * @return The matching representation.
    */
   public Representation getRepresentation(RepresentationMetadata metadata);

}
