package filter;

import java.io.IOException;
import java.io.Reader;

import org.apache.tools.ant.filters.BaseParamFilterReader;
import org.apache.tools.ant.filters.ChainableReader;
import org.apache.tools.ant.types.Parameter;

/**
 * Filters bloc of lines according to the value of parameter "edition". For
 * example, let's say the current edition is "jse". The following block will be
 * kept.
 * 
 * <pre>
 * \/\/ [ifdef jse]
 * ...
 * \/\/ [enddef]
 * </pre>
 * 
 * But the following won't.
 * 
 * <pre>
 * \/\/ [ifndef jse]
 * ...
 * \/\/ [enddef]
 * </pre>
 * 
 * Note also the "ifdef" and "ifndef" can have a list of coma separated
 * parameters: [ifndef jse,gwt]
 * 
 */
public class EditionFilterReader extends BaseParamFilterReader implements ChainableReader {

    /**
     * Test method only.
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        StringBuilder builder = new StringBuilder();

        builder.append("  ligne 1 \n");
        builder.append("  // [ifdef jse]\n");
        builder.append("  ligne 2 \n");
        builder.append("  ligne 3 \n");
        builder.append("  // [enddef]\n");
        builder.append("  // [ifndef jse]\n");
        builder.append("  ligne 4 \n");
        builder.append("  ligne 5 \n");
        builder.append("  // [enddef]\n");
        builder.append("  ligne 6 \n");
        builder.append("  // [ifdef jse] uncomment\n");
        builder.append("  //ligne 7 \n");
        builder.append("  //ligne 8 \n");
        builder.append("  // [enddef]\n");
        builder.append("  //ligne 9 \n");
        builder.append("  ligne 10 \n");
        builder.append("  // [ifndef jse] uncomment\n");
        builder.append("  //ligne 11 \n");
        builder.append("  //ligne 12 \n");
        builder.append("  // [enddef]\n");
        builder.append("  //ligne 13 \n");
        builder.append("  ligne 14 \n");

        builder.append("// [ifndef jse]\n");
        builder.append("ligne 15\n");
        builder.append("// [enddef]\n");

        builder.append("// [ifdef jse] uncomment\n");
        builder.append("// ligne 16\n");
        builder.append("// java.util.HashSet<ChallengeRequest>();\n");
        builder.append("// [enddef]\n");

        builder.append("// [ifdef jse] member uncomment\n");
        builder.append("// ligne 17;\n");
        builder.append("// ligne 18;\n");

        builder.append("// [ifndef jse] member uncomment\n");
        builder.append("// ligne 19;\n");
        builder.append("// ligne 20;\n");

        builder.append("// [ifdef jse] member\n");
        builder.append("   ligne 21;\n");
        builder.append("// ligne 22;\n");

        builder.append("// [ifndef jse] member\n");
        builder.append("// ligne 23;\n");
        builder.append("// ligne 24;\n");

        builder.append("// [ifdef jse] method uncomment\n");
        builder.append("// public ligne25() {\n");
        builder.append("// ligne 26\n");
        builder.append("// ligne 27\n");
        builder.append("// }\n");
        builder.append(" \n");

        builder.append("// [ifndef jse] method uncomment\n");
        builder.append("// public ligne28() {\n");
        builder.append("// ligne 29\n");
        builder.append("// ligne 30\n");
        builder.append("// }\n");
        builder.append(" \n");
        builder.append("// [ifdef jse] method\n");
        builder.append("    public ligne31() {\n");
        builder.append("       ligne 32\n");
        builder.append("       ligne 33\n");
        builder.append("    }\n");
        builder.append(" \n");
        builder.append("// [ifndef jse] method\n");
        builder.append("    public ligne34() {\n");
        builder.append("       ligne 35\n");
        builder.append("       ligne 36\n");
        builder.append("    }\n");
        builder.append(" \n");
        builder.append("// [ifdef jse] method\n");
        builder.append("    public ligne37() {\n");
        builder.append("       ligne 38\n");
        builder.append("       ligne 39\n");
        builder.append("    }\n");
        builder.append(" \n");
        builder.append("// [ifdef jse] line\n");
        builder.append(" ligne 40\n");
        builder.append("// [ifndef jse] line\n");
        builder.append("// ligne 41\n");
        builder.append("// [ifdef jse] line uncomment\n");
        builder.append("// ligne 42\n");
        builder.append("// ligne 43\n");
        builder.append("// [ifndef jse] line uncomment\n");
        builder.append("// ligne 44\n");
        builder.append("// ligne 45\n");

        builder.append("// [ifndef jse] javadocs\n");
        builder.append("/** \n");
        builder.append(" * NJSE - Returns the next line to read, or null otherwise.\n");
        builder.append(" * \n");
        builder.append(" * @return The next line to read, or null otherwise.\n");
        builder.append(" * @throws IOException\n");
        builder.append(" */\n");
        builder.append("// [ifdef jse] javadocs\n");
        builder.append("/** \n");
        builder.append(" * JSE - Returns the next line to read, or null otherwise.\n");
        builder.append(" * \n");
        builder.append(" * @return The next line to read, or null otherwise.\n");
        builder.append(" * @throws IOException\n");
        builder.append(" */\n");
        builder.append("ligne 46\n");
        builder.append("ligne 47\n");

