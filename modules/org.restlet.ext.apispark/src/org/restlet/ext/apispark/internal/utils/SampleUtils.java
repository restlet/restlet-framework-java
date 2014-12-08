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

package org.restlet.ext.apispark.internal.utils;

import org.restlet.data.MediaType;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.service.MetadataService;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SampleUtils {

    private static final List<String> supportedExtensions = Arrays.asList(
            "xml", "yaml", "json", "jsonsmile");

    private static final List<String> numberTypes = Arrays.asList("byte",
            "short", "integer", "long");

    private static final List<String> decimalTypes = Arrays.asList("float", "double");

    public static String convertSampleAccordingToMediaType(Map<String, Object> content,
                                                  String mediaTypeAsString,
                                                  String representationName) {
        MetadataService ms = new MetadataService();
        MediaType mediaType = MediaType.valueOf(mediaTypeAsString);
        if (!supportedExtensions.contains(ms.getExtension(mediaType))) {
            return null;
        }
        String text;
        try {
            text = new JacksonRepresentation<>(mediaType, content).getText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (ms.getAllMediaTypes("xml").contains(mediaType)) {
            text = text.replaceAll("HashMap", representationName);
        }
        return text;
    }

    public static Map<String, Object> getRepresentationSample(
            Representation representation) {
        List<Property> properties = representation.getProperties();
        Map<String, Object> content = new HashMap<>();
        for (Property property : properties) {
            content.put(property.getName(), getFieldSampleValue(property));
        }
        return content;
    }

    private static Object getFieldSampleValue(Property property) {
        Object sampleValue = property.getExample() != null ?
                convertSampleValue(property.getType(), property.getExample()) :
                getPropertyDefaultSampleValue(property.getType(), property.getName());

        if (property.getMaxOccurs() != 1) {
            if (sampleValue != null) {
                sampleValue = Arrays.asList(sampleValue);
            } else {
                sampleValue = Arrays.asList();
            }
        }
        return sampleValue;
    }

    public static Object getPropertyDefaultSampleValue(String propertyType, String propertyName) {
        if ("string".equals(propertyType)) {
            return "sample " + propertyName;
        } else if (numberTypes.contains(propertyType)) {
            return 1;
        } else if (decimalTypes.contains(propertyType)) {
            return 1.1F;
        } else if ("boolean".equals(propertyType)) {
            return false;
        } else if ("date".equals(propertyType)) {
            //do not set default value for date because we don't know the expected type
            return null;
        } else {
            return null;
        }
    }

    public static Object convertSampleValue(String propertyType, String sampleValue) {
        if ("string".equals(propertyType)) {
            return sampleValue;
        } else if (numberTypes.contains(propertyType)) {
            return Long.parseLong(sampleValue);
        } else if (decimalTypes.contains(propertyType)) {
            return Double.parseDouble(sampleValue);
        } else if ("boolean".equals(propertyType)) {
            return Boolean.parseBoolean(sampleValue);
        } else if ("date".equals(propertyType)) {
            //do not convert date sample because we don't know the expected type
            return sampleValue;
        } else {
            return null;
        }
    }
}