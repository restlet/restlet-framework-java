/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

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
 * Representation based on a JSON document. JSON stands for JavaScript Object
 * Notation and is a lightweight data-interchange format.
 * 
 * @author Neal Mi
 * @see <a href="http://code.google.com/p/google-gson/">Gson project</a>
 */
public class GsonRepresentation<T> extends WriterRepresentation {

    /**
     * Custom deserializer for {@link Date} instances.
     * 
     * @author Neal Mi.
     */
    private class ISODateDeserializer implements JsonDeserializer<Date> {
        public Date deserialize(JsonElement json, Type typeOfT,
                JsonDeserializationContext context) throws JsonParseException {
            return new DateTime(json.getAsJsonPrimitive().getAsString())
                    .toDate();
        }
    }

    /**
     * Custom serializer for {@link Date} instances.
     * 
     * @author Neal Mi.
     */
    private class ISODateSerializer implements JsonSerializer<Date> {
        public JsonElement serialize(Date src, Type typeOfSrc,
                JsonSerializationContext context) {
            DateTime dt = new DateTime(src);
            // DateTime dtz = dt.withZone(DateTimeZone.forOffsetHours(-8));
            return new JsonPrimitive(dt.toString());
        }
    }

    /** The modifiable Gson builder. */
    private GsonBuilder builder;

    /** The JSON representation to parse. */
    private Representation jsonRepresentation;

    /** The (parsed) object to format. */
    private T object;

    /** The object class to instantiate. */
    private Class<T> objectClass;

    /**
     * Constructor.
     * 
     * @param representation
     *            The representation to parse.
     * @param objectClass
     *            The object class to instantiate.
     */
    public GsonRepresentation(Representation representation,
            Class<T> objectClass) {
        super(representation.getMediaType());
        this.object = null;
        this.objectClass = objectClass;
        this.jsonRepresentation = representation;
        this.builder = null;
    }

    /**
     * Constructor for the JSON media type.
     * 
     * @param object
     *            The object to format.
     */
    @SuppressWarnings("unchecked")
    public GsonRepresentation(T object) {
        super(MediaType.APPLICATION_JSON);
        this.object = object;
        this.objectClass = ((Class<T>) ((object == null) ? null : object
                .getClass()));
        this.jsonRepresentation = null;
        this.builder = null;
    }

    /**
     * Returns a new instance of the builder for Gson instances.
     * 
     * @return a new instance of builder for Gson instances.
     */
    protected GsonBuilder createBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat(DateFormat.FULL);
        return gsonBuilder;
    }

    /**
     * Returns the builder for Gson instances.
     * 
     * @return The builder for Gson instances.
     */
    public GsonBuilder getBuilder() {
        if (builder == null) {
            builder = createBuilder().registerTypeAdapter(Date.class,
                    new ISODateSerializer()).registerTypeAdapter(Date.class,
                    new ISODateDeserializer());
        }
        return builder;
    }

    /**
     * Returns the wrapped object, deserializing the representation with Gson if
     * necessary.
     * 
     * @return The wrapped object.
     * @throws IOException
     */
    public T getObject() throws IOException {
        T result = null;

        if (this.object != null) {
            result = this.object;
        } else if (this.jsonRepresentation != null) {
            Gson gson = getBuilder().create();
            result = gson.fromJson(
                    new JsonReader(jsonRepresentation.getReader()),
                    this.objectClass);
        }

        return result;
    }

    /**
     * Returns the object class to instantiate.
     * 
     * @return The object class to instantiate.
     */
    public Class<T> getObjectClass() {
        return objectClass;
    }

    /**
     * Sets the Gson builder.
     * 
     * @param builder
     *            The Gson builder.
     */
    public void setBuilder(GsonBuilder builder) {
        this.builder = builder;
    }

    /**
     * Sets the object to format.
     * 
     * @param object
     *            The object to format.
     */
    public void setObject(T object) {
        this.object = object;
    }

    /**
     * Sets the object class to instantiate.
     * 
     * @param objectClass
     *            The object class to instantiate.
     */
    public void setObjectClass(Class<T> objectClass) {
        this.objectClass = objectClass;
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

}
