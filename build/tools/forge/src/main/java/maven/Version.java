package maven;

public class Version {
    private String lastUpdated;

    private String snapshotBuildNumber;

    private String snapshotTimestamp;

    private String version;

    public String getLastUpdated() {
        return this.lastUpdated;
    }

    public String getSnapshotBuildNumber() {
        return this.snapshotBuildNumber;
    }

    public String getSnapshotTimestamp() {
        return this.snapshotTimestamp;
    }

    public String getVersion() {
        return this.version;
    }

    public boolean isSnapshot() {
        return (this.version != null) && (this.version.endsWith("-SNAPSHOT"));
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public void setSnapshotBuildNumber(String snapshotBuildNumber) {
        this.snapshotBuildNumber = snapshotBuildNumber;
    }

    public void setSnapshotTimestamp(String snapshotTimestamp) {
        this.snapshotTimestamp = snapshotTimestamp;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}