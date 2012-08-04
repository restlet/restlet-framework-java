package org.restlet.ext.swagger;

import java.util.List;

public class PropertyInfo {

    private String description;

    private List<String> enums;

    private List<ItemInfo> items;

    private String name;

    private String type;

    public PropertyInfo(String description, String name, String type) {
        super();
        this.description = description;
        this.name = name;
        this.type = type;
    }

    public PropertyInfo(String name, String type) {
        this(name, type, null);
    }

    public String getDescription() {
        return description;
    }

    public List<String> getEnums() {
        return enums;
    }

    public List<ItemInfo> getItems() {
        return items;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnums(List<String> enums) {
        this.enums = enums;
    }

    public void setItems(List<ItemInfo> items) {
        this.items = items;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

}
