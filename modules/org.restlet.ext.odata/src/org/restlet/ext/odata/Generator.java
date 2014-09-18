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

package org.restlet.ext.odata;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.odata.internal.edm.ComplexType;
import org.restlet.ext.odata.internal.edm.EntityContainer;
import org.restlet.ext.odata.internal.edm.EntityType;
import org.restlet.ext.odata.internal.edm.Metadata;
import org.restlet.ext.odata.internal.edm.Schema;
import org.restlet.ext.odata.internal.edm.TypeUtils;
import org.restlet.ext.odata.internal.reflect.ReflectUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

import freemarker.template.Configuration;

/**
 * Code generator for accessing OData services. The generator use metadata
 * exposed by an online service to generate client-side artifacts facilitating
 * the execution of queries on the available entities.
 * 
 * @author Thierry Boileau
 */
public class Generator {

	/** The Constant SERVICE_URL. */
	private final static String SERVICE_URL = "serviceUrl";

	/** The Constant USERNAME. */
	private final static String USERNAME = "userName";

	/** The Constant PASSWORD. */
	private final static String PASSWORD = "password";

	/** The Constant CHALLENGE_SCHEME. */
	private final static String CHALLENGE_SCHEME = "challengeScheme";

	/** The Constant SERVICE_CLASSNAME. */
	private final static String SERVICE_CLASSNAME = "serviceClassName";

	/** The Constant SERVICE_CLASS_DIR. */
	private final static String SERVICE_CLASS_DIR = "serviceClassDir";

	/** The Constant ENTITY_CLASS_DIR. */
	private final static String ENTITY_CLASS_DIR = "entityClassDir";

	/** The URI of the target data service. */
	private Reference serviceUrl;

	/** The user name. */
	private static String userName;

	/** The password. */
	private static String password;

	/** The challenge scheme. */
	private static ChallengeScheme challengeScheme;

	/** The name of the service class (in case there is only one in the schema). */
	private static String serviceClassName;

	/** The entity pkg. */
	private static String entityPkg;

	/** The service pkg. */
	private static String servicePkg;

	/** The package dir where entity classes are generated. */
	private static File packageDir;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(Generator.class.getName());


	/**
	 * Default Constructor.
	 * 
	 * @param serviceRef
	 *            The URI of the OData service.
	 */
	public Generator(Reference serviceRef) {
		this(serviceRef, null);
	}

	/**
	 * Constructor. The name of the service class can be provided if there is
	 * only one service defined in the metadata.
	 * 
	 * @param serviceRef
	 *            The URI of the OData service.
	 * @param serviceClassName
	 *            The name of the service class (in case there is only one in
	 *            the metadata).
	 */
	public Generator(Reference serviceRef, String serviceClassName) {
		super();
		this.serviceUrl = serviceRef;
		if (serviceClassName != null) {
			Generator.serviceClassName = ReflectUtils.normalize(serviceClassName);
			Generator.serviceClassName = Generator.serviceClassName.substring(0, 1)
					.toUpperCase() + Generator.serviceClassName.substring(1);
		}

	}

	/**
	 * Constructor.
	 * 
	 * @param serviceUri
	 *            The URI of the OData service.
	 */
	public Generator(String serviceUri) {
		this(serviceUri, null);
	}

	/**
	 * Constructor. The name of the service class can be provided if there is
	 * only one service defined in the metadata.
	 * 
	 * @param serviceUri
	 *            The URI of the OData service.
	 * @param serviceClassName
	 *            The name of the service class (in case there is only one in
	 *            the metadata).
	 */
	public Generator(String serviceUri, String serviceClassName) {
		this(new Reference(serviceUri), serviceClassName);
	}


