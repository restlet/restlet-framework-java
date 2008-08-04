/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.ext.shell;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.restlet.ext.shell.helpers.VersionHelper;

/**
 * 
 * @author Davide Angelocola
 */
class CommandLineArguments {

    private CommandLine commandLine;

    private String error;

    private static CommandLineParser commandLineParser;

    private static Options options;

    private static String BUGS = "\nReport bugs to <davide.angelocola@gmail.com>";

    static {
        options = new Options();
        options.addOption(new Option("help", "this help"));
        options.addOption(new Option("version", "show version"));
        options.addOption(new Option("server", true,
                "start an HTTP server on the specified port"));
        commandLineParser = new GnuParser();
    }

    public CommandLineArguments(String[] args) {
        try {
            this.commandLine = commandLineParser.parse(options, args);
        } catch (final ParseException ex) {
            this.error = ex.getMessage();
        }
    }

    public String getError() {
        return this.error;
    }

    public String getHelp() {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter writer = new PrintWriter(stringWriter);
        final HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(writer, 80, "", "", options, 5, 10, BUGS, true);
        return stringWriter.toString();
    }

    public int getServerPort() {
        int port;

        try {
            port = Integer.parseInt(this.commandLine.getOptionValue("server"));
        } catch (final NumberFormatException e) {
            port = 8080;
        }

        return port;
    }

    public String[] getSourceFiles() {
        return this.commandLine.getArgs();
    }

    public String getVersion() {
        return VersionHelper.getVersion();
    }

    public boolean haveError() {
        return this.error != null;
    }

    public boolean haveHelp() {
        return this.commandLine.hasOption("help");
    }

    public boolean haveServer() {
        return this.commandLine.hasOption("server");
    }

    public boolean haveSourceFiles() {
        return this.commandLine.getArgs().length > 0;
    }

    public boolean haveVersion() {
        return this.commandLine.hasOption("version");
    }
}
