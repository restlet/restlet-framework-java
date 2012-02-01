/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
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

    /** Indicates if JSON objects and arrays should be indented. */
    private boolean indenting;

    /** Number of spaces to use for indentation. */
    private int indentingSize;

    /** The wrapped JSON value. */
    private Object jsonValue;

    /** The wrapped JSON representation. */
    private Representation jsonRepresentation;

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
    public JsonRepresentation(Map<String, Object> map) {
        this(new JSONObject(map));
    }

    // [ifndef android] method
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
    public int getIndentingSize() {
        return indentingSize;
    }

    /**
     * Gets the wrapped JSON array or converts the wrapped representation if
     * needed.
     * 
     * @return The converted JSON array.
     * @throws JSONException
     */
    public JSONArray getJsonArray() throws JSONException {
        if (this.jsonValue != null) {
            return (JSONArray) this.jsonValue;
        }

        return new JSONArray(getJsonText());
    }

    /**
     * Gets the wrapped JSON object or converts the wrapped representation if
     * needed.
     * 
     * @return The converted JSON object.
     * @throws JSONException
     */
    public JSONObject getJsonObject() throws JSONException {
        if (this.jsonValue != null) {
            return (JSONObject) this.jsonValue;
        }

        return new JSONObject(getJsonText());
    }

    /**
     * Returns the JSON text for the wrapped JSON object or representation.
     * 
     * @return The JSON text.
     * @throws JSONException
     */
    private String getJsonText() throws JSONException {
        String result = null;

        if (this.jsonValue != null) {
            if (this.jsonValue instanceof JSONArray) {
                JSONArray jsonArray = (JSONArray) this.jsonValue;

                if (isIndenting()) {
                    result = jsonArray.toString(getIndentingSize());
                } else {
                    result = jsonArray.toString();
                }
            } else if (this.jsonValue instanceof JSONObject) {
                JSONObject jsonObject = (JSONObject) this.jsonValue;

                if (isIndenting()) {
                    result = jsonObject.toString(getIndentingSize());
                } else {
                    result = jsonObject.toString();
                }
            } else if (this.jsonValue instanceof JSONStringer) {
                JSONStringer jsonStringer = (JSONStringer) this.jsonValue;
                result = jsonStringer.toString();
            } else if (this.jsonValue instanceof JSONTokener) {
                JSONTokener jsonTokener = (JSONTokener) this.jsonValue;
                result = jsonTokener.toString();
            }
        } else if (this.jsonRepresentation != null) {
            try {
                result = this.jsonRepresentation.getText();
            } catch (IOException e) {
                // [ifndef android] instruction
                throw new JSONException(e);
                // [ifdef android] instruction uncomment
                // throw new JSONException(e.getMessage());
            }
        }

        return result;
    }

    /**
     * Gets the wrapped JSON tokener or converts the wrapped representation if
     * needed.
     * 
     * @return The converted JSON tokener.
     * @throws JSONException
     */
    public JSONTokener getJsonTokener() throws JSONException {
        if (this.jsonValue != null) {
            return (JSONTokener) this.jsonValue;
        }

        return toJsonTokener();
    }

    @Override
    public long getSize() {
        if (this.jsonRepresentation != null) {
            return this.jsonRepresentation.getSize();
        }
        return super.getSize();
    }

    /**
     * 
     * @param jsonObject
     */
    private void init(Object jsonObject) {
        setCharacterSet(CharacterSet.UTF_8);
        this.jsonValue = jsonObject;
        this.indenting = false;
        this.indentingSize = 3;
    }

    /**
     * Indicates if JSON objects and arrays should be indented.
     * 
     * @return True if JSON objects and arrays should be indented.
     */
    public boolean isIndenting() {
        return indenting;
    }

    /**
     * Indicates if JSON objects and arrays should be indented.
     * 
     * @param indenting
     *            True if JSON objects and arrays should be indented.
     */
    public void setIndenting(boolean indenting) {
        this.indenting = indenting;
    }

    /**
     * Sets the number of spaces to use for indentation.
     * 
     * @param indentFactor
     *            The number of spaces to use for indentation.
     */
    public void setIndentingSize(int indentFactor) {
        this.indentingSize = indentFactor;
    }

    /**
     * Converts the representation to a JSON tokener. This method will trigger
     * the serialization of any wrapped JSON tokener.
     * 
     * @return The converted JSON tokener.
     * @throws JSONException
     * @deprecated Use {@link #getJsonTokener()} instead.
     */
    @Deprecated
    public JSONTokener toJsonTokener() throws JSONException {
        return new JSONTokener(getJsonText());
    }

    @Override
    public void write(Writer writer) throws IOException {
        try {
            writer.write(getJsonText());
        } catch (JSONException e) {
            IOException ioe = new IOException(e.getLocalizedMessage());
            ioe.initCause(e.getCause());
            throw ioe;
        }
    }
}