	/**
	 * Takes seven parameters:<br>
	 * <ol>
	 * <li>The URI of the OData service</li>
	 * <li>Username to access OData service</li>
	 * <li>Password to access OData service</li>
	 * <li>ChallangeScheme - Possible values HTTP_BASIC, HTTP_NEGOTIATE, if not provided then HTTP_BASIC will be used as default.</li>
	 * <li>Service class name</li>
	 * <li>The output directory for Service class generation (For example : src/com/ow/service), 
	 *       if no directory is provided class will be generated in the default package.</li>
	 * <li>The output directory for Entity class generation (For example : src/com/ow/entities), 
	 *       if no directory is provided schema name will be used as default package.</li>
	 * </ol>
	 * 
	 * @param args
	 *            The list of arguments.
	 */
	public static void main(String[] args) {
						
		System.out.println("---------------------------");
		System.out.println("OData client code generator");
		System.out.println("---------------------------");
		System.out.println("step 1 - check parameters");

		String errorMessage = null;
		File entityClassDir = null;
		File serviceDir = null;

		final CommandLineParser parser = new BasicParser();
		final Options options = new Options();
		setCommandLineOptions(options);

		CommandLine commandLine = null;
		// try to parse and validate the passed arguments using apache commons cli.
		try {
			commandLine = parser.parse(options, args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			printUsage(options);
		}

		userName = getOption(USERNAME, commandLine);
		password = getOption(PASSWORD, commandLine);
		challengeScheme = (getOption(CHALLENGE_SCHEME, commandLine) == null ? null
				: ChallengeScheme.valueOf(getOption(CHALLENGE_SCHEME, commandLine)));

		serviceClassName = getOption(SERVICE_CLASSNAME, commandLine);

		if (getOption(SERVICE_CLASS_DIR, commandLine) != null) {
			serviceDir = new File(getOption(SERVICE_CLASS_DIR, commandLine));
			servicePkg = getOption(SERVICE_CLASS_DIR, commandLine).substring(4, getOption(SERVICE_CLASS_DIR, commandLine).length());
		} else {
			try {
				serviceDir = new File(".").getCanonicalFile();
				// set package name to blank if user did not pass this option
				servicePkg = "";
			} catch (IOException e) {
				errorMessage = "Unable to get the target directory for service generation. "
						+ e.getMessage();
			}
		}

		if (serviceDir.exists()) {
			System.out.println("step 2 - check the service directory");
			if (!serviceDir.isDirectory()) {
				errorMessage = serviceDir.getPath()
						+ " is not a valid directory.";
			}

		} else {
			try {
				System.out.println("step 2 - create the service directory");
				serviceDir.mkdirs();
			} catch (Throwable e) {
				errorMessage = "Cannot create " + serviceDir.getPath()
						+ " due to: " + e.getMessage();
			}
		}

		if (getOption(ENTITY_CLASS_DIR, commandLine) != null) {
			entityClassDir = new File(getOption(ENTITY_CLASS_DIR, commandLine));
			entityPkg = getOption(ENTITY_CLASS_DIR, commandLine).substring(4, getOption(ENTITY_CLASS_DIR, commandLine).length());
		}

		if (errorMessage == null) {
			System.out.println("step 3 - get the metadata descriptor");
			String dataServiceUri = getOption(SERVICE_URL, commandLine);

			if (dataServiceUri.endsWith("$metadata")) {
				dataServiceUri = dataServiceUri.substring(0, dataServiceUri.length() - 10);
			} else if (dataServiceUri.endsWith("/")) {
				dataServiceUri = dataServiceUri.substring(0, dataServiceUri.length() - 1);
			}

			Service service = new Service(dataServiceUri);
			if(challengeScheme != null && userName != null && password != null){
				service.setCredentials(new ChallengeResponse(challengeScheme, userName,
						password));
			}
			if (service.getMetadata() == null) {
				errorMessage = "Cannot retrieve the metadata.";
			} else {
				System.out.println("step 4 - generate source code");
				Generator svcUtil = new Generator(service.getServiceRef());

				try {
					svcUtil.generate(entityClassDir, serviceDir);
					System.out.print("The source code has been generated in directory: ");
					System.out.println(packageDir.getPath());
				} catch (Exception e) {
					errorMessage = "Cannot generate the source code in directory: "
							+ packageDir.getPath();                    
				}
			}
		}	else{
			LOGGER.log(Level.SEVERE,errorMessage);	
		}
	}

	/**
	 * Sets the command line options.
	 *
	 * @param options the new command line options
	 */
	private static void setCommandLineOptions(Options options) {

		/** Creates an Option for serviceUrl using the specified parameters  */
		Option serviceUrlOption = new Option("su", SERVICE_URL, true, "The URI of the OData service");
		serviceUrlOption.setRequired(true);
		options.addOption(serviceUrlOption);

		/** Creates an Option for userName  */
		Option userNameOption = new Option("ur", USERNAME, true, "Username to access OData service");
		options.addOption(userNameOption);

		/** Creates an Option for password  */
		Option passwordOption = new Option("pw", PASSWORD, true, "Password to access OData service");
		options.addOption(passwordOption);

		/** Creates an Option for challangeScheme  */
		Option challangeSchemeOption = new Option("cs", CHALLENGE_SCHEME, true, "ChallengeScheme - Possible values HTTP_BASIC, HTTP_NEGOTIATE, if not provided then no authentication will be used");
		options.addOption(challangeSchemeOption);

		/** Creates an Option for sreviceClassName */
		Option sreviceClassNameOption = new Option("sc", SERVICE_CLASSNAME, true, "Service class name");
		options.addOption(sreviceClassNameOption);

		/** Creates an Option for serviceClassDir */
		Option serviceClassDirOption = new Option("sd", SERVICE_CLASS_DIR, true, "The output directory for Service class generation (For example : src/com/edm/entities)");
		options.addOption(serviceClassDirOption);

		/** Creates an Option for entityClassDir */
		Option entityClassDirOption = new Option("ed", ENTITY_CLASS_DIR, true, "The output directory for Entity class generation (For example : src/com/edm/entities");
		options.addOption(entityClassDirOption);

	}

	/**
	 * Prints the usage.
	 *
	 * @param options the options
	 */
	public static void printUsage(Options options)
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("available options as follow : ", options );
		System.exit(1);
	}

