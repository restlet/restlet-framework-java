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

package org.restlet.ext.jackson;

import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Converter between the JSON and Representation classes based on Jackson.
 * 
 * @author Jerome Louvel
 */
public class JacksonConverter extends ConverterHelper {
    /** Variant with media type application/xml. */
    private static final VariantInfo VARIANT_APPLICATION_XML = new VariantInfo(
            MediaType.APPLICATION_XML);

    /** Variant with media type application/yaml. */
    private static final VariantInfo VARIANT_APPLICATION_YAML = new VariantInfo(
            MediaType.APPLICATION_YAML);

    /** Variant with media type application/json. */
    private static final VariantInfo VARIANT_JSON = new VariantInfo(
            MediaType.APPLICATION_JSON);

    /** Variant with media type application/x-json-smile. */
    private static final VariantInfo VARIANT_JSON_SMILE = new VariantInfo(
            MediaType.APPLICATION_JSON_SMILE);

    /** Variant with media type text/csv. */
    private static final VariantInfo VARIANT_TEXT_CSV = new VariantInfo(
            MediaType.TEXT_CSV);

    /** Variant with media type text/xml. */
    private static final VariantInfo VARIANT_TEXT_XML = new VariantInfo(
            MediaType.TEXT_XML);

    /** Variant with media type text/yaml. */
    private static final VariantInfo VARIANT_TEXT_YAML = new VariantInfo(
            MediaType.TEXT_YAML);

    /** The modifiable Jackson binary object mapper. */
    private ObjectMapper binaryObjectMapper;

    /** The modifiable Jackson csv mapper. */
    private ObjectMapper csvMapper;

    /** The modifiable Jackson object mapper. */
    private ObjectMapper objectMapper;

    /** The modifiable Jackson xml mapper. */
    private ObjectMapper xmlMapper;

    /** The modifiable Jackson yaml mapper. */
    private ObjectMapper yamlMapper;

    /**
     * Creates the marshaling {@link JacksonRepresentation}.
     * 
     * @param <T>
     * @param mediaType
     *            The target media type.
     * @param source
     *            The source object to marshal.
     * @return The marshaling {@link JacksonRepresentation}.
     */
    protected <T> JacksonRepresentation<T> create(MediaType mediaType, T source) {
        JacksonRepresentation<T> result = new JacksonRepresentation<T>(
                mediaType, source);
        result.setObjectMapper(getObjectMapper());
        return result;
    }

    /**
     * Creates the unmarshaling {@link JacksonRepresentation}.
     * 
     * @param <T>
     * @param source
     *            The source representation to unmarshal.
     * @param objectClass
     *            The object class to instantiate.
     * @return The unmarshaling {@link JacksonRepresentation}.
     */
    protected <T> JacksonRepresentation<T> create(Representation source,
            Class<T> objectClass) {
        JacksonRepresentation<T> result = new JacksonRepresentation<T>(source,
                objectClass);
        result.setObjectMapper(getObjectMapper());
        return result;
    }

    /**
     * Creates the marshaling {@link JacksonSmileRepresentation}.
     * 
     * @param <T>
     * @param mediaType
     *            The target media type.
     * @param source
     *            The source object to marshal.
     * @return The marshaling {@link JacksonSmileRepresentation}.
     */
    protected <T> JacksonSmileRepresentation<T> createBinary(
            MediaType mediaType, T source) {
        JacksonSmileRepresentation<T> result = new JacksonSmileRepresentation<T>(
                mediaType, source);
        result.setObjectMapper(getBinaryObjectMapper());
        return result;
    }

    /**
     * Creates the unmarshaling {@link JacksonSmileRepresentation}.
     * 
     * @param <T>
     * @param source
     *            The source representation to unmarshal.
     * @param objectClass
     *            The object class to instantiate.
     * @return The unmarshaling {@link JacksonSmileRepresentation}.
     */
    protected <T> JacksonSmileRepresentation<T> createBinary(
            Representation source, Class<T> objectClass) {
        JacksonSmileRepresentation<T> result = new JacksonSmileRepresentation<T>(
                source, objectClass);
        result.setObjectMapper(getBinaryObjectMapper());
        return result;
    }

    /**
     * Creates a Jackson object mapper for binary representations based on a
     * media type. By default, it calls
     * {@link ObjectMapper#ObjectMapper(JsonFactory)} with a
     * {@link SmileFactory}.
     * 
     * @return The Jackson object mapper.
     */
    protected ObjectMapper createBinaryObjectMapper() {
        JsonFactory jsonFactory = new SmileFactory();
        jsonFactory.configure(Feature.AUTO_CLOSE_TARGET, false);
        return new ObjectMapper(jsonFactory);
    }

