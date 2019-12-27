package checker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import dataLoader.ForgeLoader;
import dataLoader.data.Library;
import dataLoader.data.LibraryPackage;
import fmpp.Engine;

/**
 * Check the whole integrity of the build.
 *
 * @author thierry boileau
 *
 */
public class ProjectChecker extends Task {

    public static void main(String[] args) {
        ProjectChecker checker = new ProjectChecker();
        checker.setRootDir("/home/tboileau/forge/restlet/restlet-framework-java");
        checker.setMaven(true);
        checker.execute();
    }

    /** True for handling maven checks. */
    private boolean maven;

    /** The root directory of the project. */
    private String rootDir;

    /** True to print traces. */
    private boolean verbose;

    /**
     * Checks that all modules/libraries are correctly configured.
     *
     * @param project
     *            The current project.
     */
    private void checkMavenConfiguration(dataLoader.data.Project project) {
        List<LibraryPackage> packageErrors = new ArrayList<>();
        for (Entry<String, Library> entry : project.getLibraries().entrySet()) {
            Library library = entry.getValue();
            for (LibraryPackage p : library.getPackages()) {
                if (!library.getDistributions().contains("maven")) {
                    boolean error = true;

                    if (library.getMavenReleaseRepository() == null
                            && library.getMavenSnapshotRepository() == null) {
                        error = !CheckUtils.checkMavenRepository(
                                "https://repo1.maven.org/maven2/",
                                p);
                    } else {
                        if (library.getMavenReleaseRepository() != null) {
                            error = !CheckUtils.checkMavenRepository(library
                                    .getMavenReleaseRepository().getUrl(), p);
                        }
                        if (error
                                && library.getMavenSnapshotRepository() != null) {
                            error = !CheckUtils.checkMavenRepository(library
                                    .getMavenSnapshotRepository().getUrl(), p);
                        }
                    }

                    if (error) {
                        packageErrors.add(p);
                    }
                } else if (p.getMavenGroupId() != null) {
                    packageErrors.add(p);
                }
            }
        }
        if (!packageErrors.isEmpty()) {
            StringBuilder sb = new StringBuilder(
                    "Check maven parameters for the following package");
            if (packageErrors.size() > 1) {
                sb.append("s");
            }
            sb.append(":\n");
            for (LibraryPackage p : packageErrors) {
                sb.append("\t");
                sb.append(p.getId());
                sb.append(" - [").append(p.getMavenGroupId());
                sb.append(" - ").append(p.getMavenArtifactId());
                sb.append(" - ").append(p.getMavenVersion());
                sb.append("]\n");
            }
            throw new BuildException(sb.toString());
        }
    }

    @Override
    public void execute() throws BuildException {
        dataLoader.data.Project project = null;
        try {
            ForgeLoader fl = new ForgeLoader();
            Object obj = fl.load(
                    (Engine) null,
                    Arrays.asList(rootDir + "/build/project.xml", rootDir
                            + "/modules", rootDir + "/libraries"));
            Map<String, Object> result = (Map<String, Object>) obj;
            project = (dataLoader.data.Project) result.get("project");
        } catch (Exception e) {
            throw new BuildException("Issue when loading the full project.", e);
        }
        // check the modules.
        if (project == null) {
            throw new BuildException("Empty project, check the parameters: "
                    + rootDir);
        }

        if (maven) {
            checkMavenConfiguration(project);
        }
    }

    public String getRootDir() {
        return rootDir;
    }

    @Override
    protected void handleErrorFlush(String output) {
        if (verbose) {
            super.handleErrorFlush(output);
        }
    }

    @Override
    protected void handleErrorOutput(String output) {
        if (verbose) {
            super.handleErrorOutput(output);
        }
    }

    @Override
    protected void handleFlush(String output) {
        if (verbose) {
            super.handleFlush(output);
        }
    }

    @Override
    protected void handleOutput(String output) {
        if (verbose) {
            super.handleOutput(output);
        }
    }

    public boolean isMaven() {
        return maven;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setMaven(boolean maven) {
        this.maven = maven;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

}
