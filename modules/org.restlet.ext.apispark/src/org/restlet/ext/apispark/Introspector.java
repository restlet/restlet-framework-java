/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.engine.Engine;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.apispark.internal.conversion.TranslationException;
import org.restlet.ext.apispark.internal.conversion.swagger.v1_2.SwaggerUtils;
import org.restlet.ext.apispark.internal.introspection.IntrospectionHelper;
import org.restlet.ext.apispark.internal.introspection.application.ApplicationIntrospector;
import org.restlet.ext.apispark.internal.introspection.application.ComponentIntrospector;
import org.restlet.ext.apispark.internal.introspection.jaxrs.JaxRsIntrospector;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.utils.IntrospectionUtils;

/**
 * Generates the Web API documentation of a Restlet based {@link Application}
 * and imports it into the APISpark console.
 * 
 * @author Thierry Boileau
 */
public class Introspector {

    /** Internal logger. */
    private static Logger LOGGER = Context.getCurrentLogger();

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
        Engine.register();
        String ulogin = null;
        String upwd = null;
        String serviceUrl = null;
        String defSource = null;
        String compName = null;
        String descriptorId = null;
        String language = null;
        String versionId = null;
        String updateStrategy = null;
        List<IntrospectionHelper> introspectionHelpers = new ArrayList<IntrospectionHelper>();
        boolean newVersion = false;
        boolean create = false;

        // TODO add option for enable ou disable swagger annotation support
        // (default ?)
        LOGGER.fine("Get parameters");
        for (int i = 0; i < (args.length); i++) {
            if ("-h".equals(args[i])) {
                printHelp();
                System.exit(0);
            } else if ("-u".equals(args[i]) || "--username".equals(args[i])) {
                ulogin = getParameter(args, ++i);
            } else if ("-p".equals(args[i]) || "--password".equals(args[i])) {
                upwd = getParameter(args, ++i);
            } else if ("-s".equals(args[i]) || "--service".equals(args[i])) {
                serviceUrl = getParameter(args, ++i);
            } else if ("-c".equals(args[i]) || "--component".equals(args[i])) {
                compName = getParameter(args, ++i);
            } else if ("-d".equals(args[i]) || "--descriptor".equals(args[i])) {
                descriptorId = getParameter(args, ++i);
            } else if ("-v".equals(args[i]) || "--version".equals(args[i])) {
                versionId = getParameter(args, ++i);
            } else if ("-U".equals(args[i])
                    || "--updateStrategy".equals(args[i])) {
                updateStrategy = getParameter(args, ++i).toLowerCase();
            } else if ("-n".equals(args[i]) || "--newVersion".equals(args[i])) {
                newVersion = true;
            } else if ("-C".equals(args[i]) || "--create".equals(args[i])) {
                create = true;
            } else if ("-l".equals(args[i]) || "--language".equals(args[i])) {
                language = getParameter(args, ++i).toLowerCase();
            } else if ("-V".equals(args[i]) || "--verbose".equals(args[i])) {
                // [ifndef gae,jee] instruction
                Engine.setLogLevel(Level.FINE);
            } else {
                defSource = args[i];
            }
        }

        // Discover introspection helpers
        ServiceLoader<IntrospectionHelper> ihLoader = ServiceLoader
                .load(IntrospectionHelper.class);
        for (IntrospectionHelper helper : ihLoader) {
            introspectionHelpers.add(helper);
        }

        if (newVersion && create) {
            LOGGER.severe("You can't use newVersion and create at the same time. Use parameter --help for help.");
        } else if (create && updateStrategy != null) {
            LOGGER.severe("You can't use create and updateStrategy at the same time. Use parameter --help for help.");
        } else if (newVersion && updateStrategy != null) {
            LOGGER.severe("You can't use newVersion and updateStrategy at the same time. Use parameter --help for help.");
        } else if (!newVersion && updateStrategy == null) {
            create = true;
        }

        Engine.getLogger("").getHandlers()[0]
                .setFilter(new java.util.logging.Filter() {
                    public boolean isLoggable(LogRecord record) {
                        return record.getLoggerName().startsWith(
                                "org.restlet.ext.apispark");
                    }
                });

        LOGGER.fine("Check parameters");
        if (StringUtils.isNullOrEmpty(serviceUrl)) {
            serviceUrl = "https://apispark.com/api/";
        }
        if (!serviceUrl.endsWith("/")) {
            serviceUrl += "/";
        }

        if (StringUtils.isNullOrEmpty(ulogin)
                || StringUtils.isNullOrEmpty(upwd)
                || StringUtils.isNullOrEmpty(defSource)) {
            printHelp();
            System.exit(1);
        }

