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

package org.restlet.ext.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvFactory;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.restlet.data.MediaType;
import org.restlet.ext.jackson.internal.XmlFactoryProvider;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Representation based on the Jackson library. It can serialize and deserialize
 * automatically in JSON, JSON binary (Smile), XML, YAML and CSV. <br>
 * <br>
 * SECURITY WARNING: Using XML parsers configured to not prevent nor limit
 * document type definition (DTD) entity resolution can expose the parser to an
 * XML Entity Expansion injection attack.
 * 
 * @see <a href="http://jackson.codehaus.org/">Jackson project</a>
 * @see <a
 *      href="https://github.com/restlet/restlet-framework-java/wiki/XEE-security-enhancements">XML
 *      Entity Expansion injection attack</a>
 * @author Jerome Louvel
 * @param <T>
 *            The type to wrap.
 */
public class JacksonRepresentation<T> extends OutputRepresentation {
    // [ifndef android] member
    /**
     * True for expanding entity references when parsing XML representations.
     * Default value provided by system property
     * "org.restlet.ext.xml.expandingEntityRefs", false by default.
     */
    public final static boolean XML_EXPANDING_ENTITY_REFS = Boolean
            .getBoolean("org.restlet.ext.xml.expandingEntityRefs");

    // [ifndef android] member
    /**
     * True for validating DTD documents when parsing XML representations.
     * Default value provided by system property
     * "org.restlet.ext.xml.validatingDtd", false by default.
     */
    public final static boolean XML_VALIDATING_DTD = Boolean
            .getBoolean("org.restlet.ext.xml.validatingDtd");

    /** The modifiable Jackson CSV schema. */
    private CsvSchema csvSchema;

    // [ifndef android] member
    /**
     * Specifies that the parser will expand entity reference nodes. By default
     * the value of this is set to false.
     */
    private volatile boolean expandingEntityRefs;

    /** The (parsed) object to format. */
    private volatile T object;

    /** The object class to instantiate. */
    private volatile Class<T> objectClass;

    /** The modifiable Jackson object mapper. */
    private volatile ObjectMapper objectMapper;

    /** The modifiable Jackson object reader. */
    private volatile ObjectReader objectReader;

    /** The modifiable Jackson object writer. */
    private volatile ObjectWriter objectWriter;

    /** The representation to parse. */
    private volatile Representation representation;

    // [ifndef android] member
    /**
     * Indicates the desire for validating this type of XML representations
     * against a DTD. Note that for XML schema or Relax NG validation, use the
     * "schema" property instead.
     * 
     * @see javax.xml.parsers.DocumentBuilderFactory#setValidating(boolean)
     */
    private volatile boolean validatingDtd;

    /**
     * Constructor.
     * 
     * @param mediaType
     *            The target media type.
     * @param object
     *            The object to format.
     */
    @SuppressWarnings("unchecked")
    public JacksonRepresentation(MediaType mediaType, T object) {
        super(mediaType);
        this.object = object;
        this.objectClass = (Class<T>) ((object == null) ? null : object
                .getClass());
        this.representation = null;
        this.objectMapper = null;
        this.objectReader = null;
        this.objectWriter = null;
        this.csvSchema = null;
        // [ifndef android] instruction
        this.expandingEntityRefs = XML_EXPANDING_ENTITY_REFS;
        // [ifndef android] instruction
        this.validatingDtd = XML_VALIDATING_DTD;
    }

    /**
     * Constructor.
     * 
     * @param representation
     *            The representation to parse.
     * @param objectClass
     *            The object class to instantiate.
     */
    public JacksonRepresentation(Representation representation,
            Class<T> objectClass) {
        super(representation.getMediaType());
        this.object = null;
        this.objectClass = objectClass;
        this.representation = representation;
        this.objectMapper = null;
        this.objectReader = null;
        this.objectWriter = null;
        this.csvSchema = null;
        // [ifndef android] instruction
        this.expandingEntityRefs = XML_EXPANDING_ENTITY_REFS;
        // [ifndef android] instruction
        this.validatingDtd = XML_VALIDATING_DTD;
    }

