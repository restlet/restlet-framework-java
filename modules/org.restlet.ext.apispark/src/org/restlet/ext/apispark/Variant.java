package org.restlet.ext.apispark;

public class Variant {

    /** Must be a MIME type. */
    private String dataType;

    /** Textual description of this variant. */
    private String description;

    public String getDataType() {
        return dataType;
    }

    public String getDescription() {
        return description;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
