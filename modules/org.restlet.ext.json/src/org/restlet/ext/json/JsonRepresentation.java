/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.json;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.WriterRepresentation;

/**
 * Representation based on a JSON document. JSON stands for JavaScript Object
 * Notation and is a lightweight data-interchange format.
 * 
 * @author Jerome Louvel
 * @see <a href="http://www.json.org">JSON home</a>
 */
public class JsonRepresentation extends WriterRepresentation {

    /** The wrapped JSON object. */
    private Object jsonObject;

    /** The wrapped JSON representation. */
    private Representation jsonRepresentation;

    /** Number of spaces to use for indentation. */
    private int indentFactor;

    /** Indicates if JSON objects and arrays should be indented. */
    private boolean indent;

    /**
     * Constructor from a JSON array.
     * 
     * @param jsonArray
     *            The JSON array.
     */
    public JsonRepresentation(JSONArray jsonArray) {
        super(MediaType.APPLICATION_JSON);
        init(jsonArray);
    }

    /**
     * Constructor from a JSON object.
     * 
     * @param jsonObject
     *            The JSON object.
     */
    public JsonRepresentation(JSONObject jsonObject) {
        super(MediaType.APPLICATION_JSON);
        init(jsonObject);
    }

    /**
     * Constructor from a JSON stringer.
     * 
     * @param jsonStringer
     *            The JSON stringer.
     */
    public JsonRepresentation(JSONStringer jsonStringer) {
        super(MediaType.APPLICATION_JSON);
        init(jsonStringer);
    }

    /**
     * Constructor from a JSON tokener.
     * 
     * @param jsonTokener
     *            The JSON tokener.
     */
    public JsonRepresentation(JSONTokener jsonTokener) {
        super(MediaType.APPLICATION_JSON);
        init(jsonTokener);
    }

    /**
     * Constructor from a map object.
     * 
     * @param map
     *            The map to convert to JSON.
     * @see org.json.JSONObject#JSONObject(Map)
     */
    public JsonRepresentation(Map<Object, Object> map) {
        this(new JSONObject(map));
    }

    /**
     * Constructor from a bean using reflection to generate JSON names.
     * 
     * @param bean
     *            The bean to convert to JSON.
     * @see org.json.JSONObject#JSONObject(Object)
     */
    public JsonRepresentation(Object bean) {
        this(new JSONObject(bean));
    }

    /**
     * Constructor.
     * 
     * @param jsonRepresentation
     *            A source JSON representation to parse.
     */
    public JsonRepresentation(Representation jsonRepresentation)
            throws IOException {
        super((jsonRepresentation == null) ? null : jsonRepresentation
                .getMediaType());
        this.jsonRepresentation = jsonRepresentation;
    }

    /**
     * Constructor from a JSON string.
     * 
     * @param jsonString
     *            The JSON string.
     */
    public JsonRepresentation(String jsonString) {
        super(MediaType.APPLICATION_JSON);
        setCharacterSet(CharacterSet.UTF_8);
        this.jsonRepresentation = new StringRepresentation(jsonString);
    }

    /**
     * Returns the number of spaces to use for indentation.
     * 
     * @return The number of spaces to use for indentation.
     */
    public int getIndentFactor() {
        return indentFactor;
    }

    /**
     * Returns the JSON text for the wrapped JSON object or representation.
     * 
     * @return The JSON text.
     * @throws JSONException
     */
    private String getJsonText() throws JSONException {
        String result = null;

        if (this.jsonObject != null) {
            if (this.jsonObject instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) this.jsonObject;

                if (isIndent()) {
                    result = jsonArray.toString(getIndentFactor());
                } else {
                    result = jsonArray.toString();
                }
            } else if (this.jsonObject instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) this.jsonObject;

                if (isIndent()) {
                    result = jsonObject.toString(getIndentFactor());
                } else {
                    result = jsonObject.toString();
                }
            } else if (this.jsonObject instanceof JSONStringer) {
                JSONStringer jsonStringer = (JSONStringer) this.jsonObject;
                result = jsonStringer.toString();
            } else if (this.jsonObject instanceof JSONTokener) {
                JSONTokener jsonTokener = (JSONTokener) this.jsonObject;
                result = jsonTokener.toString();
            }
        } else if (this.jsonRepresentation != null) {
            try {
                result = this.jsonRepresentation.getText();
            } catch (IOException e) {
                throw new JSONException(e);
            }
        }

        return result;
    }

    /**
     * 
     * @param jsonObject
     */
    private void init(Object jsonObject) {
        setCharacterSet(CharacterSet.UTF_8);
        this.jsonObject = jsonObject;
        this.indent = false;
        this.indentFactor = 3;
    }

    /**
     * Indicates if JSON objects and arrays should be indented.
     * 
     * @return True if JSON objects and arrays should be indented.
     */
    public boolean isIndent() {
        return indent;
    }

    /**
     * Indicates if JSON objects and arrays should be indented.
     * 
     * @param indent
     *            True if JSON objects and arrays should be indented.
     */
    public void setIndent(boolean indent) {
        this.indent = indent;
    }

    /**
     * Sets the number of spaces to use for indentation.
     * 
     * @param indentFactor
     *            The number of spaces to use for indentation.
     */
    public void setIndentFactor(int indentFactor) {
        this.indentFactor = indentFactor;
    }

    /**
     * Converts the representation to a JSON array.
     * 
     * @return The converted JSON array.
     * @throws JSONException
     */
    public JSONArray toJsonArray() throws JSONException {
        return new JSONArray(getJsonText());
    }

    /**
     * Converts the representation to a JSON object.
     * 
     * @return The converted JSON object.
     * @throws JSONException
     */
    public JSONObject toJsonObject() throws JSONException {
        return new JSONObject(getJsonText());
    }

    @Override
    public void write(Writer writer) throws IOException {
        try {
            writer.write(getJsonText());
        } catch (JSONException e) {
            throw new IOException(e.getLocalizedMessage());
        } finally {
            writer.close();
        }
    }

}
