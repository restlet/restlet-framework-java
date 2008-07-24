/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.gwt.resource;

import org.restlet.gwt.data.MediaType;

import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;

/**
 * JSON representation based on an JSON value. JSON (JavaScript Object Notation)
 * is a common serialization format similar to XML but lighter.
 * 
 * @see <a href="http://www.json.org">JSON<a/>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class JsonRepresentation extends Representation {
    /** The source JSON representation. */
    private volatile Representation jsonRepresentation;

    /** The wrapped JSON value. */
    private volatile JSONValue value;

    /**
     * Constructor for an empty document.
     * 
     * @param mediaType
     *            The representation's media type.
     */
    public JsonRepresentation(MediaType mediaType) {
        super(mediaType);
        this.value = JSONNull.getInstance();
    }

    /**
     * Constructor from an existing DOM document.
     * 
     * @param mediaType
     *            The representation's media type.
     * @param value
     *            The source JSON value.
     */
    public JsonRepresentation(MediaType mediaType, JSONValue value) {
        super(mediaType);
        this.value = value;
    }

    /**
     * Constructor.
     * 
     * @param jsonRepresentation
     *            A source JSON representation to parse.
     */
    public JsonRepresentation(Representation jsonRepresentation) {
        super((jsonRepresentation == null) ? null : jsonRepresentation
                .getMediaType());
        this.jsonRepresentation = jsonRepresentation;
    }

    @Override
    public String getText() {
        return (getValue() != null) ? getValue().toString() : null;
    }

    /**
     * Returns the wrapped JSON value. If no value is defined yet, it attempts
     * to parse the JSON representation eventually given at construction time.
     * Otherwise, it just creates a null JSON value.
     * 
     * @return The wrapped DOM document.
     */
    public JSONValue getValue() {
        if (this.value == null) {
            if (this.jsonRepresentation != null) {
                this.value = JSONParser
                        .parse(this.jsonRepresentation.getText());
            } else {
                this.value = JSONNull.getInstance();
            }
        }

        return this.value;
    }

    /**
     * Releases the wrapped JSON value and the source JSON representation if
     * they have been defined.
     */
    @Override
    public void release() {
        setValue(null);

        if (this.jsonRepresentation != null) {
            this.jsonRepresentation.release();
        }

        super.release();
    }

    /**
     * Sets the wrapped JSON value.
     * 
     * @param json
     *            The wrapped JSON value.
     */
    public void setValue(JSONValue json) {
        this.value = json;
    }
}
