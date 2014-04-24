package org.restlet.ext.apispark;

public class Body {

    /**
     * Indicates whether you should provide an array of [type] or just one
     * [type].
     */
    private boolean array;

    /** Reference of the representation in the body of the message. */
    private String type;

    public String getRepresentation() {
        return type;
    }

    public boolean isArray() {
        return array;
    }

    public void setArray(boolean array) {
        this.array = array;
    }

    public void setRepresentation(String representation) {
        this.type = representation;
    }
}
