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

package org.restlet.ext.apispark;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.engine.Engine;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.internal.conversion.TranslationException;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.SwaggerUtils;
import org.restlet.ext.apispark.internal.introspection.IntrospectionHelper;
import org.restlet.ext.apispark.internal.introspection.application.ApplicationIntrospector;
import org.restlet.ext.apispark.internal.introspection.application.ComponentIntrospector;
import org.restlet.ext.apispark.internal.introspection.jaxrs.JaxRsIntrospector;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.utils.CliUtils;
import org.restlet.ext.apispark.internal.utils.IntrospectionUtils;

/**
 * Generates the Web API documentation of a Restlet based {@link Application}
 * and imports it into the APISpark console.
 * 
 * @author Thierry Boileau
 */
public class Introspector {

    /** Internal logger. */
    private static Logger LOGGER = Engine.getLogger(Introspector.class);

    /**
     * Returns the value according to its index.
     * 
     * @param args
     *            The argument table.
     * @param index
     *            The index of the argument.
     * @return The value of the given argument.
     */
    private static String getParameter(String[] args, int index) {
        if (index >= args.length) {
            return null;
        } else {
            String value = args[index];
            if ("-s".equals(value) || "-u".equals(value) || "-p".equals(value)
                    || "-d".equals(value) || "-c".equals(value)) {
                // In case the given value is actually an option, reset it.
                value = null;
            }
            return value;
        }
    }

    /**
     * Main class, invokes this class without argument to get help instructions.
     * 
     * @param args
     *            Main arguments.
     * @throws TranslationException
     */
    public static void main(String[] args) throws TranslationException {
        try {
            process(args);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Introspection error", e);
            System.exit(1);
        }
        System.exit(0);
    }

