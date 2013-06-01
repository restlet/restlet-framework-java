package org.restlet.ext.gson;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Inspired from Jackson extension of Restlet
 * 
 * @author nealmi
 * 
 * @param <T>
 * 
 *            Copyright 2012 Neal Mi
 * 
 *            Licensed under the Apache License, Version 2.0 (the "License");
 *            you may not use this file except in compliance with the License.
 *            You may obtain a copy of the License at
 * 
 *            http://www.apache.org/licenses/LICENSE-2.0
 * 
 *            Unless required by applicable law or agreed to in writing,
 *            software distributed under the License is distributed on an
 *            "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *            either express or implied. See the License for the specific
 *            language governing permissions and limitations under the License.
 */
public class GsonRepresentation<T> extends WriterRepresentation {
    /** The (parsed) object to format. */
    private T object;

    /** The object class to instantiate. */
    private Class<T> objectClass;

    /** The JSON representation to parse. */
    private Representation jsonRepresentation;

    /** The modifiable Gson builder. */
    private GsonBuilder builder;

    public GsonRepresentation(MediaType mediaType) {
        super(mediaType);
    }

    @SuppressWarnings("unchecked")
    public GsonRepresentation(T object) {
        super(MediaType.APPLICATION_JSON);
        this.object = object;
        this.objectClass = ((Class<T>) ((object == null) ? null : object.getClass()));
        this.jsonRepresentation = null;
        this.builder = null;
    }

    public GsonRepresentation(Representation representation, Class<T> objectClass) {
        super(representation.getMediaType());
        this.object = null;
        this.objectClass = objectClass;
        this.jsonRepresentation = representation;
        this.builder = null;
    }

    @SuppressWarnings("unchecked")
    public GsonRepresentation(MediaType mediaType, T object) {
        super(mediaType);
        this.object = object;
        this.objectClass = ((Class<T>) ((object == null) ? null : object.getClass()));
        this.jsonRepresentation = null;
        this.builder = null;
    }

    @Override
    public void write(Writer writer) throws IOException {
        if (jsonRepresentation != null) {
            jsonRepresentation.write(writer);
        } else {
            Gson gson = getBuilder().create();
            gson.toJson(object, objectClass, new JsonWriter(writer));
        }
    }

    public T getObject() throws IOException {
        T result = null;

        if (this.object != null) {
            result = this.object;
        } else if (this.jsonRepresentation != null) {
            Gson gson = getBuilder().create();
            result = gson.fromJson(new JsonReader(jsonRepresentation.getReader()), this.objectClass);
        }

        return result;
    }

    public void setObject(T object) {
        this.object = object;
    }

    public Class<T> getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(Class<T> objectClass) {
        this.objectClass = objectClass;
    }

    public Representation getJsonRepresentation() {
        return jsonRepresentation;
    }

    public void setJsonRepresentation(Representation jsonRepresentation) {
        this.jsonRepresentation = jsonRepresentation;
    }

    private class ISODateSerializer implements JsonSerializer<Date> {
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            DateTime dt = new DateTime(src);
            // DateTime dtz = dt.withZone(DateTimeZone.forOffsetHours(-8));
            return new JsonPrimitive(dt.toString());
        }
    }

    private class ISODateDeserializer implements JsonDeserializer<Date> {
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return new DateTime(json.getAsJsonPrimitive().getAsString()).toDate();
        }
    }

    public GsonBuilder getBuilder() {
        if (builder == null) {
            builder = createBuilder().registerTypeAdapter(Date.class, new ISODateSerializer()).registerTypeAdapter(
                    Date.class, new ISODateDeserializer());
        }
        return builder;
    }

    private GsonBuilder createBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat(DateFormat.FULL);
        return gsonBuilder;
    }

    public void setBuilder(GsonBuilder builder) {
        this.builder = builder;
    }

}
