package dataLoader.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a library.
 */
public class Library extends Bundle {

    /** The URI used to download this library. */
    private String downloadUri;

    /** The list of exported packages. */
    private List<String> exportedPackages;

    /** The home URI where to find documentation. */
    private String homeUri;

    /**
     * In case the library is neither hosted by the Maven central repository nor
     * the Restlet one.
     */
    private MavenRepository mavenReleaseRepository;

    /**
     * In case the library is neither hosted by the Maven central repository nor
     * the Restlet one.
     */
    private MavenRepository mavenSnapshotRepository;

    /**
     * The list of contained packages (used for libs hosted on Restlet maven
     * repository).
     */
    private List<LibraryPackage> packages;

    /** The path to find this library - deprecated. */
    private String rootDirectory;

    public Library(String id) {
        super(id);
    }

    public String getDownloadUri() {
        return downloadUri;
    }

    public List<String> getExportedPackages() {
        if (exportedPackages == null) {
            exportedPackages = new ArrayList<String>();
        }
        return exportedPackages;
    }

    public String getHomeUri() {
        return homeUri;
    }

    public MavenRepository getMavenReleaseRepository() {
        return mavenReleaseRepository;
    }

    public MavenRepository getMavenSnapshotRepository() {
        return mavenSnapshotRepository;
    }

    @Override
    public Set<String> getNeededPackages() {
        Set<String> result = new TreeSet<String>();
        for (LibraryDependency ld : getNeededLibraries()) {
            result.addAll(ld.getLibrary().getExportedPackages());
            result.addAll(ld.getLibrary().getNeededPackages());
        }
        return result;
    }

    public List<LibraryPackage> getPackages() {
        if (packages == null) {
            packages = new ArrayList<LibraryPackage>();
        }
        return packages;
    }

    @Override
    public String getRootDirectory() {
        if (rootDirectory == null) {
            rootDirectory = getSymbolicName() + "_" + getMinorVersion();
        }
        return rootDirectory;
    }

    public void setDownloadUri(String downloadUri) {
        this.downloadUri = downloadUri;
    }

    public void setExportedPackages(List<String> exportedPackages) {
        this.exportedPackages = exportedPackages;
    }

    public void setHomeUri(String homeUri) {
        this.homeUri = homeUri;
    }

    public void setMavenReleaseRepository(MavenRepository mavenReleaseRepository) {
        this.mavenReleaseRepository = mavenReleaseRepository;
    }

    public void setMavenSnapshotRepository(MavenRepository mavenSnapshotRepository) {
        this.mavenSnapshotRepository = mavenSnapshotRepository;
    }

    public void setPackages(List<LibraryPackage> packages) {
        this.packages = packages;
    }

    public void setRootDirectory(String rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

}
