package org.restlet.ext.swagger.internal.swagger;

public abstract class SwaggerUtils {

    public static SwaggerTypeFormat convertType(String dataType) {
        if ("String".equals(dataType)) {
            return new SwaggerTypeFormat("string");
        } else if ("Integer".equals(dataType)) {
            return new SwaggerTypeFormat("integer", "int32");
        } else if ("Long".equals(dataType)) {
            return new SwaggerTypeFormat("integer", "int64");
        } else if ("Float".equals(dataType)) {
            return new SwaggerTypeFormat("number", "float");
        } else if ("Double".equals(dataType)) {
            return new SwaggerTypeFormat("number", "double");
        } else if ("Boolean".equals(dataType)) {
            return new SwaggerTypeFormat("boolean");
        } else if ("Date".equals(dataType)) {
            return new SwaggerTypeFormat("string", "date");
        } else {
            return new SwaggerTypeFormat(dataType);
        }
    }

    public static class SwaggerTypeFormat {
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
}
