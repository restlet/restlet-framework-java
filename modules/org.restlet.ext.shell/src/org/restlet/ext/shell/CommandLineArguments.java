package org.restlet.ext.shell;

import org.restlet.ext.shell.helpers.VersionHelper;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
        options.addOption(new Option("server", true, "start an HTTP server on the specified port"));
        commandLineParser = new GnuParser();
    }

    public CommandLineArguments(String[] args) {
        try {
            commandLine = commandLineParser.parse(options, args);
        } catch (ParseException ex) {
            error = ex.getMessage();
        }
    }

    public boolean haveError() {
        return error != null;
    }

    public String getError() {
        return error;
    }

    public boolean haveHelp() {
        return commandLine.hasOption("help");
    }

    public String getHelp() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(writer, 80, "", "", options, 5, 10, BUGS, true);
        return stringWriter.toString();
    }

    public boolean haveVersion() {
        return commandLine.hasOption("version");
    }

    public String getVersion() {
        return VersionHelper.getVersion();
    }

    public boolean haveSourceFiles() {
        return commandLine.getArgs().length > 0;
    }

    public String[] getSourceFiles() {
        return commandLine.getArgs();
    }

    public boolean haveServer() {
        return commandLine.hasOption("server");
    }

    public int getServerPort() {
        int port;

        try {
            port = Integer.parseInt(commandLine.getOptionValue("server"));
        } catch (NumberFormatException e) {
            port = 8080;
        }

        return port;
    }
}
