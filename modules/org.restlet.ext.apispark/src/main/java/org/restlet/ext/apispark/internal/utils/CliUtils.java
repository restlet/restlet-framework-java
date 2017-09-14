/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.internal.utils;

import java.io.PrintStream;

/**
 * @author Manuel Boillod
 */
public class CliUtils {

    private final PrintStream o;
    private int columnSize;
    private int indentSize = 7;

    public CliUtils(PrintStream o, int columnSize) {
        this.o = o;
        this.columnSize = columnSize;
    }


    /**
     * Print a new line
     */
    public void print() {
        o.println();
    }

    /**
     * Print each string with a indentation of 0
     *
     * @param strings
     *            The strings to display
     */
    public void print0(String... strings) {
        printSentences(0, strings);
    }

    /**
     * Print each string with a indentation of 1
     *
     * @param strings
     *            The strings to display
     */
    public void print1(String... strings) {
        printSentences(1, strings);
    }

    /**
     * Print each string with a indentation of 2
     *
     * @param strings
     *            The strings to display
     */
    public void print2(String... strings) {
        printSentences(2, strings);
    }

    /**
     * Print the first string with a indentation of 1
     * and others strings with a indentation of 2
     *
     * @param strings
     *            The strings to display
     */
    public void print12(String string1, String... strings) {
        print1(string1);
        print2(strings);
    }

    /**
     * Formats a list of Strings by lines of columnSize characters maximum, and displays
     * it to the console.
     *
     * @param indent
     *            The number of indentation to shift the list of strings on the
     *            left.
     * @param strings
     *            The list of Strings to display.
     */
    public void printSentences(int indent, String... strings) {
        for (String string : strings) {
            printSentence(indent, string);
        }

    }
    public void printSentence(int indent, String string) {
        int shift = indent * indentSize;
        int blockLength = columnSize - shift - 1;
        String tab = "";
        for (int i = 0; i < shift; i++) {
            tab = tab.concat(" ");
        }
        String sentence = string.replace("\n", "\n" + tab);
        // Cut in slices
        int index = 0;
        while (index < (sentence.length() - 1)) {
            o.print(tab);
            int length = Math.min(index + blockLength, sentence.length() - 1);
            if ((length - index) < blockLength) {
                o.println(sentence.substring(index));
                index = length + 1;
            } else if (sentence.charAt(length) == ' ') {
                o.println(sentence.substring(index, length));
                index = length + 1;
            } else {
                length = sentence.substring(index, length - 1).lastIndexOf(' ');
                if (length != -1) {
                    o.println(sentence.substring(index, index + length));
                    index += length + 1;
                } else {
                    length = sentence.substring(index).indexOf(' ');
                    if (length != -1) {
                        o.println(sentence.substring(index, index + length));
                        index += length + 1;
                    } else {
                        o.println(sentence.substring(index));
                        index = sentence.length();
                    }
                }
            }
        }
    }
}
