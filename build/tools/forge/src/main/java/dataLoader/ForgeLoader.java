package dataLoader;

import dataLoader.data.Distribution;
import dataLoader.data.Edition;
import dataLoader.data.Library;
import dataLoader.data.LibraryDependency;
import dataLoader.data.Module;
import dataLoader.data.ModuleDependency;
import dataLoader.data.Project;
import fmpp.Engine;
import fmpp.tdd.DataLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Load a project, its modules and libraries.
 */
public class ForgeLoader implements DataLoader {

    // Used for test purpose only.
    public static void main(String[] args) throws Exception {
        if (args.length == 1) {
            ForgeLoader fl = new ForgeLoader();
            Object obj = fl.load(
                    null,
                    Arrays.asList(args[0] + "/build/project.xml",
                            args[0] + "/modules",
                            args[0] + "/libraries"));
            System.out.println(obj);
        } else {
            System.err
                    .println("Specify the root directory of the project as unique argument.");
        }

    }

    /**
     * Fmpp data loader.
     */
    public Object load(Engine fmppEngine, List arguments) throws Exception {
        File rootDir = null;
        if (fmppEngine != null) {
            rootDir = fmppEngine.getDataRoot();
        }
        if (arguments.size() < 3) {
            final String msg = String.format(
                    "Specify the correct parameters for the \"%s\" loader: \n" +
                            " - project descriptor path, \n" +
                            " - modules root directory, \n" +
                            " - libraries root directory. \n",
                    this.getClass()
            );
            throw new Exception(msg);
        }

        File projectDescriptor = LoaderUtils.loadFile(arguments.get(0), rootDir, false);
        File modulesDir = LoaderUtils.loadFile(arguments.get(1), rootDir, true);

        // load the project descriptor.
        ProjectLoader projectLoader = new ProjectLoader(projectDescriptor);
        projectLoader.load();

        Project project = projectLoader.getProject();

        // load the libraries descriptors.
        for (int i = 2; i < arguments.size(); i++) {
            File librariesDir = LoaderUtils.loadFile(arguments.get(i), rootDir, true);
            new LibraryLoader(project, librariesDir).load();
        }

        completeLibrariesDependencies(project);

        // load the modules descriptors.
        new ModuleLoader(project, modulesDir).load();

        // At this point each edition has correct lists of libraries and
        // modules. However, each module has a set of module dependencies
        // which is not correct.
        completeEditionModulesDependencies(project);

        // Updates the distributions.
        completeDistributions(project);

        // Sort by module dependencies
        sortModuleDependencies(project);

        Map<String, Object> result = new HashMap<>();
        result.put("project", project);
        result.put("editions", project.getEditions());

        return result;

    }

    private void sortModuleDependencies(final Project project) {
        Map<String, Edition> editions = project.getEditions();
        for (Edition edition : editions.values()) {
            List<String> sorted = new ArrayList<>();
            int dependencyLevel = 1;
            for (int i = 0; (sorted.size() != edition.getModules().size())
                    && (i < edition.getModules().size()); i++) {
                List<String> notYetSorted = new ArrayList<>();
                for (Module module2 : edition.getModules()) {
                    boolean allDependenciesSorted = true;
                    for (int j = 0; allDependenciesSorted
                            && j < module2.getNeededModules().size(); j++) {
                        allDependenciesSorted = sorted.contains(module2
                                .getNeededModules().get(j).getModule()
                                .getId());
                    }
                    if (allDependenciesSorted
                            && !sorted.contains(module2.getId())) {
                        notYetSorted.add(module2.getId());
                        module2.setDependencyLevel(dependencyLevel);
                    }
                }
                sorted.addAll(notYetSorted);
                dependencyLevel++;
            }
        }
    }

    private void completeDistributions(final Project project) {
        Map<String, Edition> editions = project.getEditions();
        for (Edition edition : editions.values()) {
            for (Distribution distribution : edition.getDistributions()) {
                // Add the modules.
                for (Module module : edition.getModules()) {
                    if (module.getDistributions().contains(distribution.getId())) {
                        distribution.getModules().add(module);
                    }
                }
                // Add the libraries.
                for (Library library : edition.getLibraries()) {
                    if (library.getDistributions().contains(distribution.getId())) {
                        distribution.getLibraries().add(library);
                    }
                }
                if ("p2".equals(distribution.getId())) {
                    List<Module> list = new ArrayList<>(
                            distribution.getModules());
                    // Ensure that dependencies are also correctly
                    // distributed
                    for (Module module : list) {
                        for (ModuleDependency dep : module.getNeededModules()) {
                            boolean found = false;
                            for (Module m : distribution.getModules()) {
                                if (m.equals(dep.getModule())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                distribution.getModules().add(dep.getModule());
                            }
                        }
                        for (LibraryDependency dep : module.getNeededLibraries()) {
                            boolean found = false;
                            for (Library l : distribution.getLibraries()) {
                                if (l.equals(dep.getLibrary())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                distribution.getLibraries().add(dep.getLibrary());
                            }
                        }
                    }
                    List<Library> listLib = new ArrayList<>(distribution.getLibraries());
                    for (Library library : listLib) {
                        for (LibraryDependency dep : library.getNeededLibraries()) {
                            boolean found = false;
                            for (Library l : distribution.getLibraries()) {
                                if (l.equals(dep.getLibrary())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                distribution.getLibraries().add(dep.getLibrary());
                            }
                        }
                    }
                }
            }
        }
    }

    private void completeEditionModulesDependencies(final Project project) {
        Map<String, Edition> editions = project.getEditions();

        for (Edition edition : editions.values()) {
            for (Module module : edition.getModules()) {
                // Refresh each module dependency
                for (ModuleDependency dep : module.getNeededModules()) {
                    boolean found = false;
                    for (Iterator<Module> iterator = edition.getModules()
                            .iterator(); iterator.hasNext() && !found; ) {
                        Module m = iterator.next();
                        if (m.equals(dep.getModule())) {
                            dep.setModule(m);
                            found = true;
                        }
                    }
                    if (!found) {
                        System.err.println("The module " + module.getId()
                                + " declares an unknown module "
                                + dep.getModule() + " in edition "
                                + edition.getId());
                    }
                }
            }
        }
    }

    private void completeLibrariesDependencies(final Project project) {
        // Update the libraries's dependencies
        for (Library library : project.getLibraries().values()) {
            for (LibraryDependency dep : library.getNeededLibraries()) {
                Library lib = project.getLibraries().get(dep.getLibrary().getId());
                if (lib != null) {
                    dep.setLibrary(lib);
                } else {
                    throw new IllegalStateException(
                            "Library "
                                    + library
                                    + " depends on the following library which is not registered: "
                                    + lib);
                }
            }
        }
    }

}
