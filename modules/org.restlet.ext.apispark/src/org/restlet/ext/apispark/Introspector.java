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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.internal.conversion.TranslationException;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.SwaggerUtils;
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

    private static final List<String> SUPPORTED_LANGUAGES = Arrays
            .asList("swagger");

    private static void failWithErrorMessage(String message) {
        LOGGER.severe(message + "Use parameter --help for help.");
        System.exit(1);
    }

    private static Definition getDefinitionFromJaxrsSources(String defSource,
            boolean useSectionNamingPackageStrategy, String applicationName,
            String endpoint, List<String> jaxRsResources) {
        javax.ws.rs.core.Application jaxrsApplication = JaxRsIntrospector
                .getApplication(defSource);
        @SuppressWarnings("rawtypes")
        List<Class> resources = new ArrayList<>();
        try {
            for (String c : jaxRsResources) {
                resources.add(Class.forName(c));
            }
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Cannot locate the JAXRS resource class.",
                    e);
            System.exit(1);
        }
        Reference baseRef = endpoint != null ? new Reference(endpoint) : null;
        return JaxRsIntrospector.getDefinition(jaxrsApplication,
                applicationName, resources, baseRef,
                useSectionNamingPackageStrategy);
    }

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

        String applicationName = null;
        String endpoint = null;
        List<String> jaxRsResources = new ArrayList<>();

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
            } else if ("--application-name".equals(arg)) {
                applicationName = getParameter(args, ++i);
            } else if ("--endpoint".equals(arg)) {
                endpoint = getParameter(args, ++i);
            } else if ("--jaxrs-resources".equals(arg)) {
                jaxRsResources = Arrays.asList(getParameter(args, ++i).split(
                        ","));
            } else {
                defSource = arg;
            }
        }

        if (!createNewCell && !createNewVersion && !updateCell) {
            failWithErrorMessage("You should specify the wanted action among -d (--create-descriptor), -c (--create-connector), "
                    + "-U (--update) or -n (--new-version). ");
        }

        if (createNewCell) {
            if (createNewVersion || updateCell) {
                failWithErrorMessage("In create new cell mode, you can't use -U (--update) or -n (--new-version). ");
            }
            if (cellId != null || cellVersion != null) {
                failWithErrorMessage("In create new cell mode, you can't use -i (--id) or -v (--version). ");
            }
        }
        if (createNewVersion) {
            if (createNewCell || updateCell) {
                failWithErrorMessage("In create new version mode, you can't use -d (--create-descriptor), -c (--create-connector) or -n (--new-version). ");
            }
            if (cellId == null) {
                failWithErrorMessage("In create new version mode, you should specify the cell id with -i (--id). ");
            }
            if (cellVersion != null) {
                failWithErrorMessage("In create new version mode, you can't use -v (--version). ");
            }
        }
        if (updateCell) {
            if (createNewCell || createNewVersion) {
                failWithErrorMessage("In update mode, you can't use -d (--create-descriptor), -c (--create-connector) or -N (--new-version). ");
            }
            if (cellId == null || cellVersion == null) {
                failWithErrorMessage("In update mode, you should specify the cell id with -i (--id) and the cell version with -v (--version). ");
            }
            if (!IntrospectionUtils.STRATEGIES.contains(updateStrategy)) {
                failWithErrorMessage("The strategy: " + updateStrategy
                        + " is not available. ");
            }
        }

        if (StringUtils.isNullOrEmpty(ulogin)
                || StringUtils.isNullOrEmpty(upwd)) {
            failWithErrorMessage("You should specify your API spark login and password with -U (--username) and -p (--password). ");
        }

        if (StringUtils.isNullOrEmpty(defSource) && jaxRsResources.isEmpty()) {
            failWithErrorMessage("You should specify the definition source to use (value no prefixed by any option). ");
        }

        if (!StringUtils.isNullOrEmpty(language)
                && !SUPPORTED_LANGUAGES.contains(language)) {
            failWithErrorMessage("The language " + language
                    + " is not currently supported. ");
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

        // Validate the application class name
        Definition definition = null;

        // get definition
        if (language != null) {
            if ("swagger".equals(language)) {
                definition = SwaggerUtils
                        .getDefinition(defSource, ulogin, upwd);
            } else {
                failWithErrorMessage("The language " + language
                        + " is not currently supported. ");
            }
        } else {
            if (defSource != null) {
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
                    Reference baseRef = endpoint != null ? new Reference(
                            endpoint) : null;
                    if (applicationName != null) {
                        application.setName(applicationName);
                    }
                    definition = ApplicationIntrospector.getDefinition(
                            application, baseRef, component,
                            useSectionNamingPackageStrategy);
                } else if (javax.ws.rs.core.Application.class
                        .isAssignableFrom(clazz)) {
                    definition = getDefinitionFromJaxrsSources(defSource,
                            useSectionNamingPackageStrategy, applicationName,
                            endpoint, jaxRsResources);
                } else {
                    LOGGER.log(Level.SEVERE, "Class " + defSource
                            + " is not supported");
                    System.exit(1);
                }
            } else if (!jaxRsResources.isEmpty()) {
                definition = getDefinitionFromJaxrsSources(defSource,
                        useSectionNamingPackageStrategy, applicationName,
                        endpoint, jaxRsResources);
            }
        }

        if (definition == null) {
            failWithErrorMessage("Please provide a valid application class name or definition URL.");
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
                "The optional parameter switching the process to a verbose mode.");
        cli.print12("--application-name name",
                "The optional parameter overriding the name of the API.");
        cli.print12("--endpoint endpoint",
                "The optional parameter overriding the endpoint of the API.");
        cli.print12(
                "--jaxrs-resources resourcesClasses",
                "The optional parameter providing the list of fully qualified classes separated by a "
                        + "comma that should be introspected. Example: com.example.MyResource,com.example.MyResource2.",
                "Replaces javax.ws.rs.core.Application#getClasses.");

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
