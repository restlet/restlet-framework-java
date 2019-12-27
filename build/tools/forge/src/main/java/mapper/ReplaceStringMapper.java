package mapper;

import org.apache.tools.ant.util.FileNameMapper;

/**
 * Own implementation of the buggy FilterMapper class provided by ant.
 */
public class ReplaceStringMapper implements FileNameMapper {
    private String fileSeparator = System.getProperty("file.separator");

    private String from;

    private String to;

    public String[] mapFileName(String sourceFileName) {
        String[] result = new String[1];
        result[0] = sourceFileName.replace(from, to);
        return result;
    }

    public void setFrom(String from) {
        this.from = from.replace("/", fileSeparator);
    }

    public void setTo(String to) {
        this.to = to.replace("/", fileSeparator);
    }

}