        builder.append(" \n");

        java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.FileReader(
                        new java.io.File(
                                "/mnt/sda5/data/alaska/forge/build/swc/restlet/trunk/modules/org.restlet/src/org/restlet/resource/Result.java")));
        EditionFilterReader efr = null;

        try {
            efr = new EditionFilterReader(br);
            efr.setEdition("android");

            int index = efr.read();
            while (index != -1) {
                System.out.print((char) index);
                index = efr.read();
            }
        } finally {
            if (efr != null) {
                efr.close();
            }
        }
    }

    /** The current edition. */
    private String edition = null;

    /** Indicates if we are inside a block. */
    boolean insideBlock = false;

    /** Indicates if we are handling an instruction. */
    boolean instruction = false;

    /** Indicates if we are handling a javadocs comment. */
    boolean javadocs = false;

    /** Indicates if we are handling a line. */
    boolean line = false;

    /** Indicates if we are handling a class member. */
    boolean member = false;

    /** Indicates if we are handling a method. */
    boolean method = false;

    /** The line that has been read ahead. */
    private String readLine = null;

    /** Indicates if the following block is to be uncommented. */
    boolean toUncomment = false;

    public EditionFilterReader(final Reader in) {
        super(in);
    }

    /**
     * Used when chaining readers.
     */
    public Reader chain(Reader reader) {
        EditionFilterReader efr = new EditionFilterReader(reader);
        efr.setEdition(this.edition);
        return efr;
    }

    /**
     * Returns the next line to read, or null otherwise.
     * 
     * @return The next line to read, or null otherwise.
     * @throws IOException
     */
    private String getNextLine() throws IOException {
        // Indicates if the incoming line is to be ignored.
        boolean strip = false;

        String result = readLine();
        if (result != null) {
            String str = result.trim().replace("\t", "");
            if (!insideBlock) {
                if (str.startsWith("// [ifdef")) {
                    int index = str.indexOf("]");
                    if (index != -1) {
                        String[] tab = str.substring("// [ifdef".length() + 1,
                                index).split(",");
                        boolean found = false;
                        for (int i = 0; i < tab.length && !found; i++) {
                            found = (edition.equalsIgnoreCase(tab[i].trim()));
                        }
                        strip = !found;
                        instruction = str.contains("instruction");
                        line = str.contains("line");
                        member = str.contains("member");
                        method = str.contains("method");
                        javadocs = str.contains("javadocs");
                        toUncomment = str.contains("uncomment");
                        insideBlock = true;
                        result = readLine();
                    }
                } else if (str.startsWith("// [ifndef")) {
                    int index = str.indexOf("]");
                    if (index != -1) {
                        String[] tab = str.substring("// [ifndef".length() + 1,
                                index).split(",");
                        boolean found = false;
                        for (int i = 0; i < tab.length && !found; i++) {
                            found = (edition.equalsIgnoreCase(tab[i].trim()));
                        }
                        strip = found;
                        instruction = str.contains("instruction");
                        line = str.contains("line");
                        member = str.contains("member");
                        method = str.contains("method");
                        javadocs = str.contains("javadocs");
                        toUncomment = str.contains("uncomment");
                        insideBlock = true;
                        result = readLine();
                    }
                }
            }
            if (insideBlock) {
                if (strip) {
                    boolean end = false;
                    if (line) {
                        end = true;
                    } else if (instruction || member) {
                        // Read until the end of the member block
                        while (!end && result != null) {
                            if (result.endsWith(";\r\n")
                                    || result.endsWith(";\n")
                                    || result.endsWith(";")) {
                                end = true;
                            } else {
                                result = readLine();
                            }
                        }
                    } else if (method) {
                        // Read until the end of the method block
                        if (toUncomment) {
                            // we assume that the next line without comment
                            // marks the end of the block...
                            while (!end && result != null) {
                                str = result.trim().replace("\t", "");
                                if (!str.startsWith("//")) {
                                    end = true;
                                } else {
                                    result = readLine();
                                }
                            }
                        } else {
                            while (!end && result != null) {
                                if (result.startsWith("    }")
                                        || result.startsWith("\t}")
                                        || result.startsWith("   }")) {
                                    end = true;
                                } else {
                                    result = readLine();
                                }
                            }
                        }
                    } else if (javadocs) {
                        while (!end && result != null) {
                            if (result.trim().startsWith("*/")) {
                                end = true;
                            } else {
                                result = readLine();
                            }
                        }
                    } else {
                        // Read until the block // [enddef]
                        while (!end && result != null) {
                            str = result.trim().replace("\t", "");
                            if (str.startsWith("// [enddef]")) {
                                end = true;
                            } else {
                                result = readLine();
                            }
                        }
                    }
                    if (end) {
                        strip = false;
                        initBooleans();
                        result = getNextLine();
                    }
                } else if (toUncomment) {
                    str = result.trim().replace("\t", "");
                    if (line) {
                        if (str.startsWith("//")) {
                            result = result.replaceFirst("//", "");
                        }
                        // end of the block
                        initBooleans();
                    } else if (instruction || member) {
                        if (str.startsWith("//")) {
                            result = result.replaceFirst("//", "");
                        }
                        if (result.endsWith(";\r\n") || str.endsWith(";\n")
                                || str.endsWith(";")) {
                            // end of the block
                            initBooleans();
                        }
                    } else if (method) {
                        if (str.startsWith("//")) {
                            result = result.replaceFirst("//", "");
                        } else {
                            // we assume that the next line without comment
                            // marks the end of the block...
                            initBooleans();
                        }
                    } else {
                        if (str.startsWith("// [enddef]")) {
                            // ignore this line and read the next one
                            initBooleans();
                            result = getNextLine();
                        } else {
                            if (str.startsWith("//")) {
                                result = result.replaceFirst("//", "");
                            }
                        }
                    }
                } else {
                    if (!instruction && !line && !member && !method
                            && !javadocs) {
                        if (str.startsWith("// [enddef]")) {
                            // ignore this line and read the next one
                            initBooleans();
                            result = getNextLine();
                        }
                    } else {
                        // there is no end marker, let's consider we are not
                        // inside a block
                        insideBlock = false;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Used to initialize the value of the booleans members.
     */
    private void initBooleans() {
        insideBlock = false;
        instruction = false;
        line = false;
        member = false;
        method = false;
        toUncomment = false;
    }

    /**
     * Scans the parameters list for the "lines" parameter and uses it to set
     * the number of lines to be returned in the filtered stream. also scan for
     * "skip" parameter.
     */
    private void initialize() {
        Parameter[] params = getParameters();
        if (params != null) {
            for (Parameter parameter : params) {
                if ("edition".equals(parameter.getName())) {
                    setEdition(parameter.getValue());
                    break;
                }
            }
        }
    }

    @Override
    public int read() throws IOException {
        if (!getInitialized()) {
            initialize();
            setInitialized(true);
        }
        int ch = -1;
        if (edition == null) {
            return super.read();
        }

        if (readLine != null) {
            // Consume the read line.
            ch = readLine.charAt(0);
            if (readLine.length() == 1) {
                readLine = null;
            } else {
                readLine = readLine.substring(1);
            }
        } else {
            if ((readLine = getNextLine()) != null) {
                // read the first character of the line.
                return read();
            }
        }

        return ch;
    }

    /**
     * Sets the current edition.
     * 
     * @param edition
     *            The current edition.
     */
    public void setEdition(String edition) {
        this.edition = edition;
    }

}