    /**
     * Creates the marshaling {@link JacksonRepresentation}.
     * 
     * @param <T>
     * @param mediaType
     *            The target media type.
     * @param source
     *            The source object to marshal.
     * @return The marshaling {@link JacksonRepresentation}.
     */
    protected <T> JacksonRepresentation<T> createCsv(MediaType mediaType,
            T source) {
        JacksonRepresentation<T> result = new JacksonRepresentation<T>(
                mediaType, source);
        result.setObjectMapper(getCsvMapper());
        return result;
    }

    /**
     * Creates the unmarshaling {@link JacksonRepresentation}.
     * 
     * @param <T>
     * @param source
     *            The source representation to unmarshal.
     * @param objectClass
     *            The object class to instantiate.
     * @return The unmarshaling {@link JacksonRepresentation}.
     */
    protected <T> JacksonRepresentation<T> createCsv(Representation source,
            Class<T> objectClass) {
        JacksonRepresentation<T> result = new JacksonRepresentation<T>(source,
                objectClass);
        result.setObjectMapper(getCsvMapper());
        return result;
    }

    /**
     * Creates a Jackson csv mapper based on a media type. By default, it calls
     * {@link CsvMapper()}.
     * 
     * @return The Jackson object mapper.
     */
    protected ObjectMapper createCsvMapper() {
        return new CsvMapper();
    }

    /**
     * Creates a Jackson object mapper based on a media type. By default, it
     * calls {@link ObjectMapper#ObjectMapper(JsonFactory)}.
     * 
     * @return The Jackson object mapper.
     */
    protected ObjectMapper createObjectMapper() {
        JsonFactory jsonFactory = new JsonFactory();
        jsonFactory.configure(Feature.AUTO_CLOSE_TARGET, false);
        return new ObjectMapper(jsonFactory);
    }

    /**
     * Creates the marshaling {@link JacksonRepresentation}.
     * 
     * @param <T>
     * @param mediaType
     *            The target media type.
     * @param source
     *            The source object to marshal.
     * @return The marshaling {@link JacksonRepresentation}.
     */
    protected <T> JacksonRepresentation<T> createXml(MediaType mediaType,
            T source) {
        JacksonRepresentation<T> result = new JacksonRepresentation<T>(
                mediaType, source);
        result.setObjectMapper(getXmlMapper());
        return result;
    }

    /**
     * Creates the unmarshaling {@link JacksonRepresentation}.
     * 
     * @param <T>
     * @param source
     *            The source representation to unmarshal.
     * @param objectClass
     *            The object class to instantiate.
     * @return The unmarshaling {@link JacksonRepresentation}.
     */
    protected <T> JacksonRepresentation<T> createXml(Representation source,
            Class<T> objectClass) {
        JacksonRepresentation<T> result = new JacksonRepresentation<T>(source,
                objectClass);
        result.setObjectMapper(getXmlMapper());
        return result;
    }

    /**
     * Creates a Jackson xml mapper based on a media type. By default, it calls
     * {@link XmlMapper#XmlMapper(XmlFactory)}.
     * 
     * @return The Jackson object mapper.
     */
    protected ObjectMapper createXmlMapper() {
        XmlFactory xmlFactory = new XmlFactory();
        xmlFactory.configure(Feature.AUTO_CLOSE_TARGET, false);
        return new XmlMapper(xmlFactory);
    }

    /**
     * Creates the marshaling {@link JacksonRepresentation}.
     * 
     * @param <T>
     * @param mediaType
     *            The target media type.
     * @param source
     *            The source object to marshal.
     * @return The marshaling {@link JacksonRepresentation}.
     */
    protected <T> JacksonRepresentation<T> createYaml(MediaType mediaType,
            T source) {
        JacksonRepresentation<T> result = new JacksonRepresentation<T>(
                mediaType, source);
        result.setObjectMapper(getYamlMapper());
        return result;
    }

    /**
     * Creates the unmarshaling {@link JacksonRepresentation}.
     * 
     * @param <T>
     * @param source
     *            The source representation to unmarshal.
     * @param objectClass
     *            The object class to instantiate.
     * @return The unmarshaling {@link JacksonRepresentation}.
     */
    protected <T> JacksonRepresentation<T> createYaml(Representation source,
            Class<T> objectClass) {
        JacksonRepresentation<T> result = new JacksonRepresentation<T>(source,
                objectClass);
        result.setObjectMapper(getYamlMapper());
        return result;
    }

