package dataLoader.data;

public class Identified implements Comparable<Identified> {
    /** Identifier. */
    private String id;

    /**
     * Constructor with parameter.
     * 
     * @param id
     *            Identifier.
     */
    public Identified(String id) {
        super();
        this.id = id;
    }

    public int compareTo(Identified o) {
        return getId().compareTo(o.getId());
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Identified)
                && (((Identified) obj).getId().equals(getId()));
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return getId();
    }

}
