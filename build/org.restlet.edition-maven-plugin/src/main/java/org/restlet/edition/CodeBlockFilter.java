package org.restlet.edition;

import java.io.BufferedReader;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reader that wraps a reader and updates its content by reading the blocks annotations and interprets them. <br>
 * Samples of syntax are shown below:
 * <p>
 * The block is kept, only if the current edition is either jse or gwt.
 * <p>
 * <pre>
 * \/\/ [ifdef jse,gwt]
 * ...
 * \/\/ [enddef]
 * </pre>
 * <p>
 * The block is removed, only if the current edition is either jse or gwt.
 * <p>
 * <pre>
 * \/\/ [ifndef jse,gwt]
 * ...
 * \/\/ [enddef]
 * </pre>
 * <p>
 * As you may have noticed, "\/\/
 * [enddef]" is the marker of the end of the block. This marker can be removed by adding a "range", cf this syntax.
 * <p>
 * <pre>
 * \/\/ [ifdef jse,gwt] line
 * \/\/ [ifdef jse,gwt] member
 * \/\/ [ifdef jse,gwt] instruction
 * \/\/ [ifdef jse,gwt] method
 * \/\/ [ifdef jse,gwt] javadocs
 * </pre>
 * <ul>
 * <li>line : means that the block is only one line
 * <li>member: the block ends with the next uncommented ";"
 * <li>instruction: the block ends with the next uncommented ";"
 * <li>method: the block ends with the next "}"
 * <li>javadocs: the block ends with the next "*\/"
 * </ul>
 * <br>
 * Last, but not least, some code can be uncommentted and revealed, if needed, by adding the "uncomment" parameter.
 * This is useful when the commented block of code contains references to classes that prevent to compile the unique
 * base of code with the current JDK.
 * <p>
 * <pre>
 * \/\/ [ifdef gwt] line uncomment
 * \/\/ private com.google.gwt.TestProxy test;
 * </pre>
 */
public class CodeBlockFilter extends FilterReader {

    private static final Pattern startBlockPattern = Pattern
            .compile("\\s*//\\s*\\[(?<def>ifdef|ifndef) (?<editions>.*)\\s*\\](?<parameters>.*)");

    private static final Pattern excludeBlockPattern = Pattern
            .compile("\\s*//\\s*\\[exclude (?<editions>.*)\\s*\\]");

    private static final Pattern rangeWithUncommentPattern = Pattern.compile("\\s*(?<range>\\w*)\\s*uncomment\\s*");

    private static final Pattern rangePattern = Pattern.compile("\\s*(?<range>\\w+)\\s*");

    private static final Pattern commentedLinePattern = Pattern.compile("\\s*//.*");

    private static final Pattern endBlockPattern = Pattern.compile("\\s*//\\s*\\[enddef\\]");

    private String edition = null;

    private boolean insideCodeBlock = false;

    private BlockRange currentBlockRange;

    private String readLine = null;

    private int readLineIndex = 0;

    // Indicates if the following block is to be uncommented.
    private boolean toUncomment = false;

    private final BufferedReader wrappedReader;

    public CodeBlockFilter(Reader reader, String edition) {
        super(reader);
        if (reader instanceof BufferedReader) {
            this.wrappedReader = (BufferedReader) reader;
        } else {
            this.wrappedReader = new BufferedReader(reader);
        }
        this.edition = edition;
    }

    @Override
    public int read() throws IOException {
        if (edition == null) {
            return super.read();
        }

        if (readLine != null && readLineIndex < readLine.length()) {
            // let's consume the current line
            return readLine.charAt(readLineIndex++);
        }

        // read another one.
        readLine = getNextLine();
        readLineIndex = 0;
        if (readLine != null) {
            readLine += "\n";
            // read the first character of the line.
            return read();
        }

        return -1;
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        int nbRead = 0;

        for (int i = off; i < off + len; i++) {
            int next = this.read();
            if (next == -1) {
                break;
            }

            cbuf[i] = (char) next;
            nbRead++;
        }

        return (nbRead == 0)? -1 : nbRead;
    }

