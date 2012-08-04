package org.restlet.ext.swagger;

public class ItemInfo {

    private String href;

    private String type;

    public ItemInfo(String type, String href) {
        super();
        this.type = type;
        this.href = href;
    }

    public String getHref() {
        return href;
    }

    public String getType() {
        return type;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public void setType(String type) {
        this.type = type;
    }

}
