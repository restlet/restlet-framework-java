/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.internal.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.service.MetadataService;

public class SampleUtils {

    private static final List<String> supportedExtensions = Arrays.asList(
            "xml", "yaml", "json", "jsonsmile");

    private static final List<String> numberTypes = Arrays.asList("byte",
            "short", "integer", "long", "float", "double");

    public static String buildSampleAsString(Map<String, Object> content,
            String mediaTypeAsString, String representationType)
            throws IOException {
        MetadataService ms = new MetadataService();
        MediaType mediaType = MediaType.valueOf(mediaTypeAsString);
        if (!supportedExtensions.contains(ms.getExtension(mediaType))) {
            return null;
        }
        org.restlet.representation.Representation result = buildSampleRepresentation(
                content, mediaType, representationType);
        System.out.println(result.getText());
        if (ms.getAllMediaTypes("xml").contains(mediaType)) {
            return result.getText().replaceAll("HashMap", representationType);
        }
        return result.getText();
    }

    public static org.restlet.representation.Representation buildSampleRepresentation(
            Map<String, Object> content, MediaType mediaType,
            String representationType) throws IOException {
        JacksonRepresentation<Map<String, Object>> result = new JacksonRepresentation<Map<String, Object>>(
                mediaType, content);
        return result;
    }

    public static Map<String, Object> buildSampleContent(
            Representation representation) {
        List<Property> properties = representation.getProperties();
        Map<String, Object> content = new HashMap<String, Object>();
        for (Property property : properties) {
            String fieldName = property.getName();
            Object fieldValue = getSampleValue(property, property.getExample());
            content.put(fieldName, fieldValue);
        }
        return content;
    }

    public static Object getSampleValue(Property property, String value) {
        Object result;
        String propertyType = property.getType();

        if ("string".equals(propertyType)) {
            result = "sample " + property.getName();
        } else if (numberTypes.contains(propertyType)) {
            result = 0;
        } else if ("boolean".equals(propertyType)) {
            result = false;
        } else if ("date".equals(propertyType)) {
            result = "Sun, 06 Nov 1994 08:49:37 GMT";
        } else {
            result = null;
        }

        if (property.getMaxOccurs() != 1) {
            result = Arrays.asList(result);
        }

        return result;
    }
}