    /**
     * Constructor for the JSON media type.
     * 
     * @param object
     *            The object to format.
     */
    public JacksonRepresentation(T object) {
        this(MediaType.APPLICATION_JSON, object);
    }

    /**
     * Creates a Jackson CSV schema based on a mapper and the current object
     * class.
     * 
     * @param csvMapper
     *            The source CSV mapper.
     * @return A Jackson CSV schema
     */
    protected CsvSchema createCsvSchema(CsvMapper csvMapper) {
        return csvMapper.schemaFor(getObjectClass());
    }

    /**
     * Creates a Jackson object mapper based on a media type. It supports JSON,
     * JSON Smile, XML, YAML and CSV.
     * 
     * @return The Jackson object mapper.
     */
    protected ObjectMapper createObjectMapper() {
        ObjectMapper result = null;

        if (MediaType.APPLICATION_JSON.isCompatible(getMediaType())) {
            JsonFactory jsonFactory = new JsonFactory();
            jsonFactory.configure(Feature.AUTO_CLOSE_TARGET, false);
            result = new ObjectMapper(jsonFactory);
        } else if (MediaType.APPLICATION_JSON_SMILE
                .isCompatible(getMediaType())) {
            SmileFactory smileFactory = new SmileFactory();
            smileFactory.configure(Feature.AUTO_CLOSE_TARGET, false);
            result = new ObjectMapper(smileFactory);
            // [ifndef android]
        } else if (MediaType.APPLICATION_XML.isCompatible(getMediaType())
                || MediaType.TEXT_XML.isCompatible(getMediaType())) {
            javax.xml.stream.XMLInputFactory xif = XmlFactoryProvider.newInputFactory();
            xif.setProperty(
                    javax.xml.stream.XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
                    isExpandingEntityRefs());
            xif.setProperty(javax.xml.stream.XMLInputFactory.SUPPORT_DTD,
                    isExpandingEntityRefs());
            xif.setProperty(javax.xml.stream.XMLInputFactory.IS_VALIDATING,
                    isValidatingDtd());
            javax.xml.stream.XMLOutputFactory xof = XmlFactoryProvider.newOutputFactory();
            XmlFactory xmlFactory = new XmlFactory(xif, xof);
            xmlFactory.configure(Feature.AUTO_CLOSE_TARGET, false);
            result = new XmlMapper(xmlFactory);
            // [enddef]
        } else if (MediaType.APPLICATION_YAML.isCompatible(getMediaType())
                || MediaType.TEXT_YAML.isCompatible(getMediaType())) {
            YAMLFactory yamlFactory = new YAMLFactory();
            yamlFactory.configure(Feature.AUTO_CLOSE_TARGET, false);
            result = new ObjectMapper(yamlFactory);
        } else if (MediaType.TEXT_CSV.isCompatible(getMediaType())) {
            CsvFactory csvFactory = new CsvFactory();
            csvFactory.configure(Feature.AUTO_CLOSE_TARGET, false);
            result = new CsvMapper(csvFactory);
        } else {
            JsonFactory jsonFactory = new JsonFactory();
            jsonFactory.configure(Feature.AUTO_CLOSE_TARGET, false);
            result = new ObjectMapper(jsonFactory);
        }

        return result;
    }

    /**
     * Creates a Jackson object reader based on a mapper. Has a special handling
     * for CSV media types.
     * 
     * @return The Jackson object reader.
     */
    protected ObjectReader createObjectReader() {
        ObjectReader result = null;

        if (MediaType.TEXT_CSV.isCompatible(getMediaType())) {
            CsvMapper csvMapper = (CsvMapper) getObjectMapper();
            CsvSchema csvSchema = createCsvSchema(csvMapper);
            result = csvMapper.reader(getObjectClass()).with(csvSchema);
        } else {
            result = getObjectMapper().reader(getObjectClass());
        }

        return result;
    }

    /**
     * Creates a Jackson object writer based on a mapper. Has a special handling
     * for CSV media types.
     * 
     * @return The Jackson object writer.
     */
    protected ObjectWriter createObjectWriter() {
        ObjectWriter result = null;

        if (MediaType.TEXT_CSV.isCompatible(getMediaType())) {
            CsvMapper csvMapper = (CsvMapper) getObjectMapper();
            CsvSchema csvSchema = createCsvSchema(csvMapper);
            result = csvMapper.writer(csvSchema);
        } else {
            result = getObjectMapper().writerWithType(getObjectClass());
        }

        return result;
    }

