/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
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
 * Representation based on a JSON document. JSON stands for JavaScript Object
 * Notation and is a lightweight data-interchange format.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 * @see <a href="http://www.json.org">JSON home</a>
 */
public class JsonRepresentation extends StringRepresentation {
    /**
     * Constructor.
     * 
     * @param jsonRepresentation
     *            A source JSON representation to parse.
     */
    public JsonRepresentation(Representation jsonRepresentation)
            throws IOException {
        super(jsonRepresentation.getText(), MediaType.APPLICATION_JSON);
    }

    /**
     * Constructor from a JSON object.
     * 
     * @param jsonObject
     *            The JSON object.
     */
    public JsonRepresentation(JSONObject jsonObject) {
        super(jsonObject.toString(), MediaType.APPLICATION_JSON);
    }

    /**
     * Constructor from a JSON array.
     * 
     * @param jsonArray
     *            The JSON array.
     */
    public JsonRepresentation(JSONArray jsonArray) {
        super(jsonArray.toString(), MediaType.APPLICATION_JSON);
    }

    /**
     * Constructor from a JSON string.
     * 
     * @param jsonString
     *            The JSON string.
     */
    public JsonRepresentation(String jsonString) {
        super(jsonString, MediaType.APPLICATION_JSON);
    }

    /**
     * Converts the representation to a JSON object.
     * 
     * @return The converted JSON object.
     * @throws JSONException
     */
    public JSONObject toJsonObject() throws JSONException {
        return new JSONObject(getText());
    }

    /**
     * Converts the representation to a JSON array.
     * 
     * @return The converted JSON array.
     * @throws JSONException
     */
    public JSONArray toJsonArray() throws JSONException {
        return new JSONArray(getText());
    }

}
