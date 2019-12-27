package checker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.objectweb.asm.ClassReader;

/**
 * Vérifie le code source généré pour GAE.
 * 
 * @author thierry boileau
 * 
 */
public class GaeCodeChecker extends Task {

    /** Les listes de classes autorisées */
    private List<File> checkedClasses;

    /** Indique si le traitement a rencontré au moins une erreur. */
    private boolean error;

    /** Indique s'il faut s'arrêter dès qu'une erreur est détectée. */
    private boolean failOnError;

    /** Les listes de classes autorisées */
    private List<File> jdkClassesWhiteLists;

    /** Les listes des méthodes autorisées ou interdites. */
    private List<File> jdkMethodsLists;

    public GaeCodeChecker() {
        super();
        jdkClassesWhiteLists = new ArrayList<File>();
        jdkMethodsLists = new ArrayList<File>();
        checkedClasses = new ArrayList<File>();
        failOnError = false;
        error = false;
    }

    public void addConfiguredJdkClassesWhiteLists(FileSet fileSet) {
        addFileset(fileSet, jdkClassesWhiteLists);
    }

    public void addConfiguredJdkMethodsLists(FileSet fileSet) {
        addFileset(fileSet, jdkMethodsLists);
    }

    public void addConfiguredCheckedClasses(FileSet fileSet) {
        addFileset(fileSet, checkedClasses);
    }

    private void addFileset(FileSet fileSet, List<File> files) {
        DirectoryScanner ds = fileSet.getDirectoryScanner(getProject());
        for (String string : ds.getIncludedFiles()) {
            files.add(new File(ds.getBasedir(), string));
        }
    }

    /**
     * Vérifie le contenu d'une classe.
     * 
     * @param file
     *            Le fichier binaire à vérifier.
     * @param jdkClasses
     *            La liste des classes du JDK autorisées.
     * @throws BuildException
     */
    private void check(File file, List<String> jdkClasses,
            Map<String, List<String>> authorizedMethods,
            Map<String, List<String>> forbiddenMethods) throws BuildException {
        WhiteListeClassChecker checker = new WhiteListeClassChecker(jdkClasses,
                authorizedMethods, forbiddenMethods, file.getName());
        FileInputStream fis = null;
        ClassReader reader = null;
        try {
            fis = new FileInputStream(file);
            reader = new ClassReader(fis);
        } catch (FileNotFoundException e) {
            throw new BuildException("Cannot find " + file.getAbsolutePath()
                    + " due to " + e.getMessage());
        } catch (IOException e) {
            throw new BuildException("Cannot read " + file.getAbsolutePath()
                    + " due to " + e.getMessage());
        }
        try {
            reader.accept(checker, 0);
        } catch (Throwable e) {
            String path = file.getAbsolutePath();
            if (path.startsWith(getProject().getBaseDir().getAbsolutePath())) {
                path = path.substring(getProject().getBaseDir()
                        .getAbsolutePath().length());
            }

            if (failOnError) {
                throw new BuildException("This class is not GAE compliant: "
                        + path + "\t" + e.getMessage());
            } else {
                error = true;
                getProject().log(
                        "This class is not GAE compliant: " + path + "\t"
                                + e.getMessage(), Project.MSG_ERR);
            }
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public void execute() throws BuildException {
        /** La liste des classes du JDK autorisées. */
        List<String> jdkClasses = new ArrayList<String>();

        /** Liste des méthodes formellement autorisées par classe. */
        Map<String, List<String>> authorizedMethods = new HashMap<String, List<String>>();

        /** Liste des méthodes formellement interdites par classe. */
        Map<String, List<String>> forbiddenMethods = new HashMap<String, List<String>>();

        for (File whiteList : jdkClassesWhiteLists) {
            // Lecture de la liste des classes supportées
            BufferedReader rd = null;
            try {
                rd = new BufferedReader(new FileReader(whiteList));
                String line = null;
                while ((line = rd.readLine()) != null) {
                    jdkClasses.add(line);
                }
            } catch (FileNotFoundException e) {
                throw new BuildException(
                        "Cannot find (from classes white list) "
                                + whiteList.getAbsolutePath());
            } catch (IOException e) {
                throw new BuildException(
                        "Cannot read (from classes white list) "
                                + whiteList.getAbsolutePath());
            } finally {
                if (rd != null) {
                    try {
                        rd.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        for (File mList : jdkMethodsLists) {
            // Read the list of methods (authorized or forbidden)
            BufferedReader rd = null;
            try {
                rd = new BufferedReader(new FileReader(mList));
                String line = null;
                while ((line = rd.readLine()) != null) {
                    int minus = line.indexOf("-");
                    int plus = line.indexOf("+");
                    if (minus > 0) {
                        put(forbiddenMethods, line, minus);
                    } else if (plus > 0) {
                        put(authorizedMethods, line, plus);
                    }
                }
            } catch (FileNotFoundException e) {
                throw new BuildException(
                        "Cannot find (from JDK's methods list) "
                                + mList.getAbsolutePath());
            } catch (IOException e) {
                throw new BuildException(
                        "Cannot read (from JDK's methods list) "
                                + mList.getAbsolutePath());
            } finally {
                if (rd != null) {
                    try {
                        rd.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

        for (File classe : checkedClasses) {
            check(classe, jdkClasses, authorizedMethods, forbiddenMethods);
        }

        if (error) {
            throw new BuildException("At least one class is not GAE compliant.");
        }
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    private void put(Map<String, List<String>> map, String line, int index) {
        String classe = line.substring(0, index);
        if (!map.containsKey(classe)) {
            map.put(classe, new ArrayList<String>());
        }
        map.get(classe).add(line.substring(index + 1));
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

}
