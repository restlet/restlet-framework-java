package org.restlet.ext.apispark.internal.conversion.swagger.v1_2;

/**
 * Internal class representing a Swagger type
 */
public class SwaggerTypeFormat {
    private String format;

    private String type;

    public SwaggerTypeFormat(String type) {
        this(type, null);
    }

    public SwaggerTypeFormat(String type, String format) {
        this.type = type;
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    public String getType() {
        return type;
    }
}