    public static void process(String[] args) throws TranslationException {
        Engine.register();
        String ulogin = null;
        String upwd = null;
        String serviceUrl = null;
        String defSource = null;
        String compName = null;
        String language = null;

        String cellId = null;
        String cellVersion = null;
        String cellType = null;

        boolean createNewCell = false;
        boolean createNewVersion = false;
        boolean updateCell = false;
        String updateStrategy = "update";

        boolean useSectionNamingPackageStrategy = false;

        // (default ?)
        LOGGER.fine("Get parameters");
        for (int i = 0; i < (args.length); i++) {
            String arg = args[i];
            if ("-h".equals(arg) || "--help".equals(arg)) {
                printHelp();
                System.exit(0);
            } else if ("-u".equals(arg) || "--username".equals(arg)) {
                ulogin = getParameter(args, ++i);
            } else if ("-p".equals(arg) || "--password".equals(arg)) {
                upwd = getParameter(args, ++i);
            } else if ("-S".equals(arg) || "--service".equals(arg)) {
                serviceUrl = getParameter(args, ++i);
            } else if ("-c".equals(arg) || "--create-connector".equals(arg)) {
                createNewCell = true;
                cellType = "webapiconnector";
            } else if ("-d".equals(arg) || "--create-descriptor".equals(arg)) {
                createNewCell = true;
                cellType = "webapidescriptor";
            } else if ("--component".equals(arg)) {
                compName = getParameter(args, ++i);
            } else if ("-i".equals(arg) || "--id".equals(arg)) {
                cellId = getParameter(args, ++i);
            } else if ("-v".equals(arg) || "--version".equals(arg)) {
                cellVersion = getParameter(args, ++i);
            } else if ("-U".equals(arg) || "--update".equals(arg)) {
                updateCell = true;
            } else if ("-s".equals(arg) || "--update-strategy".equals(arg)) {
                updateStrategy = getParameter(args, ++i).toLowerCase();
            } else if ("-n".equals(arg) || "--new-version".equals(arg)) {
                createNewVersion = true;
            } else if ("-l".equals(arg) || "--language".equals(arg)) {
                language = getParameter(args, ++i).toLowerCase();
            } else if ("--sections".equals(arg)) {
                useSectionNamingPackageStrategy = true;
            } else if ("-V".equals(arg) || "--verbose".equals(arg)) {
                // [ifndef gae,jee] instruction
                Engine.setLogLevel(Level.FINE);
            } else {
                defSource = arg;
            }
        }

        if (!createNewCell && !createNewVersion && !updateCell) {
            LOGGER.severe("You should specify the wanted action among -d (--create-descriptor), -c (--create-connector), "
                    + "-U (--update) or -n (--new-version). "
                    + "Use parameter --help for help.");
            System.exit(1);
        }

        if (createNewCell) {
            if (createNewVersion || updateCell) {
                LOGGER.severe("In create new cell mode, you can't use -U (--update) or -n (--new-version). "
                        + "Use parameter --help for help.");
                System.exit(1);
            }
            if (cellId != null || cellVersion != null) {
                LOGGER.severe("In create new cell mode, you can't use -i (--id) or -v (--version). "
                        + "Use parameter --help for help.");
                System.exit(1);
            }
        }
        if (createNewVersion) {
            if (createNewCell || updateCell) {
                LOGGER.severe("In create new version mode, you can't use -d (--create-descriptor), -c (--create-connector) or -n (--new-version). "
                        + "Use parameter --help for help.");
                System.exit(1);
            }
            if (cellId == null) {
                LOGGER.severe("In create new version mode, you should specify the cell id with -i (--id). "
                        + "Use parameter --help for help.");
                System.exit(1);
            }
            if (cellVersion != null) {
                LOGGER.severe("In create new version mode, you can't use -v (--version). "
                        + "Use parameter --help for help.");
                System.exit(1);
            }
        }
        if (updateCell) {
            if (createNewCell || createNewVersion) {
                LOGGER.severe("In update mode, you can't use -d (--create-descriptor), -c (--create-connector) or -N (--new-version). "
                        + "Use parameter --help for help.");
                System.exit(1);
            }
            if (cellId == null || cellVersion == null) {
                LOGGER.severe("In update mode, you should specify the cell id with -i (--id) and the cell version with -v (--version). "
                        + "Use parameter --help for help.");
                System.exit(1);
            }
            if (!IntrospectionUtils.STRATEGIES.contains(updateStrategy)) {
                LOGGER.severe("The strategy: " + updateStrategy
                        + " is not available. Use parameter --help for help.");
                System.exit(1);
            }
        }

        if (StringUtils.isNullOrEmpty(ulogin)
                || StringUtils.isNullOrEmpty(upwd)) {
            LOGGER.severe("You should specify your API spark login and password with -U (--username) and -p (--password). "
                    + "Use parameter --help for help.");
            System.exit(1);
        }

        if (StringUtils.isNullOrEmpty(defSource)) {
            LOGGER.severe("You should specify the definition source to use (value no prefixed by any option). "
                    + "Use parameter --help for help.");
            System.exit(1);
        }

        if (StringUtils.isNullOrEmpty(serviceUrl)) {
            serviceUrl = "https://apispark.restlet.com/";
        }
        if (!serviceUrl.endsWith("/")) {
            serviceUrl += "/";
        }

        Engine.getLogger("").getHandlers()[0]
                .setFilter(new java.util.logging.Filter() {
                    public boolean isLoggable(LogRecord record) {
                        return record.getLoggerName().startsWith(
                                "org.restlet.ext.apispark");
                    }
                });

        // Discover introspection helpers
        List<IntrospectionHelper> introspectionHelpers = new ArrayList<>();
        ServiceLoader<IntrospectionHelper> ihLoader = ServiceLoader
                .load(IntrospectionHelper.class);
        for (IntrospectionHelper helper : ihLoader) {
            introspectionHelpers.add(helper);
        }

        // Validate the application class name
        Definition definition = null;

        // get definition
        if (language == null) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(defSource);
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE,
                        "Cannot locate the application class.", e);
                System.exit(1);
            }
            // Is Restlet application ?
            // TODO implement introspection of Restlet based JaxRs
            // (org.restlet.ext.jaxrs.JaxRsApplication)
            if (Application.class.isAssignableFrom(clazz)) {
                Application application = ApplicationIntrospector
                        .getApplication(defSource);
                Component component = ComponentIntrospector
                        .getComponent(compName);
                definition = ApplicationIntrospector.getDefinition(application,
                        null, component, useSectionNamingPackageStrategy);
            } else if (clazz != null) {
                javax.ws.rs.core.Application jaxrsApplication = JaxRsIntrospector
                        .getApplication(defSource);
                definition = JaxRsIntrospector.getDefinition(jaxrsApplication,
                        null, useSectionNamingPackageStrategy);
            } else {
                LOGGER.log(Level.SEVERE, "Class " + defSource
                        + " is not supported");
                System.exit(1);
            }
        } else {
            if ("swagger".equals(language)) {
                definition = SwaggerUtils
                        .getDefinition(defSource, ulogin, upwd);
            } else {
                LOGGER.log(Level.SEVERE, "Language " + language
                        + " is not supported");
                System.exit(1);
            }
        }

        if (definition == null) {
            LOGGER.severe("Please provide a valid application class name or definition URL.");
            System.exit(1);
        }

        IntrospectionUtils.sendDefinition(definition, ulogin, upwd, serviceUrl,
                cellType, cellId, cellVersion, createNewCell, createNewVersion,
                updateCell, updateStrategy, LOGGER);

        LOGGER.info("Instrospection complete");
    }

    /**
     * Prints the instructions necessary to launch this tool.
     */
    private static void printHelp() {
        CliUtils cli = new CliUtils(System.out, 100);
        cli.print();
        cli.print0("SYNOPSIS");
        cli.print1("org.restlet.ext.apispark.Introspector [credentials] [actions] [options] [--language swagger SWAGGER_DEFINITION_URL_OR_PATH | APPLICATION]");

        cli.print();
        cli.print0("DESCRIPTION");
        cli.print1(
                "Publish to the APISpark platform the description of your Web API, represented by APPLICATION, the full name of your Restlet or JAX-RS application class or by the Swagger definition available at URL/PATH",
                "If the whole process is successful, it displays the url of the corresponding descriptor or connector cell.");

        cli.print();
        cli.print0("EXAMPLES");
        cli.print1(
                "org.restlet.ext.apispark.Introspector -u 1234 -p Xy12 --create-descriptor com.acme.Application",
                "org.restlet.ext.apispark.Introspector -u 1234 -p Xy12 --new-version --id 60 com.acme.Application",
                "org.restlet.ext.apispark.Introspector -u 1234 -p Xy12 --update --update-strategy replace --id 60 --version 1 --language swagger http://acme.com/api/swagger");

        cli.print();
        cli.print0("OPTIONS");
        cli.print12("-h, --help" + "Prints this help.");
        cli.print();
        cli.print1("[credentials]");
        cli.print12("-u, --username username",
                "The mandatory APISpark user name.");
        cli.print12("-p, --password password",
                "The mandatory APISpark user secret key.");
        cli.print();
        cli.print1("[actions]");
        cli.print12("-d, --create-descriptor",
                "Creates a new descriptor from introspection.");
        cli.print12("-c, --create-connector",
                "Creates a new connector from introspection.");
        cli.print12(
                "-n, --new-version",
                "Creates a new version of the descriptor/connector identified by the -i (--id) option");
        cli.print12(
                "-U, --update",
                "Updates the cell descriptor/connector specified by the -i (--id) and -v (--version) options.",
                "Use the default update strategy (update) except if -S (--update-strategy) option is specified.");
        cli.print();
        cli.print1("[options]");
        cli.print12(
                "-i, --id cellId",
                "The identifier of an existing cell hosted by APISpark you want to update with this new documentation.",
                "Required if -n (--new-version) or -U (--update) options are specified.");
        cli.print12("-v, --version cellVersion",
                "The version of the cell to be updated.",
                "Required if -U (--update) option is specified.");
        cli.print12(
                "-s, --update-strategy strategy",
                "Specifies the update strategy.",
                "Available strategies:",
                "- update: (default) new objects will be added to the APISpark's descriptor/connector, primitive fields of existing objects will be updated. Nothing will be deleted.",
                "- replace: deletes all the information in the descriptor/connector on APISpark's and fills it again with introspected definition.");
        cli.print12(
                "--component componentClass",
                "The optional full name of your Restlet Component class. This allows to collect some",
                "other data, such as the endpoint.");
        cli.print12(
                "-l, --language languageName",
                "The optional name of the description language of the definition you want to upload.",
                "Possible value:", "- swagger: Swagger 1.2 specification.");
        cli.print12("--sections",
                "Set section of introspected resources from java package name.");
        cli.print12("-v, --verbose",
                "The optional parameter switching the process to a verbose mode");

        cli.print();
        cli.print0("ENHANCE INTROSPECTION");
        cli.print1(
                "You can extend the basic introspection and enrich the generated documentation by providing",
                "dedicated helpers to the introspector.",
                "By default, swagger annotation are supported.",
                "Introspection use the Java Service Loader system.",
                "To add a new helper, create a",
                "'META-INF/services/org.restlet.ext.apispark.internal.introspection.IntrospectionHelper' file",
                "with the name of your implementation class.");
    }
}
