package org.restlet.edition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class CodeBlockFilterTest extends TestCase {

    public void testCodeFilterJse() throws IOException {
        Reader expectedReader = new InputStreamReader(getClass().getResourceAsStream("source.jse.txt"));
        Reader sourceToFilterReader = new InputStreamReader(getClass().getResourceAsStream("source.txt"));

        assertEquals(readAll(expectedReader), readAll(new CodeBlockFilter(sourceToFilterReader, "jse")));
    }

    public void testCodeFilterGwt() throws IOException {
        Reader expectedReader = new InputStreamReader(getClass().getResourceAsStream("source.gwt.txt"));
        Reader sourceToFilterReader = new InputStreamReader(getClass().getResourceAsStream("source.txt"));

        assertEquals(readAll(expectedReader), readAll(new CodeBlockFilter(sourceToFilterReader, "gwt")));
    }

    private List<String> readAll(Reader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader b = new BufferedReader(reader)) {
            String line;
            while ((line = b.readLine()) != null) {
                lines.add(line);
            }
        }

        return lines;
    }
}
