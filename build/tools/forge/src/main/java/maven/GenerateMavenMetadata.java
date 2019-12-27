package maven;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Parse a full Maven repository and generate the maven-metadata.xml files (for
 * the artifacts aned their versions).
 * 
 * @author Thierry Boileau
 */
public class GenerateMavenMetadata extends Task {

    public static void main(String[] args) throws Exception {
        GenerateMavenMetadata gmm = null;
        if (args != null) {
            if (args.length == 1) {
                gmm = new GenerateMavenMetadata(new File(args[0]));
            } else if (args.length == 2) {
                gmm = new GenerateMavenMetadata(new File(args[0]), new File(
                        args[1]));
            }
        }

        if (gmm != null) {
            gmm.parse();
            gmm.generate();
        } else {
            System.out.println("Command line parameters: arg#1 [arg#2]");
            System.out
                    .println("Where arg#1 is the mandatory full path of the Maven repository.");
            System.out
                    .println("Where arg#1 is the optional full path of the directory where the Maven medata files will be generated. By default, this is the full path of the Maven repository");
        }
    }

    /** Used to sort the versions. */
    private Comparator<Version> comparatorVersions = new Comparator<Version>() {
        public int compare(Version arg0, Version arg1) {
            return arg0.getVersion().compareTo(arg1.getVersion());
        }
    };

    /** Filter that accepts directories and POM files only. */
    private FileFilter filter = new FileFilter() {
        public boolean accept(File pathname) {
            return (pathname.isDirectory())
                    || (pathname.getName().endsWith(".pom"));
        }
    };

    /** The map of parsed groups. */
    private Map<String, Group> groups;

    /** The root directory of the Maven repository. */
    private File mavenDir;

    /** The root directory where the metadata files will be generated. */
    private File outDir;

    /** The number of parsed POM files. */
    private int parsedPomFiles;

    /**
     * The default constructor.
     */
    public GenerateMavenMetadata() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param mavenDir
     *            The root directory of the Maven repository which is also the
     *            directory where the metadata files will be generated.
     */
    public GenerateMavenMetadata(File mavenDir) {
        this(mavenDir, mavenDir);
    }

    /**
     * Constructor.
     * 
     * @param mavenDir
     *            The root directory of the Maven repository.
     * @param outDir
     *            The root directory where the metadata files will be generated.
     */
    public GenerateMavenMetadata(File mavenDir, File outDir) {
        setMavenDir(mavenDir);
        setOutDir(outDir);
        this.groups = new HashMap<String, Group>();
        this.parsedPomFiles = 0;
    }

    /**
     * Checks the arguments.
     * 
     * @param mavenRootDir
     *            The maven repository root directory.
     * @param outDir
     *            The output directory.
     * @throws IllegalArgumentException
     */
    private void check(File mavenRootDir, File outDir)
            throws IllegalArgumentException {
        if (mavenRootDir == null)
            throw new IllegalArgumentException(
                    "Please provide the complete path of the maven repository.");
        if (!mavenRootDir.isDirectory()) {
            throw new IllegalArgumentException(
                    "Please provide the valid path of the maven repository.");
        }
        if (outDir == null) {
            throw new IllegalArgumentException(
                    "Please provide the complete path of the ouput directory.");
        } else if (!outDir.exists()) {
            outDir.mkdirs();
        } else if (!outDir.isDirectory()) {
            throw new IllegalArgumentException(
                    "Please provide a valid ouput directory.");
        }
    }

