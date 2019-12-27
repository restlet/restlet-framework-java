package dataLoader.data;

/**
 * A label and its three formats.
 */
public class Label {
    private String full;

    private String medium;

    private String shortLabel;

    public Label() {
        super();
    }

    public Label(String shortLabel, String medium, String full) {
        super();
        this.shortLabel = shortLabel;
        this.medium = medium;
        this.full = full;
    }

    public String getFull() {
        return full;
    }

    public String getMedium() {
        return medium;
    }

    public String getShort() {
        return shortLabel;
    }

    public void setFull(String full) {
        this.full = full;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public void setShort(String shortLabel) {
        this.shortLabel = shortLabel;
    }

}
