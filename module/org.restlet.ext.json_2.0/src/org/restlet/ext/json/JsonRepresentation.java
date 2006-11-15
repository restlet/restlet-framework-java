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

package org.restlet.ext.json;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

/**
 * Representation based on a JSON document.
 * JSON stands for JavaScript Object Notation and is a lightweight data-interchange format.
 * @author Jerome Louvel (contact@noelios.com)
 * @see <a href="http://www.json.org">JSON home</a>
 */
public class JsonRepresentation extends StringRepresentation
{
	/**
	 * Constructor.
	 * @param jsonRepresentation A source JSON representation to parse.
	 */
	public JsonRepresentation(Representation jsonRepresentation) throws IOException
	{
		super(jsonRepresentation.getValue(), MediaType.APPLICATION_JSON);
	}

	/**
	 * Constructor from a JSON object.
	 * @param jsonObject The JSON object.
	 */
	public JsonRepresentation(JSONObject jsonObject)
	{
		super(jsonObject.toString(), MediaType.APPLICATION_JSON);
	}

	/**
	 * Constructor from a JSON array.
	 * @param jsonArray The JSON array.
	 */
	public JsonRepresentation(JSONArray jsonArray)
	{
		super(jsonArray.toString(), MediaType.APPLICATION_JSON);
	}

	/**
	 * Constructor from a JSON string.
	 * @param jsonString The JSON string.
	 */
	public JsonRepresentation(String jsonString)
	{
		super(jsonString, MediaType.APPLICATION_JSON);
	}

	/**
	 * Converts the representation to a JSON object.
	 * @return The converted JSON object.
	 * @throws JSONException 
	 */
	public JSONObject toJsonObject() throws JSONException
	{
		return new JSONObject(getValue());
	}

	/**
	 * Converts the representation to a JSON array.
	 * @return The converted JSON array.
	 * @throws JSONException 
	 */
	public JSONArray toJsonArray() throws JSONException
	{
		return new JSONArray(getValue());
	}

}