        // TODO validate the definition URL:
        // * accept absolute urls
        // * accept relative urls such as /definitions/{id} and concatenate with
        // the serviceUrl
        // * accept relative urls such as {id} and concatenate with the
        // serviceUrl

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
                throw new RuntimeException(
                        "Cannot locate the application class.", e);
            }
            // Is Restlet application ?
            // TODO implement introspection of Restlet based JaxRs (org.restlet.ext.jaxrs.JaxRsApplication)
            if (Application.class.isAssignableFrom(clazz)) {
                Application application = ApplicationIntrospector
                        .getApplication(defSource);
                Component component = ComponentIntrospector
                        .getComponent(compName);
                definition = ApplicationIntrospector.getDefinition(application,
                        null, component, introspectionHelpers);
            } else if (clazz != null) {
                javax.ws.rs.core.Application jaxrsApplication = JaxRsIntrospector
                        .getApplication(defSource);
                definition = JaxRsIntrospector.getDefinition(jaxrsApplication,
                        null, introspectionHelpers);
            } else {
                LOGGER.log(Level.SEVERE, "Class " + defSource
                        + " is not supported");
                throw new RuntimeException("Class " + defSource
                        + " is not supported");
            }
        } else {
            if ("swagger".equals(language)) {
                definition = SwaggerUtils
                        .getDefinition(defSource, ulogin, upwd);
            } else {
                LOGGER.log(Level.SEVERE, "Language " + language
                        + " is not supported");
                throw new RuntimeException("Language " + language
                        + " is not supported");
            }
        }

        if (definition != null) {
            IntrospectionUtils.sendDefinition(definition, descriptorId,
                    versionId, ulogin, upwd, serviceUrl, updateStrategy,
                    create, newVersion, LOGGER);

        } else {
            LOGGER.severe("Please provide a valid application class name or definition URL.");
        }
    }

    /**
     * Prints the instructions necessary to launch this tool.
     */
    private static void printHelp() {
        PrintStream o = System.out;

        o.println("SYNOPSIS");
        IntrospectionUtils
                .printSynopsis(o, Introspector.class,
                        "[options] [--language swagger SWAGGER DEFINITION URL/PATH | APPLICATION]");
        IntrospectionUtils
                .printSynopsis(
                        o,
                        Introspector.class,
                        "--create [options] [--language swagger SWAGGER DEFINITION URL/PATH | APPLICATION]");
        IntrospectionUtils
                .printSynopsis(
                        o,
                        Introspector.class,
                        "--newVersion --descriptor descriptorId [options] [--language swagger SWAGGER DEFINITION URL/PATH | APPLICATION]");
        IntrospectionUtils
                .printSynopsis(
                        o,
                        Introspector.class,
                        "--updateStrategy strategy --descriptor descriptorId --version versionId [options] [--language swagger SWAGGER DEFINITION URL/PATH | APPLICATION]");

        o.println("DESCRIPTION");
        IntrospectionUtils
                .printSentence(
                        o,
                        "Publish to the APISpark platform the description of your Web API, represented by APPLICATION,",
                        "the full name of your Restlet or JAX-RS application class or by the Swagger definition available at ",
                        "URL/PATH");
        IntrospectionUtils
                .printSentence(
                        o,
                        "If the whole process is successfull, it displays the url of the corresponding documentation.");
        o.println("OPTIONS");
        IntrospectionUtils.printOption(o, "-h, --help", "Prints this help.");
        IntrospectionUtils.printOption(o, "-u, --username username",
                "The mandatory APISpark user name.");
        IntrospectionUtils.printOption(o, "-p, --password password",
                "The mandatory APISpark user secret key.");
        IntrospectionUtils
                .printOption(
                        o,
                        "-c, --component commponent class",
                        "The optional full name of your Restlet Component class.",
                        "This allows to collect some other data, such as the endpoint.");
        IntrospectionUtils
                .printOption(o, "-C, --create",
                        "Creates a new descriptor from introspection.",
                        "Is set to true if neither --newVersion nor --updateStrategy are specified.");
        IntrospectionUtils
                .printOption(o, "-n, --newVersion",
                        "Creates a new version of the descriptor identified by the --descriptor option");
        IntrospectionUtils
                .printOption(
                        o,
                        "-d, --descriptor descriptorId",
                        "The optional identifier of an existing descriptor hosted by APISpark you want to update with this new documentation.",
                        "Required if --updateStrategy or --newVersion options are specified.");
        IntrospectionUtils
                .printOption(
                        o,
                        "-U, --updateStrategy strategy",
                        "Updates the descriptor version specified by the --descriptor and --version options with the given strategy. If no strategy is specified, the \"add\" strategy is selected by default. \n",
                        "Strategies available:\n",
                        "\"add\": new objects will be added to the APISpark's descriptor, primitive fields of existing objects will be updated. Nothing will be deleted.\n",
                        "\"reset\": deletes all the information in the descriptor on APISpark's and fills it again with introspected definition.");
        IntrospectionUtils
                .printOption(
                        o,
                        "-v, --version versionId",
                        "The version of the descriptor to be updated. Required if --updateStrategy is specified.");
        IntrospectionUtils
                .printOption(
                        o,
                        "-l, --language languageName",
                        "The optional name of the description language of the definition you want to upload. Possible value: \"swagger\"");
        IntrospectionUtils
                .printOption(o, "-v, --verbose",
                        "The optional parameter switching the process to a verbose mode");
        o.println("ENHANCE INTROSPECTION");
        IntrospectionUtils
                .printSentence(
                        o,
                        "You can extend the basic introspection and enrich the generated documentation by providing dedicated helpers to the introspector.",
                        "For example, if you are using Swagger annotations inside your code, complete the classpath by adding the jar either ",
                        "of the org.restlet.ext.apispark-swagger-annotation-2_0 extension or the the org.restlet.ext.apispark-swagger-annotation-1_2 extension. \n");
    }
}
