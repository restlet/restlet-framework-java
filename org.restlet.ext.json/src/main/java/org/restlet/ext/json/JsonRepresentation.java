/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
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
 * Representation based on a JSON document. JSON stands for JavaScript Object Notation and is a
 * lightweight data-interchange format.
 *
 * @author Jerome Louvel
 * @see <a href="http://www.json.org">JSON home</a>
 */
public class JsonRepresentation extends WriterRepresentation {

    /** Indicates if JSON objects and arrays should be indented. */
    private boolean indenting;

    /** Number of spaces to use for indentation. */
    private int indentingSize;

    /** The wrapped JSON representation. */
    private Representation wrappedJsonRepresentation;

    /** The wrapped JSON value. */
    private Object jsonValue;

    /**
     * Constructor from a JSON array.
     *
     * @param jsonArray The JSON array.
     */
    public JsonRepresentation(JSONArray jsonArray) {
        super(MediaType.APPLICATION_JSON);
        init(jsonArray);
    }

    /**
     * Constructor from a JSON object.
     *
     * @param jsonObject The JSON object.
     */
    public JsonRepresentation(JSONObject jsonObject) {
        super(MediaType.APPLICATION_JSON);
        init(jsonObject);
    }

    /**
     * Constructor from a JSON stringer.
     *
     * @param jsonStringer The JSON stringer.
     */
    public JsonRepresentation(JSONStringer jsonStringer) {
        super(MediaType.APPLICATION_JSON);
        init(jsonStringer);
    }

    /**
     * Constructor from a JSON tokener.
     *
     * @param jsonTokener The JSON tokener.
     */
    public JsonRepresentation(JSONTokener jsonTokener) {
        super(MediaType.APPLICATION_JSON);
        init(jsonTokener);
    }

    /**
     * Constructor from a map object.
     *
     * @param map The map to convert to JSON.
     * @see org.json.JSONObject#JSONObject(Map)
     */
    public JsonRepresentation(Map<String, Object> map) {
        this(new JSONObject(map));
    }

    /**
     * Constructor from a bean using reflection to generate JSON names.
     *
     * @param bean The bean to convert to JSON.
     * @see org.json.JSONObject#JSONObject(Object)
     */
    public JsonRepresentation(Object bean) {
        this(new JSONObject(bean));
    }

    /**
     * Constructor.
     *
     * @param wrappedJsonRepresentation A source JSON representation to parse.
     */
    public JsonRepresentation(Representation wrappedJsonRepresentation) {
        super(
                (wrappedJsonRepresentation == null)
                        ? null
                        : wrappedJsonRepresentation.getMediaType());
        this.wrappedJsonRepresentation = wrappedJsonRepresentation;
    }

    /**
     * Constructor from a JSON string.
     *
     * @param jsonString The JSON string.
     */
    public JsonRepresentation(String jsonString) {
        super(MediaType.APPLICATION_JSON);
        setCharacterSet(CharacterSet.UTF_8);
        this.wrappedJsonRepresentation = new StringRepresentation(jsonString);
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
     * Gets the wrapped JSON array or converts the wrapped representation if needed.
     *
     * @return The converted JSON array.
     * @throws JSONException
     */
    public JSONArray getJsonArray() throws JSONException {
        if (this.jsonValue != null) {
            return (JSONArray) this.jsonValue;
        }

        final String jsonText = getJsonText();
        return (jsonText == null) ? null : new JSONArray(jsonText);
    }

    /**
     * Gets the wrapped JSON object or converts the wrapped representation if needed.
     *
     * @return The converted JSON object.
     * @throws JSONException
     */
    public JSONObject getJsonObject() throws JSONException {
        if (this.jsonValue != null) {
            return (JSONObject) this.jsonValue;
        }

        final String jsonText = getJsonText();
        return (jsonText == null) ? null : new JSONObject(getJsonText());
    }

    /**
     * Returns the JSON text for the wrapped JSON object or representation.
     *
     * @return The JSON text.
     * @throws JSONException
     */
    private String getJsonText() throws JSONException {
        final String result;

        if (this.jsonValue != null) {
            result = getJsonTextFromJsonValue();
        } else if (this.wrappedJsonRepresentation != null) {
            try {
                result = this.wrappedJsonRepresentation.getText();
            } catch (IOException e) {
                throw new JSONException(e);
            }
        } else {
            result = null;
        }

        return result;
    }

    private String getJsonTextFromJsonValue() {
        final String result;
        switch (this.jsonValue) {
            case final JSONArray jsonArray -> {
                if (isIndenting()) {
                    result = jsonArray.toString(getIndentingSize());
                } else {
                    result = jsonArray.toString();
                }
            }
            case final JSONObject jsonObject -> {
                if (isIndenting()) {
                    result = jsonObject.toString(getIndentingSize());
                } else {
                    result = jsonObject.toString();
                }
            }
            case final JSONStringer jsonStringer -> result = jsonStringer.toString();
            case final JSONTokener jsonTokener -> result = jsonTokener.toString();
            case null, default -> result = null;
        }
        return result;
    }

    /**
     * Gets the wrapped JSON tokener or converts the wrapped representation if needed.
     *
     * @return The converted JSON tokener.
     * @throws JSONException
     */
    public JSONTokener getJsonTokener() throws JSONException {
        if (this.jsonValue != null) {
            return (JSONTokener) this.jsonValue;
        }

        final String jsonText = getJsonText();
        return (jsonText == null) ? null : new JSONTokener(getJsonText());
    }

    @Override
    public long getSize() {
        if (this.wrappedJsonRepresentation != null) {
            return this.wrappedJsonRepresentation.getSize();
        }
        return super.getSize();
    }

    /**
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
     * @param indenting True if JSON objects and arrays should be indented.
     */
    public void setIndenting(boolean indenting) {
        this.indenting = indenting;
    }

    /**
     * Sets the number of spaces to use for indentation.
     *
     * @param indentFactor The number of spaces to use for indentation.
     */
    public void setIndentingSize(int indentFactor) {
        this.indentingSize = indentFactor;
    }

    @Override
    public void write(Writer writer) throws IOException {
        try {
            final String jsonText = getJsonText();
            if (jsonText != null) {
                writer.write(jsonText);
            }
        } catch (JSONException e) {
            throw new IOException(e.getLocalizedMessage(), e);
        }
    }
}
