package org.restlet.ext.apispark.internal.utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.restlet.data.MediaType;
import org.restlet.ext.apispark.internal.introspection.util.Types;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.service.MetadataService;

import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Column;

public class SampleUtils {

    public static String buildSampleRepresentation(Map<String, Object> content,
            String mediaTypeAsString, String representationType)
            throws IOException {
        MediaType mediaType = MediaType.valueOf(mediaTypeAsString);
        JacksonRepresentation<Map<String, Object>> result = new JacksonRepresentation<Map<String, Object>>(
                mediaType, content);
        if (new MetadataService().getAllMediaTypes("xml").contains(mediaType)) {
            return result.getText().replaceAll("HashMap", representationType);
        }
        if (new MetadataService().getAllMediaTypes("csv").contains(mediaType)) {
            char[] columnChar = { '\n' };
            Column[] columns = buildCsvColumns(content);
            CsvSchema empty = CsvSchema.emptySchema();
            CsvSchema schema = new CsvSchema(columns, true, false, ',',
                    empty.getQuoteChar(), empty.getEscapeChar(), columnChar);
            result.setCsvSchema(schema);
        }
        return result.getText();
    }

    private static Column[] buildCsvColumns(Map<String, Object> content) {
        Column[] columns = new Column[content.size()];
        int index = 0;
        for (Entry<String, Object> entry : content.entrySet()) {
            columns[index] = new Column(index++, entry.getKey());
        }
        return columns;
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