    /**
     * Returns the modifiable Jackson CSV schema.
     * 
     * @return The modifiable Jackson CSV schema.
     */
    public CsvSchema getCsvSchema() {
        if (this.csvSchema == null) {
            this.csvSchema = createCsvSchema((CsvMapper) getObjectMapper());
        }

        return this.csvSchema;
    }

    /**
     * Returns the wrapped object, deserializing the representation with Jackson
     * if necessary.
     * 
     * @return The wrapped object.
     * @throws IOException
     */
    public T getObject() throws IOException {
        T result = null;

        if (this.object != null) {
            result = this.object;
        } else if (this.representation != null) {
            result = getObjectReader().readValue(
                    this.representation.getStream());
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
     * Returns the modifiable Jackson object mapper. Useful to customize
     * mappings.
     * 
     * @return The modifiable Jackson object mapper.
     */
    public ObjectMapper getObjectMapper() {
        if (this.objectMapper == null) {
            this.objectMapper = createObjectMapper();
        }

        return this.objectMapper;
    }

    /**
     * Returns the modifiable Jackson object reader. Useful to customize
     * deserialization.
     * 
     * @return The modifiable Jackson object reader.
     */
    public ObjectReader getObjectReader() {
        if (this.objectReader == null) {
            this.objectReader = createObjectReader();
        }

        return this.objectReader;
    }

    /**
     * Returns the modifiable Jackson object writer. Useful to customize
     * serialization.
     * 
     * @return The modifiable Jackson object writer.
     */
    public ObjectWriter getObjectWriter() {
        if (this.objectWriter == null) {
            this.objectWriter = createObjectWriter();
        }

        return this.objectWriter;
    }

    // [ifndef android] method
    /**
     * Indicates if the parser will expand entity reference nodes. By default
     * the value of this is set to true.
     * 
     * @return True if the parser will expand entity reference nodes.
     */
    public boolean isExpandingEntityRefs() {
        return expandingEntityRefs;
    }

    // [ifndef android] method
    /**
     * Indicates the desire for validating this type of XML representations
     * against an XML schema if one is referenced within the contents.
     * 
     * @return True if the schema-based validation is enabled.
     */
    public boolean isValidatingDtd() {
        return validatingDtd;
    }

    /**
     * Sets the Jackson CSV schema.
     * 
     * @param csvSchema
     *            The Jackson CSV schema.
     */
    public void setCsvSchema(CsvSchema csvSchema) {
        this.csvSchema = csvSchema;
    }

    // [ifndef android] method
    /**
     * Indicates if the parser will expand entity reference nodes. By default
     * the value of this is set to true.
     * 
     * @param expandEntityRefs
     *            True if the parser will expand entity reference nodes.
     */
    public void setExpandingEntityRefs(boolean expandEntityRefs) {
        this.expandingEntityRefs = expandEntityRefs;
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

    /**
     * Sets the Jackson object mapper.
     * 
     * @param objectMapper
     *            The Jackson object mapper.
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Sets the Jackson object reader.
     * 
     * @param objectReader
     *            The Jackson object reader.
     */
    public void setObjectReader(ObjectReader objectReader) {
        this.objectReader = objectReader;
    }

    /**
     * Sets the Jackson object writer.
     * 
     * @param objectWriter
     *            The Jackson object writer.
     */
    public void setObjectWriter(ObjectWriter objectWriter) {
        this.objectWriter = objectWriter;
    }

    // [ifndef android] method
    /**
     * Indicates the desire for validating this type of XML representations
     * against an XML schema if one is referenced within the contents.
     * 
     * @param validating
     *            The new validation flag to set.
     */
    public void setValidatingDtd(boolean validating) {
        this.validatingDtd = validating;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        if (representation != null) {
            representation.write(outputStream);
        } else if (object != null) {
            getObjectWriter().writeValue(outputStream, object);
        }
    }
}
