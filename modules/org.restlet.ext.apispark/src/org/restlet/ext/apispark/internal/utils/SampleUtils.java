package org.restlet.ext.apispark.internal.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.ext.apispark.internal.introspection.util.Types;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.service.MetadataService;

public class SampleUtils {

    private static final List<String> supportedExtensions = Arrays.asList(
            "xml", "yaml", "json", "jsonsmile");

    public static String buildSampleRepresentation(Map<String, Object> content,
            String mediaTypeAsString, String representationType)
            throws IOException {
        MetadataService ms = new MetadataService();
        MediaType mediaType = MediaType.valueOf(mediaTypeAsString);
        if (!supportedExtensions.contains(ms.getExtension(mediaType))) {
            return null;
        }
        JacksonRepresentation<Map<String, Object>> result = new JacksonRepresentation<Map<String, Object>>(
                mediaType, content);
        if (ms.getAllMediaTypes("xml").contains(mediaType)) {
            return result.getText().replaceAll("HashMap", representationType);
        }
        return result.getText();
    }

    public static Map<String, Object> buildContent(Representation representation) {
        List<Property> properties = representation.getProperties();
        Map<String, Object> content = new HashMap<String, Object>();
        for (Property property : properties) {
            String fieldName = property.getName();
            Object fieldValue = convertFieldValue(property,
                    property.getExample());
            content.put(fieldName, fieldValue);
        }
        return content;
    }

    private static Object convertFieldValue(Property property, String value) {
        Object defaultValue = Types.getDefaultSampleValue(property.getType());
        return (value != null && defaultValue != null) ? value : defaultValue;
    }
}