    /**
     * Creates a Jackson yaml mapper based on a media type. By default, it calls
     * {@link ObjectMapper#ObjectMapper(YAMLFactory)}.
     * 
     * @return The Jackson object mapper.
     */
    protected ObjectMapper createYamlMapper() {
        YAMLFactory yamlFactory = new YAMLFactory();
        yamlFactory.configure(Feature.AUTO_CLOSE_TARGET, false);
        return new ObjectMapper(yamlFactory);
    }

    /**
     * Returns the modifiable Jackson binary object mapper. Useful to customize
     * mappings.
     * 
     * @return The modifiable Jackson binary object mapper.
     */
    public ObjectMapper getBinaryObjectMapper() {
        if (this.binaryObjectMapper == null) {
            synchronized (this) {
                if (this.binaryObjectMapper == null) {
                    this.binaryObjectMapper = createBinaryObjectMapper();
                }
            }
        }

        return this.binaryObjectMapper;
    }

    public ObjectMapper getCsvMapper() {
        if (this.csvMapper == null) {
            synchronized (this) {
                if (this.csvMapper == null) {
                    this.csvMapper = createCsvMapper();
                }
            }
        }

        return this.csvMapper;
    }

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (VARIANT_JSON.isCompatible(source)
                || VARIANT_JSON_SMILE.isCompatible(source)
                || VARIANT_APPLICATION_XML.isCompatible(source)
                || VARIANT_TEXT_XML.isCompatible(source)
                || VARIANT_APPLICATION_YAML.isCompatible(source)
                || VARIANT_TEXT_YAML.isCompatible(source)
                || VARIANT_TEXT_CSV.isCompatible(source)) {
            result = addObjectClass(result, Object.class);
            result = addObjectClass(result, JacksonRepresentation.class);
            result = addObjectClass(result, JacksonSmileRepresentation.class);
        }

