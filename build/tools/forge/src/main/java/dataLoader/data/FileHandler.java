package dataLoader.data;

public class FileHandler {

    /** Ant files sets. */
    private String filters;

    /** Ant files mappers. */
    private String mappers;

    /** Ant files sets. */
    private String sets;

    public String getFilters() {
        return filters;
    }

    public String getMappers() {
        return mappers;
    }

    public String getSets() {
        return sets;
    }

    public void setFilters(String filters) {
        this.filters = filters;
    }

    public void setMappers(String mappers) {
        this.mappers = mappers;
    }

    public void setSets(String sets) {
        this.sets = sets;
    }

}
