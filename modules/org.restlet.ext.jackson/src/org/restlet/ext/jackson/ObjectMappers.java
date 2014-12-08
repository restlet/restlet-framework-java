package org.restlet.ext.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvFactory;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;

/**
 * List of default object mappers for each supported media type.
 *
 * Each default object mappers could be overridden for customize the default behaviour.
 *
 * @author Manuel Boillod
 */
public class ObjectMappers {

    /**
     * Simple {@link ObjectMapper} factory
     */
    public static interface ObjectMapperFactory {
        /**
         * Returns a new instance of {@link ObjectMapper}.
         */
        ObjectMapper newObjectMapper();
    }

    /** Default object mapper for JSON media type */
    private static ObjectMapperFactory DEFAULT_JSON_MAPPER_FACTORY = new JsonMapperFactory();
    /** Default object mapper for JSON SMILE media type */
    private static ObjectMapperFactory DEFAULT_JSON_SMILE_MAPPER_FACTORY = new JsonSmileMapperFactory();
    /** Default object mapper for XML media type */
    private static ObjectMapperFactory DEFAULT_XML_MAPPER_FACTORY = new XmlMapperFactory();
    /** Default object mapper for YAML media type */
    private static ObjectMapperFactory DEFAULT_YAML_MAPPER_FACTORY = new YamlMapperFactory();
    /** Default object mapper for CSV media type */
    private static ObjectMapperFactory DEFAULT_CSV_MAPPER_FACTORY = new CsvMapperFactory();

    /** Returns the default object mapper for JSON media type */
    public static ObjectMapperFactory getDefaultJsonMapperFactory() {
        return DEFAULT_JSON_MAPPER_FACTORY;
    }

    /** Define the default object mapper for JSON media type */
    public static void setDefaultJsonMapperFactory(ObjectMapperFactory DEFAULT_JSON_MAPPER_FACTORY) {
        ObjectMappers.DEFAULT_JSON_MAPPER_FACTORY = DEFAULT_JSON_MAPPER_FACTORY;
    }

    /** Returns the default object mapper for JSON SMILE media type */
    public static ObjectMapperFactory getDefaultJsonSmileMapperFactory() {
        return DEFAULT_JSON_SMILE_MAPPER_FACTORY;
    }

    /** Define the default object mapper for JSON SMILE media type */
    public static void setDefaultJsonSmileMapperFactory(ObjectMapperFactory DEFAULT_JSON_SMILE_MAPPER_FACTORY) {
        ObjectMappers.DEFAULT_JSON_SMILE_MAPPER_FACTORY = DEFAULT_JSON_SMILE_MAPPER_FACTORY;
    }

    /** Returns the default object mapper for XML media type */
    public static ObjectMapperFactory getDefaultXmlMapperFactory() {
        return DEFAULT_XML_MAPPER_FACTORY;
    }

    /** Define the default object mapper for XML media type */
    public static void setDefaultXmlMapperFactory(ObjectMapperFactory DEFAULT_XML_MAPPER_FACTORY) {
        ObjectMappers.DEFAULT_XML_MAPPER_FACTORY = DEFAULT_XML_MAPPER_FACTORY;
    }

    /** Returns the default object mapper for YAML media type */
    public static ObjectMapperFactory getDefaultYamlMapperFactory() {
        return DEFAULT_YAML_MAPPER_FACTORY;
    }

    /** Define the default object mapper for YAML media type */
    public static void setDefaultYamlMapperFactory(ObjectMapperFactory DEFAULT_YAML_MAPPER_FACTORY) {
        ObjectMappers.DEFAULT_YAML_MAPPER_FACTORY = DEFAULT_YAML_MAPPER_FACTORY;
    }

    /** Returns the default object mapper for CSV media type */
    public static ObjectMapperFactory getDefaultCsvMapperFactory() {
        return DEFAULT_CSV_MAPPER_FACTORY;
    }

    /** Define the default object mapper for CSV media type */
    public static void setDefaultCsvMapperFactory(ObjectMapperFactory DEFAULT_CSV_MAPPER_FACTORY) {
        ObjectMappers.DEFAULT_CSV_MAPPER_FACTORY = DEFAULT_CSV_MAPPER_FACTORY;
    }

    /** Default {@link ObjectMapperFactory} for JSON media type */
    public static class JsonMapperFactory implements ObjectMapperFactory {
        @Override
        public ObjectMapper newObjectMapper() {
            JsonFactory jsonFactory = new JsonFactory();
            jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
            ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
            objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            return objectMapper;
        }
    }

    /** Default {@link ObjectMapperFactory} for JSON SMILE media type */
    public static class JsonSmileMapperFactory implements ObjectMapperFactory {
        @Override
        public ObjectMapper newObjectMapper() {
            JsonFactory jsonFactory = new JsonFactory();
            jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
            ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
            objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            return objectMapper;
        }
    }

    /** Default {@link ObjectMapperFactory} for XML media type */
    public static class XmlMapperFactory implements ObjectMapperFactory {
        /**
         * True for expanding entity references when parsing XML representations;
         * default value provided by system property
         * "org.restlet.ext.xml.expandingEntityRefs", false by default.
         */
        public static boolean XML_EXPANDING_ENTITY_REFS = Boolean
                .getBoolean("org.restlet.ext.xml.expandingEntityRefs");

        /**
         * True for validating DTD documents when parsing XML representations;
         * default value provided by system property
         * "org.restlet.ext.xml.validatingDtd", false by default.
         */
        public static boolean XML_VALIDATING_DTD = Boolean
                .getBoolean("org.restlet.ext.xml.validatingDtd");

        @Override
        public ObjectMapper newObjectMapper() {
            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, XML_EXPANDING_ENTITY_REFS);
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, XML_EXPANDING_ENTITY_REFS);
            xif.setProperty(XMLInputFactory.IS_VALIDATING, XML_VALIDATING_DTD);
            XMLOutputFactory xof = XMLOutputFactory.newFactory();
            XmlFactory xmlFactory = new XmlFactory(xif, xof);
            xmlFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
            ObjectMapper objectMapper = new XmlMapper(xmlFactory);
            objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            return objectMapper;
        }
    }

    /** Default {@link ObjectMapperFactory} for YAML media type */
    public static class YamlMapperFactory implements ObjectMapperFactory {
        @Override
        public ObjectMapper newObjectMapper() {
            YAMLFactory yamlFactory = new YAMLFactory();
            yamlFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
            ObjectMapper objectMapper = new ObjectMapper(yamlFactory);
            objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            return objectMapper;
        }
    }

    /** Default {@link ObjectMapperFactory} for CSV media type */
    public static class CsvMapperFactory implements ObjectMapperFactory {
        @Override
        public ObjectMapper newObjectMapper() {
            CsvFactory csvFactory = new CsvFactory();
            csvFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
            ObjectMapper objectMapper = new CsvMapper(csvFactory);
            objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            return objectMapper;
        }
    }
}
