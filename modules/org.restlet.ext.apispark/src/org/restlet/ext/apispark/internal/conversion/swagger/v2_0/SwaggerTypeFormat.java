package org.restlet.ext.apispark.internal.conversion.swagger.v2_0;
/**
 * Internal class representing a Swagger type
 */
public class SwaggerTypeFormat {
    private String type;

    private String format;

    public SwaggerTypeFormat(String type) {
        this(type, null);
    }

    public SwaggerTypeFormat(String type, String format) {
        this.type = type;
        this.format = format;
    }

    public String getType() {
        return type;
    }

    public String getFormat() {
        return format;
    }
}