	/**
	 *  To retrieve argument for the Option.
	 *
	 * @param option the option
	 * @param commandLine the command line
	 * @return the option
	 */
	public static String getOption(final String option, final CommandLine commandLine) {

		if (commandLine.hasOption(option)) {
			return commandLine.getOptionValue(option);
		}

		return null;
	}

	/**
	 * Generates the client code to the given output directory.
	 *
	 * @param outputDir            The output directory.
	 * @param serviceDir the service dir
	 * @throws Exception the exception
	 */
	public void generate(File outputDir, File serviceDir) throws Exception {
		Service service = new Service(serviceUrl);
		if(challengeScheme != null && userName != null && password != null){
			service.setCredentials(new ChallengeResponse(challengeScheme, userName,
					password));
		}
		Configuration fmc = new Configuration();
		fmc.setDefaultEncoding(CharacterSet.UTF_8.getName());

		// Generate classes
		String rootTemplates = "clap://class/org/restlet/ext/odata/internal/templates";
		Representation complexTmpl = new StringRepresentation(
				new ClientResource(rootTemplates + "/complexType.ftl").get()
				.getText());
		Representation entityTmpl = new StringRepresentation(
				new ClientResource(rootTemplates + "/entityType.ftl").get()
				.getText());
		Representation serviceTmpl = new StringRepresentation(
				new ClientResource(rootTemplates + "/service.ftl").get()
				.getText());

		Metadata metadata = (Metadata) service.getMetadata();
		for (Schema schema : metadata.getSchemas()) {
			if ((schema.getEntityTypes() != null && !schema.getEntityTypes()
					.isEmpty())
					|| (schema.getComplexTypes() != null && !schema
					.getComplexTypes().isEmpty())) {

				packageDir = outputDir != null ? outputDir : new File(
						TypeUtils.getPackageName(schema));
				packageDir.mkdirs();

				String packageName = outputDir != null ? (entityPkg.replace(
						"/", ".")) : TypeUtils.getPackageName(schema);
				// For each entity type
				for (EntityType type : schema.getEntityTypes()) {
					String className = type.getClassName();
					Map<String, Object> dataModel = new HashMap<String, Object>();
					dataModel.put("type", type);
					dataModel.put("schema", schema);
					dataModel.put("metadata", metadata);
					dataModel.put("className", className);
					dataModel.put("packageName", packageName);

					try {
						TemplateRepresentation templateRepresentation = new TemplateRepresentation(
								entityTmpl, fmc, dataModel,
								MediaType.TEXT_PLAIN);
						templateRepresentation
						.setCharacterSet(CharacterSet.UTF_8);

						// Write the template representation as a Java class
						templateRepresentation.write(new FileOutputStream(
								new File(packageDir, type.getClassName()
										+ ".java")));
					} catch (Exception e) {
						String errormsg = "Exception Occurred in generating entity type for - "
								+ type.getClassName();
						 LOGGER.log(Level.SEVERE,errormsg);
					}

				}

				for (ComplexType type : schema.getComplexTypes()) {
					String className = type.getClassName();
					Map<String, Object> dataModel = new HashMap<String, Object>();
					dataModel.put("type", type);
					dataModel.put("schema", schema);
					dataModel.put("metadata", metadata);
					dataModel.put("className", className);
					dataModel.put("packageName", packageName);

					try {
						TemplateRepresentation templateRepresentation = new TemplateRepresentation(
								complexTmpl, fmc, dataModel,
								MediaType.TEXT_PLAIN);

						templateRepresentation
						.setCharacterSet(CharacterSet.UTF_8);

						// Write the template representation as a Java class
						templateRepresentation.write(new FileOutputStream(
								new File(packageDir, type.getClassName()
										+ ".java")));
					} catch (Exception e) {
						String errormsg = "Exception Occurred in generating complex type for - "
								+ type.getClassName();
						LOGGER.log(Level.SEVERE,errormsg);
					}
				}
			}
		}

		if (metadata.getContainers() != null
				&& !metadata.getContainers().isEmpty()) {
			for (EntityContainer entityContainer : metadata.getContainers()) {
				Schema schema = entityContainer.getSchema();
				// Generate Service subclass
				StringBuffer className = new StringBuffer();
				if (serviceClassName != null) {
					// Try to use the Client preference
					if (entityContainer.isDefaultEntityContainer()) {
						className.append(serviceClassName);
					} else if (metadata.getContainers().size() == 1) {
						className.append(serviceClassName);
					} else {
						className.append(schema.getNamespace()
								.getNormalizedName().substring(0, 1)
								.toUpperCase());
						className.append(schema.getNamespace()
								.getNormalizedName().substring(1));
						className.append("Service");
					}
				} else {
					className.append(schema.getNamespace().getNormalizedName()
							.substring(0, 1).toUpperCase());
					className.append(schema.getNamespace().getNormalizedName()
							.substring(1));
					className.append("Service");
				}

				String packageName = outputDir != null ? (servicePkg.replace(
						"/", ".")) : TypeUtils.getPackageName(schema);
				String entityClassPkg = outputDir != null ? (entityPkg.replace("/", ".")) : TypeUtils
						.getPackageName(schema);
				Map<String, Object> dataModel = new HashMap<String, Object>();
				dataModel.put("schema", schema);
				dataModel.put("metadata", metadata);
				dataModel.put("className", className);
				dataModel.put(CHALLENGE_SCHEME, challengeScheme != null ? "ChallengeScheme."+challengeScheme.getName() : null);
				dataModel.put(USERNAME, userName);
				dataModel.put(PASSWORD, password);
				dataModel.put("dataServiceUri", service.getServiceRef()
						.getTargetRef());
				dataModel.put("entityContainer", entityContainer);
				dataModel.put("servicePkg", servicePkg.replace("/", "."));
				dataModel.put("entityClassPkg", entityClassPkg);
				dataModel.put("packageName", packageName);

				try {
					TemplateRepresentation templateRepresentation = new TemplateRepresentation(
							serviceTmpl, fmc, dataModel, MediaType.TEXT_PLAIN);
					templateRepresentation.setCharacterSet(CharacterSet.UTF_8);

					// Write the template representation as a Java class
					templateRepresentation.write(new FileOutputStream(new File(
							serviceDir, className + ".java")));
				} catch (Exception e) {
					String errormsg = "Exception Occurred in generating Service class for - "
							+ className;
					LOGGER.log(Level.SEVERE,errormsg);
				}
			}
		}
	}

	/**
	 * Generates the client code to the given output directory.
	 *
	 * @param outputDir            The output directory.
	 * @throws Exception the exception
	 */
	public void generate(String outputDir) throws Exception {
		generate(new File(outputDir), new File(outputDir));
	}
}