        return result;
    }

    /**
     * Returns the modifiable Jackson object mapper. Useful to customize
     * mappings.
     * 
     * @return The modifiable Jackson object mapper.
     */
    public ObjectMapper getObjectMapper() {
        if (this.objectMapper == null) {
            synchronized (this) {
                if (this.objectMapper == null) {
                    this.objectMapper = createObjectMapper();
                }
            }
        }

        return this.objectMapper;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (source != null) {
            result = addVariant(result, VARIANT_JSON);
            result = addVariant(result, VARIANT_JSON_SMILE);
            result = addVariant(result, VARIANT_APPLICATION_XML);
            result = addVariant(result, VARIANT_TEXT_XML);
            result = addVariant(result, VARIANT_APPLICATION_YAML);
            result = addVariant(result, VARIANT_TEXT_YAML);
            result = addVariant(result, VARIANT_TEXT_CSV);
        }

        return result;
    }

    public ObjectMapper getXmlMapper() {
        if (this.xmlMapper == null) {
            synchronized (this) {
                if (this.xmlMapper == null) {
                    this.xmlMapper = createXmlMapper();
                }
            }
        }

        return this.xmlMapper;
    }

    public ObjectMapper getYamlMapper() {
        if (this.yamlMapper == null) {
            synchronized (this) {
                if (this.yamlMapper == null) {
                    this.yamlMapper = createYamlMapper();
                }
            }
        }

        return this.yamlMapper;
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        float result = -1.0F;

        if (source instanceof JacksonRepresentation<?>) {
            result = 1.0F;
        } else if (source instanceof JacksonSmileRepresentation<?>) {
            result = 1.0F;
        } else {
            if (target == null) {
                result = 0.5F;
            } else if (VARIANT_JSON.isCompatible(target)) {
                result = 0.8F;
            } else if (VARIANT_JSON_SMILE.isCompatible(target)) {
                result = 0.8F;
            } else if (VARIANT_APPLICATION_XML.isCompatible(target)) {
                result = 0.8F;
            } else if (VARIANT_TEXT_XML.isCompatible(target)) {
                result = 0.8F;
            } else if (VARIANT_APPLICATION_YAML.isCompatible(target)) {
                result = 0.8F;
            } else if (VARIANT_TEXT_YAML.isCompatible(target)) {
                result = 0.8F;
            } else if (VARIANT_TEXT_CSV.isCompatible(target)) {
                result = 0.8F;
            } else {
                result = 0.5F;
            }
        }

        return result;
    }

    @Override
    public <T> float score(Representation source, Class<T> target,
            Resource resource) {
        float result = -1.0F;

        if (source instanceof JacksonRepresentation<?>) {
            result = 1.0F;
        } else if (source instanceof JacksonSmileRepresentation<?>) {
            result = 1.0F;
        } else if ((target != null)
                && JacksonRepresentation.class.isAssignableFrom(target)) {
            result = 1.0F;
        } else if ((target != null)
                && JacksonSmileRepresentation.class.isAssignableFrom(target)) {
            result = 1.0F;
        } else if (VARIANT_JSON.isCompatible(source)) {
            result = 0.8F;
        } else if (VARIANT_JSON_SMILE.isCompatible(source)) {
            result = 0.8F;
        } else if (VARIANT_APPLICATION_XML.isCompatible(source)) {
            result = 0.8F;
        } else if (VARIANT_TEXT_XML.isCompatible(source)) {
            result = 0.8F;
        } else if (VARIANT_APPLICATION_YAML.isCompatible(source)) {
            result = 0.8F;
        } else if (VARIANT_TEXT_YAML.isCompatible(source)) {
            result = 0.8F;
        } else if (VARIANT_TEXT_CSV.isCompatible(source)) {
            result = 0.8F;
        }

        return result;
    }

    /**
     * Sets the Jackson binary object mapper.
     * 
     * @param objectMapper
     *            The Jackson binary object mapper.
     */
    public void setBinaryObjectMapper(ObjectMapper objectMapper) {
        this.binaryObjectMapper = objectMapper;
    }

    /**
     * Sets the Jackson object mapper.
     * 
     * @param objectMapper
     *            The Jackson object mapper.
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setXmlMapper(ObjectMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation source, Class<T> target,
            Resource resource) throws IOException {
        Object result = null;

        JacksonSmileRepresentation<?> bSource = null;
        if (source instanceof JacksonSmileRepresentation) {
            bSource = (JacksonSmileRepresentation<?>) source;
        } else if (VARIANT_JSON_SMILE.isCompatible(source)) {
            bSource = createBinary(source, target);
        }
        if (bSource != null) {
            // Handle the conversion
            if ((target != null)
                    && JacksonSmileRepresentation.class
                            .isAssignableFrom(target)) {
                result = bSource;
            } else {
                result = bSource.getObject();
            }
        } else {
            // The source for the Jackson conversion
            JacksonRepresentation<?> jacksonSource = null;
            if (source instanceof JacksonRepresentation) {
                jacksonSource = (JacksonRepresentation<?>) source;
            } else if (VARIANT_JSON.isCompatible(source)) {
                jacksonSource = create(source, target);
            } else if (VARIANT_APPLICATION_XML.isCompatible(source)
                    || VARIANT_TEXT_XML.isCompatible(source)) {
                jacksonSource = createXml(source, target);
            } else if (VARIANT_APPLICATION_YAML.isCompatible(source)
                    || VARIANT_TEXT_YAML.isCompatible(source)) {
                jacksonSource = createYaml(source, target);
            } else if (VARIANT_TEXT_CSV.isCompatible(source)) {
                jacksonSource = createCsv(source, target);
            }

            if (jacksonSource != null) {
                // Handle the conversion
                if ((target != null)
                        && JacksonRepresentation.class.isAssignableFrom(target)) {
                    result = jacksonSource;
                } else {
                    result = jacksonSource.getObject();
                }
            }
        }

        return (T) result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            Resource resource) {
        Representation result = null;

        if (source instanceof JacksonRepresentation) {
            result = (JacksonRepresentation<?>) source;
        } else if (source instanceof JacksonSmileRepresentation) {
            result = (JacksonSmileRepresentation<?>) source;
        } else {
            if (target.getMediaType() == null) {
                target.setMediaType(MediaType.APPLICATION_JSON);
            }
            if (VARIANT_JSON_SMILE.isCompatible(target)) {
                result = createBinary(target.getMediaType(), source);
            } else if (VARIANT_JSON.isCompatible(target)) {
                result = create(target.getMediaType(), source);
            } else if (VARIANT_APPLICATION_XML.isCompatible(target)
                    || VARIANT_TEXT_XML.isCompatible(target)) {
                result = createXml(target.getMediaType(), source);
            } else if (VARIANT_APPLICATION_YAML.isCompatible(target)
                    || VARIANT_TEXT_YAML.isCompatible(target)) {
                result = createYaml(target.getMediaType(), source);
            } else if (VARIANT_TEXT_CSV.isCompatible(target)) {
                result = createCsv(target.getMediaType(), source);
            }
        }

        return result;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        updatePreferences(preferences, MediaType.APPLICATION_JSON, 1.0F);
        updatePreferences(preferences, MediaType.APPLICATION_JSON_SMILE, 1.0F);
        updatePreferences(preferences, MediaType.APPLICATION_XML, 1.0F);
        updatePreferences(preferences, MediaType.TEXT_XML, 1.0F);
        updatePreferences(preferences, MediaType.APPLICATION_YAML, 1.0F);
        updatePreferences(preferences, MediaType.TEXT_YAML, 1.0F);
        updatePreferences(preferences, MediaType.TEXT_CSV, 1.0F);
    }

}