    /**
     * Returns the next line to read, or null otherwise.
     *
     * @return The next line to read, or null otherwise.
     * @throws IOException
     */
    private String getNextLine() throws IOException {
        String currentLine = readNextLine();

        if (currentLine == null) {
            return null;
        }

        if (excludeBlockPattern.matcher(currentLine).matches()) {
            currentLine = readNextLine();
        }

        // Indicates if the incoming lines are to be ignored.
        boolean ignoreLines = false;

        if (!insideCodeBlock) {
            // check whether a new code block starts
            Matcher startBlockMatcher = startBlockPattern.matcher(currentLine.toLowerCase());

            insideCodeBlock = startBlockMatcher.matches();
            if (insideCodeBlock) {
                boolean excludeEditions = "ifndef".equals(startBlockMatcher.group("def"));
                boolean isEditionListed = Arrays.asList(startBlockMatcher.group("editions").split(",")).
                        contains(edition.toLowerCase());

                ignoreLines = (excludeEditions && isEditionListed) || (!excludeEditions && !isEditionListed);

                String parameters = startBlockMatcher.group("parameters");

                Matcher rangeMatcher = rangeWithUncommentPattern.matcher(parameters);
                if (rangeMatcher.matches()) {
                    toUncomment = true;
                } else {
                    rangeMatcher = rangePattern.matcher(parameters);
                }
                if (rangeMatcher.matches() && !rangeMatcher.group("range").isEmpty()) {
                    currentBlockRange = BlockRange.valueOf(rangeMatcher.group("range"));
                } else {
                    currentBlockRange = BlockRange.blockWithEndMarker;
                }

                // read next line
                currentLine = readNextLine();
            }
        }

        if (!insideCodeBlock) {
            return currentLine;
        }

        if (ignoreLines) {
            return ignoreLines(currentLine);
        } else if (toUncomment) {
            return uncommentLineInBlock(currentLine);
        } else if (BlockRange.blockWithEndMarker == currentBlockRange) {
            if (currentBlockRange.endOfMatcher.match(currentLine)) {
                // ignore this line and read the next one
                markEndOfBlock();
                currentLine = getNextLine();
            }
        } else {
            // there is no end marker, let's consider we are outside a block
            insideCodeBlock = false;
        }

        return currentLine;
    }

    private String ignoreLines(String currentLine) throws IOException {
        // consume next lines up until the end of the block range.
        boolean end = false;

        while (!end && currentLine != null) {
            if (toUncomment && isCommentedLine(currentLine)) {
                // In this case, the last line of the block is commented
                // let's uncomment it so that the end matcher works as expected
                currentLine = currentLine.replaceFirst("//", "");
            }
            end = currentBlockRange.endOfMatcher.match(currentLine);
            if (!end) {
                currentLine = readNextLine();
            }
        }
        if (end) {
            markEndOfBlock();
            currentLine = getNextLine();
        }
        return currentLine;
    }

    private String uncommentLineInBlock(String currentLine) throws IOException {
        // remove comments up until the end of the block range
        if (isCommentedLine(currentLine)) {
            currentLine = currentLine.replaceFirst("//", "");
        }
        if (currentBlockRange.endOfMatcher.match(currentLine)) {
            if (currentBlockRange == BlockRange.blockWithEndMarker) {
                markEndOfBlock();
                currentLine = getNextLine();
            } else {
                markEndOfBlock();
            }
        }
        return currentLine;
    }

    /**
     * Used to initialize the value of the booleans members.
     */
    private void markEndOfBlock() {
        insideCodeBlock = false;
        currentBlockRange = null;
        toUncomment = false;
    }

    private enum BlockRange {
        line(new CurrentLineMatcher() {
            @Override
            public boolean match(String line) {
                return true;
            }
        }),
        instruction(new CurrentLineMatcher() {
            @Override
            public boolean match(String line) {
                return endOfInstructionPattern.matcher(line).matches();
            }
        }),
        member(new CurrentLineMatcher() {
            @Override
            public boolean match(String line) {
                return endOfInstructionPattern.matcher(line).matches();
            }
        }),
        javadocs(new CurrentLineMatcher() {
            @Override
            public boolean match(String line) {
                return endOfJavadocsPattern.matcher(line).matches();
            }
        }),
        method(new CurrentLineMatcher() {
            @Override
            public boolean match(String line) {
                return endOfJavaBlockPattern.matcher(line).matches();
            }
        }),
        blockWithEndMarker(new CurrentLineMatcher() {
            @Override
            public boolean match(String line) {
                return endBlockPattern.matcher(line).matches();
            }
        });

        public final CurrentLineMatcher endOfMatcher;

        BlockRange(CurrentLineMatcher endOf) {
            this.endOfMatcher = endOf;
        }

        public static interface CurrentLineMatcher {
            boolean match(String line);
        }

        private static final Pattern endOfInstructionPattern = Pattern.compile(".*;\r*\n*");

        private static final Pattern endOfJavaBlockPattern = Pattern.compile("\\s*}.*");

        private static final Pattern endOfJavadocsPattern = Pattern.compile("\\s*\\*/.*");

    }

    private boolean isCommentedLine(String line) {
        return !(line == null || line.length() < 2
                || startBlockPattern.matcher(line).matches()
                || endBlockPattern.matcher(line).matches())
                && commentedLinePattern.matcher(line).matches();
    }

    private String readNextLine() throws IOException {
        return wrappedReader.readLine();
    }

}