    @Override
    public void execute() throws BuildException {
        try {
            parse();
            generate();
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }

    /**
     * Generates the maven metadata.
     * 
     * @throws Exception
     */
    public void generate() throws Exception {
        for (Group group : this.groups.values()) {
            File groupDir = new File(getOutDir(), group.getId().replace(".",
                    System.getProperty("file.separator")));
            groupDir.mkdirs();
            for (Artifact artifact : group.getArtifacts().values()) {
                File artifactDir = new File(groupDir, artifact.getId());
                artifactDir.mkdir();
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
                        artifactDir, "maven-metadata.xml")));
                bw.append("<metadata>");
                bw.newLine();
                bw.append("<groupId>").append(group.getId())
                        .append("</groupId>");
                bw.newLine();
                bw.append("<artifactId>").append(artifact.getId())
                        .append("</artifactId>");
                bw.newLine();
                bw.append("<versioning>");
                bw.newLine();
                List<Version> versions = new ArrayList<Version>(artifact
                        .getVersions().values());

                Collections.sort(versions, this.comparatorVersions);
                if (!versions.isEmpty()) {
                    artifact.setLastestVersion(versions.get(versions.size() - 1));
                }
                if (artifact.getLastestVersion() != null) {
                    bw.append("<latest>")
                            .append(artifact.getLastestVersion().getVersion())
                            .append("</latest>");
                    bw.newLine();
                    if (artifact.getLastestVersion().isSnapshot()) {
                        bw.append("<lastUpdated>")
                                .append(artifact.getLastestVersion()
                                        .getLastUpdated())
                                .append("</lastUpdated>");
                        bw.newLine();
                    }
                }
                bw.append("<release/>");
                bw.newLine();
                bw.append("<versions>");
                bw.newLine();
                for (Version version : versions) {
                    bw.append("<version>").append(version.getVersion())
                            .append("</version>");
                    bw.newLine();
                    generateVersionMetadataFile(group, artifact, artifactDir,
                            version);
                }
                bw.append("</versions>");
                bw.newLine();
                bw.append("</versioning>");
                bw.newLine();
                bw.append("</metadata>");
                bw.newLine();
                bw.flush();
                bw.close();
            }
        }
    }

    /**
     * Generate the metadata file for the given group, artifact, version.
     * 
     * @param group
     *            The group.
     * @param artifact
     *            The artifact.
     * @param artifactDir
     *            The root directory of the artifact.
     * @param version
     *            The version.
     * @throws IOException
     */
    private void generateVersionMetadataFile(Group group, Artifact artifact,
            File artifactDir, Version version) throws IOException {
        File versionDir = new File(artifactDir, version.getVersion());
        versionDir.mkdir();
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
                versionDir, "maven-metadata.xml")));
        bw.append("<metadata>");
        bw.newLine();
        bw.append("<groupId>").append(group.getId()).append("</groupId>");
        bw.newLine();
        bw.append("<artifactId>").append(artifact.getId())
                .append("</artifactId>");
        bw.newLine();
        bw.append("<version>").append(version.getVersion())
                .append("</version>");
        bw.newLine();
        if (version.isSnapshot()) {
            bw.append("<versioning>");
            bw.newLine();
            bw.append("<snapshot>");
            bw.newLine();
            bw.append("<timestamp>").append(version.getSnapshotTimestamp())
                    .append("</timestamp>");
            bw.newLine();
            bw.append("<buildNumber>").append(version.getSnapshotBuildNumber())
                    .append("</buildNumber>");
            bw.newLine();
            bw.append("</snapshot>");
            bw.newLine();
            bw.append("<lastUpdated>").append(version.getLastUpdated())
                    .append("</lastUpdated>");
            bw.newLine();
            bw.append("</versioning>");
            bw.newLine();
        }
        bw.append("</metadata>");
        bw.newLine();
        bw.flush();
        bw.close();
    }

    /**
     * Returns the root directory of the Maven repository.
     * 
     * @return The root directory of the Maven repository.
     */
    public File getMavenDir() {
        return this.mavenDir;
    }

    /**
     * Returns the root directory where the metadata files will be generated.
     * 
     * @return The root directory where the metadata files will be generated.
     */
    public File getOutDir() {
        return this.outDir;
    }

    /**
     * Parses the maven repository.
     * 
     * @throws Exception
     */
    public void parse() throws Exception {
        check(this.mavenDir, this.outDir);
        parse(this.mavenDir, this.groups);
        int parsedArtifacts = 0;
        int parsedVersions = 0;
        for (Group g : this.groups.values()) {
            parsedArtifacts += g.getArtifacts().size();
            for (Artifact a : g.getArtifacts().values()) {
                parsedVersions += a.getVersions().size();
            }
        }
        System.out
                .format("Parsed %d pom files: %d group(s), %d artifact(s), %d version(s).\n",
                        Integer.valueOf(this.parsedPomFiles),
                        Integer.valueOf(this.groups.size()),
                        Integer.valueOf(parsedArtifacts),
                        Integer.valueOf(parsedVersions));
    }

    /**
     * Parses a file and completes the map of groups.
     * 
     * @param file
     *            The file (directory or POM file).
     * @param groups
     *            The map of groups to complete.
     */
    private void parse(File file, Map<String, Group> groups) {
        if (file.isDirectory()) {
            File[] files = file.listFiles(this.filter);
            for (File f : files)
                parse(f, groups);
        } else if (file.isFile()) {
            try {
                parsePomFile(file, groups);
            } catch (Throwable e) {
                System.out.println("Cannot parse " + file.getAbsolutePath()
                        + " due to " + e.getMessage());
            }
        }
    }

    /**
     * Generates the metadata for the given POM file.
     * 
     * @param pomFile
     *            The POM file to parse.
     * @param groups
     *            The map of groups to complete.
     * @throws Exception
     */
    private void parsePomFile(File pomFile, Map<String, Group> groups)
            throws Exception {
        this.parsedPomFiles += 1;
        PomReader pomReader = new PomReader(groups);
        try {
            Result result = new SAXResult(pomReader);
            try {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                spf.setNamespaceAware(false);
                spf.setValidating(false);
                spf.setXIncludeAware(false);
                spf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
                spf.setFeature(
                        "http://xml.org/sax/features/external-general-entities",
                        false);
                spf.setFeature(
                        "http://xml.org/sax/features/external-parameter-entities",
                        false);
                XMLReader xmlReader = spf.newSAXParser().getXMLReader();
                SAXSource source = new SAXSource(xmlReader, new InputSource(
                        new BufferedReader(new FileReader(pomFile))));
                TransformerFactory.newInstance().newTransformer()
                        .transform(source, result);
            } catch (Exception e) {
                throw new IOException("Unable to create customized SAX source",
                        e);
            }
        } catch (Exception e) {
            throw new IOException("Unable to create customized SAX source", e);
        }
        if ((pomReader.getGroup() != null) && (pomReader.getArtifact() != null)
                && (pomReader.getVersion() != null)) {
            Version version = pomReader.getVersion();
            if (version.isSnapshot()) {
                String str = pomFile.getName().replace(".pom", "");
                if (!str.endsWith("-SNAPSHOT")) {
                    String[] tab = str.split("-");

                    if ((tab.length == 4) && (tab[2] != null)
                            && (tab[3] != null)) {
                        if (version.getSnapshotTimestamp() != null) {
                            if (tab[2]
                                    .compareTo(version.getSnapshotTimestamp()) > 0) {
                                version.setSnapshotBuildNumber(tab[3]);
                                version.setSnapshotTimestamp(tab[2]);
                                version.setLastUpdated(tab[2].replace(".", ""));
                            }
                        } else {
                            version.setSnapshotBuildNumber(tab[3]);
                            version.setSnapshotTimestamp(tab[2]);
                            version.setLastUpdated(tab[2].replace(".", ""));
                        }
                    }
                }
            }

            String path = pomReader.getGroup().getId()
                    .replace(".", System.getProperty("file.separator"))
                    + "/"
                    + pomReader.getArtifact().getId()
                    + "/"
                    + version.getVersion();
            File f = new File(this.mavenDir, path);
            if ((!f.isDirectory())
                    || (!f.getAbsolutePath().equals(pomFile.getParent()))) {
                System.out.println("Wrong location for "
                        + pomFile.getAbsolutePath());
                System.out.println("    [groupId:"
                        + pomReader.getGroup().getId() + "] [artifactId:"
                        + pomReader.getArtifact().getId() + "] [version:"
                        + version.getVersion() + "]");
            }
        } else {
            System.out.println("Cannot parse pom file "
                    + pomFile.getAbsolutePath());
        }
    }

    /**
     * Sets the root directory of the Maven repository.
     * 
     * @param mavenDir
     *            The root directory of the Maven repository.
     */
    public void setMavenDir(File mavenDir) {
        this.mavenDir = mavenDir;
    }

    /**
     * Sets the root directory where the metadata files will be generated.
     * 
     * @param outDir
     *            The root directory where the metadata files will be generated.
     */
    public void setOutDir(File outDir) {
        this.outDir = (outDir != null ? outDir : this.mavenDir);
    }
}