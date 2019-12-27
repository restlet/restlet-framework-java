package selector;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.selectors.BaseExtendSelector;

public class EditionFileSelector extends BaseExtendSelector {

    public static void main(String[] args) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("// [excludes android]\n");
        sb.append("/**\n");
        sb.append(" * Application implementation.\n");
        sb.append(" * \n");
        sb.append(" * @author Jerome Louvel\n");
        sb.append(" */\n");
        sb.append("public class ApplicationHelper extends ChainHelper {\n");

        EditionFileSelector selector = new EditionFileSelector();
        selector.edition = "android";
        // Should be false
        System.out.println(selector.check(new BufferedReader(new StringReader(
                sb.toString()))));

        selector.edition = "gwt";
        // Should be true
        System.out.println(selector.check(new BufferedReader(new StringReader(
                sb.toString()))));

        sb = new StringBuilder();

        sb.append("// [includes android]\n");
        sb.append("/**\n");
        sb.append(" * Application implementation.\n");
        sb.append(" * \n");
        sb.append(" * @author Jerome Louvel\n");
        sb.append(" */\n");
        sb.append("public class ApplicationHelper extends ChainHelper {\n");

        selector = new EditionFileSelector();
        selector.edition = "android";
        // Should be true
        System.out.println(selector.check(new BufferedReader(new StringReader(
                sb.toString()))));

        selector.edition = "gwt";
        // Should be false
        System.out.println(selector.check(new BufferedReader(new StringReader(
                sb.toString()))));
    }

    private String edition;

    /**
     * Returns true if the Reader is to be selected.
     * 
     * @param bf
     *            The reader.
     * @return
     * @throws IOException
     */
    private boolean check(BufferedReader bf) throws IOException {
        boolean result = true;
        boolean end = false;
        String line;
        while (!end && (line = bf.readLine()) != null) {
            if (line.startsWith("public")) {
                end = true;
            } else {
                String str = line.trim().replace("\t", "");
                if (str.startsWith("//")) {
                    str = str.substring(2).trim();
                }
                if (str.startsWith("[")) {
                    end = true;
                    int index = str.indexOf("]");
                    if (index != -1) {
                        str = str.substring(1, index)
                                .replace("includes", "\nincludes")
                                .replace("excludes", "\nexcludes");
                        Properties p = new Properties();
                        p.load(new ByteArrayInputStream(str.getBytes()));
                        String strIncludes = p.getProperty("includes", null);
                        String strExcludes = p.getProperty("excludes", null);
                        result = check(strIncludes, strExcludes, edition);
                    }
                }
            }
        }
        return result;
    }

    private boolean check(String strIncludes, String strExcludes, String edition) {
        if (strIncludes == null && strExcludes == null) {
            return true;
        }
        if ("*".equals(strExcludes)) {
            return false;
        }
        if ("*".equals(strIncludes)) {
            return true;
        }

        if (strExcludes == null && strIncludes != null) {
            String[] strings = strIncludes.split(",");
            for (int i = 0; i < strings.length; i++) {
                if (edition.equals(strings[i].trim())) {
                    return true;
                }
            }
            return false;
        }

        String[] strings = strExcludes.split(",");
        for (int i = 0; i < strings.length; i++) {
            if (edition.equals(strings[i].trim())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Method that each selector will implement to create their selection
     * behaviour. If there is a problem with the setup of a selector, it can
     * throw a BuildException to indicate the problem.
     * 
     * @param basedir
     *            A File object for the base directory
     * @param filename
     *            The name of the file to check
     * @param file
     *            A File object for this filename
     * @return whether the file should be selected or not
     * @exception BuildException
     *                if the selector was not configured correctly
     */
    @Override
    public boolean isSelected(File basedir, String filename, File file)
            throws BuildException {
        if (!file.getName().contains(".java")) {
            return true;
        }

        boolean result = true;
        BufferedReader bf = null;
        try {
            // Read the file and check if it contains an "// [" mark before the
            // declaration of the class
            bf = new BufferedReader(new FileReader(file));
            result = check(bf);
        } catch (FileNotFoundException e) {
            // should not arrive
            result = false;
        } catch (IOException e) {
            result = false;
        } finally {
            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException e) {
                    // nothing
                }
            }
        }

        return result;
    }

    @Override
    public void setParameters(Parameter[] parameters) {
        for (Parameter parameter : parameters) {
            if (parameter.getName().equals("edition")) {
                this.edition = parameter.getValue();
            }
        }
    }

    @Override
    public void verifySettings() {
        if (this.edition == null) {
            throw new IllegalArgumentException(
                    "Please provide a value to the mandatory argument 'edition'. ");
        }
    }

}
