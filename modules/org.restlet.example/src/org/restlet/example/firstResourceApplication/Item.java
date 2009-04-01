package org.restlet.example.firstResourceApplication;

public class Item {
    /** A description of the item. */
    private String description;

    /** Name of the item. */
    private String name;

    public Item(String name) {
        super();
        setName(name);
    }

    public Item(String name, String description) {
        super();
        setName(name);
        setDescription(description);
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

}
