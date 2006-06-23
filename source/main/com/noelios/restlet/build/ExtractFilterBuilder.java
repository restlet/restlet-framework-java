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

package com.noelios.restlet.build;

import com.noelios.restlet.ExtractFilter;

/**
 * Fluent builder for Extract Filters.
 * @author Jerome Louvel (contact[at]noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ExtractFilterBuilder extends FilterBuilder
{
	/**
	 * Constructor.
	 * @param parent The parent builder.
	 * @param node The wrapped node.
	 */
   public ExtractFilterBuilder(ObjectBuilder parent, ExtractFilter node)
   {
      super(parent, node);
   }

   /**
    * Returns the node wrapped by the builder.
    * @return The node wrapped by the builder.
    */
   public ExtractFilter getNode()
   {
      return (ExtractFilter)super.getNode();
   }

   /**
    * Extracts an attribute from the query string of the resource reference. Only the first occurrence
    * of a query string parameter is set.
    * @param attributeName The name of the call attribute to set.
    * @param parameterName The name of the query string parameter to extract.
    * @return The current builder.
    */
   public ExtractFilterBuilder fromQuery(String attributeName, String parameterName)
   {
   	getNode().fromQuery(attributeName, parameterName);
   	return this;
   }

   /**
    * Extracts an attribute from the query string of the resource reference.
    * @param attributeName The name of the call attribute to set.
    * @param parameterName The name of the query string parameter to extract.
    * @param multiple Indicates if the parameters should be set as a List in the attribute value. Useful for repeating parameters.
    * @return The current builder.
    */
   public ExtractFilterBuilder fromQuery(String attributeName, String parameterName, boolean multiple)
   {
   	getNode().fromQuery(attributeName, parameterName, multiple);
   	return this;
   }

   /**
    * Extracts an attribute from the input form. Only the first occurrence
    * of a query string parameter is set.
    * @param attributeName The name of the call attribute to set.
    * @param parameterName The name of the input form parameter to extract.
    * @return The current builder.
    */
   public ExtractFilterBuilder fromInput(String attributeName, String parameterName)
   {
   	getNode().fromInput(attributeName, parameterName);
   	return this;
   }

   /**
    * Extracts an attribute from the input form.
    * @param attributeName The name of the call attribute to set.
    * @param parameterName The name of the input form parameter to extract.
    * @param multiple Indicates if the parameters should be set as a List in the attribute value. Useful for repeating parameters.
    * @return The current builder.
    */
   public ExtractFilterBuilder fromInput(String attributeName, String parameterName, boolean multiple)
   {
   	getNode().fromInput(attributeName, parameterName, multiple);
   	return this;
   }
   /**
    * Extracts an attribute from the context matches.
    * @param attributeName The name of the call attribute to set.
    * @param matchIndex The index of the match to extract from the Call.getMatches() list.
    * @return The current builder.
    */
   public ExtractFilterBuilder fromContext(String attributeName, int matchIndex)
   {
   	getNode().fromContext(attributeName, matchIndex);
      return this;
   }

   /**
    * Extracts an attribute from the call's model.
    * @param attributeName The name of the call attribute to set.
    * @param pattern The model pattern to resolve.
    * @return The current builder.
    * @see com.noelios.restlet.util.CallModel
    */
   public ExtractFilterBuilder fromModel(String attributeName, String pattern)
   {
   	getNode().fromModel(attributeName, pattern);
      return this;
   }

}
