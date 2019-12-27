package dataLoader;

import dataLoader.data.Edition;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LoaderUtils {

    private LoaderUtils() {}

    /**
     * Computes the map of editions.
     *
     * @param editions
     *            the current list of editions.
     * @param includes
     *            The "includes" attributes read from the descriptor.
     * @param excludes
     *            The "excludes" attributes read from the descriptor.
     */

    public static Collection<Edition> filterEditions(Collection<Edition> editions,
                                                     String includes, String excludes) {
        if (includes == null && excludes == null) {
            return editions;
        }
        if ("*".equals(excludes)) { // exclude all editions
            return Collections.emptyList();
        }

        // filter the included editions
        List<Edition> result = new ArrayList<>();
        if (includes == null || "*".equals(includes)) {
            for (Edition edition : editions) {
                result.add(edition);
            }
        } else {
            // The "includes" part is not null
            String[] strings = includes.split(",");
            for (int i = 0; i < strings.length; i++) {
                for (Edition edition : editions) {
                    if (edition.getId().equals(strings[i].trim())) {
                        result.add(edition);
                    }
                }
            }
        }
        // eds empty, is null
        if (result.isEmpty()) {
            return result;
        }
        // Nothing to remove
        if (excludes == null) {
            return result;
        }
        // Something to remove
        List<Edition> list = new ArrayList<Edition>(result);
        result.clear();
        String[] strings = excludes.split(",");
        if (strings.length > 0) {
            for (Edition edition : list) {
                boolean found = false;
                for (int i = 0; !found && i < strings.length; i++) {
                    if (edition.getId().equals(strings[i].trim())) {
                        found = true;
                    }
                }
                if (!found) {
                    result.add(edition);
                }
            }
        }
        return result;
    }

    /**
     * Returns the builder value without trailing or leading white spaces.
     *
     * @param builder
     *            The string builder.
     * @return The builder value without trailing or leading white spaces.
     */
    protected static String trim(StringBuilder builder) {
        return Optional.ofNullable(builder)
                .map(StringBuilder::toString)
                .map(String::trim)
                .map(trimmed -> trimmed.replace("\n", "").replace("\t", ""))
                .orElse(null);
    }

    protected static String getTagName(String uri, String localName, String name) {
        if (localName != null && !localName.isEmpty()) {
            return localName;
        }
        return name;
    }

    /**
     * Check the given parameter is a correct file or directory.
     *
     * @param parameter
     *         The parameter value
     * @param rootDir
     *         The optional root directory, if the parameter is a relative
     *         path.
     * @param isDir
     *         True if this parameter must be a directory, a file otherwise.
     * @return The file descriptor to the file or the directory.
     * @throws Exception
     */
    static File loadFile(Object parameter, File rootDir, boolean isDir)
            throws Exception {
        if (!(parameter instanceof String)) {
            throw new Exception(ForgeLoader.class + " loader supports only String parameters!");
        }

        File result = Optional.ofNullable(rootDir)
                .map(rd -> new File(rd, (String) parameter))
                .orElse(new File((String) parameter));

        if (isDir && !result.isDirectory()) {
            throw new Exception("Must be a valid directory: " + result.getPath());
        } else if (!isDir && !result.isFile()) {
            throw new Exception("Must be a valid file: " + result.getPath());
        }
        return result;
    }
}
