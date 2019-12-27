package dataLoader.data;

public class Category extends Identified {

    private String description;

    private String label;

    public Category() {
        this(null);
    }

    public Category(String id) {
        super(id);
    }

    public String getDescription() {
        return description;
    }

    public String getLabel() {
        return label;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLabel(String label) {
        this.label = label;
    }

}
