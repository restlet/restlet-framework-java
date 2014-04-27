package org.restlet.ext.apispark;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.ext.apispark.internal.info.ApplicationInfo;
import org.restlet.ext.apispark.internal.info.DocumentationInfo;
import org.restlet.ext.apispark.internal.info.MethodInfo;
import org.restlet.ext.apispark.internal.info.ParameterInfo;
import org.restlet.ext.apispark.internal.info.ParameterStyle;
import org.restlet.ext.apispark.internal.info.RepresentationInfo;
import org.restlet.ext.apispark.internal.info.ResourceInfo;
import org.restlet.ext.apispark.internal.info.ResponseInfo;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Directory;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;

/**
 * Publish the documentation of a Restlet base Application to the APISpark
 * console.
 * 
 * @author thboileau
 */
public class Introspector {

	/**
	 * Completes a map of representations with a list of representations.
	 * 
	 * @param mapReps
	 *            The map to complete.
	 * @param representations
	 *            The source list.
	 */
	private static void addRepresentations(
			Map<String, RepresentationInfo> mapReps,
			List<RepresentationInfo> representations) {
		if (representations != null) {
			for (RepresentationInfo r : representations) {
				if (!mapReps.containsKey(r.getIdentifier())) {
					mapReps.put(r.getIdentifier(), r);
				}
			}
		}
	}

	/**
	 * Completes the given {@link Contract} with the list of resources.
	 * 
	 * @param application
	 *            The source application.
	 * @param contract
	 *            The contract to complete.
	 * @param resources
	 *            The list of resources.
	 * @param basePath
	 *            The resources base path.
	 * @param mapReps
	 *            The lndex of representations.
	 */
	private static void addResources(ApplicationInfo application,
			Contract contract, List<ResourceInfo> resources, String basePath,
			Map<String, RepresentationInfo> mapReps) {
		for (ResourceInfo ri : resources) {
			Resource resource = new Resource();
			resource.setDescription(toString(ri.getDocumentations()));
			resource.setName(ri.getIdentifier());
			if (basePath != null) {
				if (basePath.endsWith("/")) {
					if (ri.getPath().startsWith("/")) {
						resource.setResourcePath(basePath
								+ ri.getPath().substring(1));
					} else {
						resource.setResourcePath(basePath + ri.getPath());
					}
				} else {
					if (ri.getPath().startsWith("/")) {
						resource.setResourcePath(basePath + ri.getPath());
					} else {
						resource.setResourcePath(basePath + "/" + ri.getPath());
					}
				}

			} else {
				resource.setResourcePath(ri.getPath());
			}

			if (!ri.getChildResources().isEmpty()) {
				addResources(application, contract, ri.getChildResources(),
						resource.getResourcePath(), mapReps);
			}

			if (ri.getMethods().isEmpty()) {
				continue;
			}

			resource.setPathVariables(new ArrayList<PathVariable>());
			for (ParameterInfo pi : ri.getParameters()) {
				if (ParameterStyle.TEMPLATE.equals(pi.getStyle())) {
					PathVariable pathVariable = new PathVariable();

					pathVariable
							.setDescription(toString(pi.getDocumentations()));
					pathVariable.setName(pi.getName());

					resource.getPathVariables().add(pathVariable);
				}
			}

			resource.setOperations(new ArrayList<Operation>());
			for (MethodInfo mi : ri.getMethods()) {

				Operation operation = new Operation();
				operation.setDescription(toString(mi.getDocumentations()));
				operation.setName(mi.getName().getName());
				// TODO complete Method class with mi.getName()
				operation.setMethod(new org.restlet.ext.apispark.Method());
				operation.getMethod().setDescription(
						mi.getName().getDescription());
				operation.getMethod().setName(mi.getName().getName());

				// Complete parameters
				operation.setHeaders(new ArrayList<Parameter>());
				operation.setQueryParameters(new ArrayList<Parameter>());
				if (mi.getRequest() != null
						&& mi.getRequest().getParameters() != null) {
					for (ParameterInfo pi : mi.getRequest().getParameters()) {
						if (ParameterStyle.HEADER.equals(pi.getStyle())) {
							Parameter parameter = new Parameter();
							parameter.setAllowMultiple(pi.isRepeating());
							parameter.setDefaultValue(pi.getDefaultValue());
							parameter.setDescription(toString(pi
									.getDocumentations()));
							parameter.setName(pi.getName());
							parameter
									.setPossibleValues(new ArrayList<String>());
							parameter.setRequired(pi.isRequired());

							operation.getHeaders().add(parameter);
						} else if (ParameterStyle.QUERY.equals(pi.getStyle())) {
							Parameter parameter = new Parameter();
							parameter.setAllowMultiple(pi.isRepeating());
							parameter.setDefaultValue(pi.getDefaultValue());
							parameter.setDescription(toString(pi
									.getDocumentations()));
							parameter.setName(pi.getName());
							parameter
									.setPossibleValues(new ArrayList<String>());
							parameter.setRequired(pi.isRequired());

							operation.getHeaders().add(parameter);
						}
					}
				}

				if (mi.getRequest() != null
						&& mi.getRequest().getRepresentations() != null
						&& !mi.getRequest().getRepresentations().isEmpty()) {
					addRepresentations(mapReps, mi.getRequest()
							.getRepresentations());

					Body body = new Body();
					// TODO analyze
					// The models differ : one representation / one variant
					// for Restlet one representation / several variants for
					// APIspark
					body.setRepresentation(mi.getRequest().getRepresentations()
							.get(0).getIdentifier());

					operation.setInRepresentation(body);
				}

				if (mi.getResponses() != null && !mi.getResponses().isEmpty()) {
					operation.setResponses(new ArrayList<Response>());

					Body body = new Body();
					// TODO analyze
					// The models differ : one representation / one variant
					// for Restlet one representation / several variants for
					// APIspark
					if (!mi.getResponse().getRepresentations().isEmpty()) {
						body.setRepresentation(mi.getResponse()
								.getRepresentations().get(0).getIdentifier());
					}
					operation.setOutRepresentation(body);

					for (ResponseInfo rio : mi.getResponses()) {
						addRepresentations(mapReps, rio.getRepresentations());

						if (!rio.getStatuses().isEmpty()) {
							Status status = rio.getStatuses().get(0);
							// TODO analyze
							// The models differ : one representation / one
							// variant
							// for Restlet one representation / several
							// variants for
							// APIspark

							Response response = new Response();
							response.setBody(body);
							response.setCode(status.getCode());
							response.setName(toString(rio.getDocumentations()));
							response.setDescription(toString(rio
									.getDocumentations()));
							response.setMessage(status.getDescription());
							// response.setName();

							operation.getResponses().add(response);
						}
					}
				}

				resource.getOperations().add(operation);
			}

			contract.getResources().add(resource);
		}
	}

	/**
	 * Returns a APISpark description of the current application. By default,
	 * this method discovers all the resources attached to this application. It
	 * can be overridden to add documentation, list of representations, etc.
	 * 
	 * @param request
	 *            The current request.
	 * @param response
	 *            The current response.
	 * @return An application description.
	 */
	protected static ApplicationInfo getApplicationInfo(
			Application application, Reference baseRef) {
		ApplicationInfo applicationInfo = new ApplicationInfo();
		if ((application.getName() != null) && !application.getName().isEmpty()) {
			DocumentationInfo doc = null;
			if (applicationInfo.getDocumentations().isEmpty()) {
				doc = new DocumentationInfo();
				applicationInfo.getDocumentations().add(doc);
			} else {
				doc = applicationInfo.getDocumentations().get(0);
			}
			applicationInfo.setName(application.getName());
			doc.setTitle(application.getName());
		}
		applicationInfo.getResources().setBaseRef(baseRef);
		applicationInfo.getResources().setResources(
				getResourceInfos(applicationInfo,
						getNextRouter(application.getInboundRoot())));
		return applicationInfo;
	}

	/**
	 * Returns the next router available.
	 * 
	 * @param current
	 *            The current Restlet to inspect.
	 * @return The first router available.
	 */
	private static Router getNextRouter(Restlet current) {
		Router result = null;
		if (current instanceof Router) {
			result = (Router) current;
		} else if (current instanceof Filter) {
			result = getNextRouter(((Filter) current).getNext());
		}

		return result;
	}

	private static String getParameter(String[] args, int index) {
		if (index >= args.length) {
			return null;
		} else {
			String value = args[index];
			if ("-s".equals(value) || "-u".equals(value) || "-p".equals(value)
					|| "-s".equals(value)) {
				value = null;
			}
			return value;
		}
	}

	/**
	 * Completes the data available about a given Filter instance.
	 * 
	 * @param applicationInfo
	 *            The parent application.
	 * @param filter
	 *            The Filter instance to document.
	 * @param path
	 *            The base path.
	 * @param request
	 *            The current request.
	 * @param response
	 *            The current response.
	 * @return The resource description.
	 */
	private static ResourceInfo getResourceInfo(
			ApplicationInfo applicationInfo, Filter filter, String path) {
		return getResourceInfo(applicationInfo, filter.getNext(), path);
	}

	/**
	 * Completes the data available about a given Finder instance.
	 * 
	 * @param applicationInfo
	 *            The parent application.
	 * @param resourceInfo
	 *            The ResourceInfo object to complete.
	 * @param finder
	 *            The Finder instance to document.
	 */
	private static ResourceInfo getResourceInfo(
			ApplicationInfo applicationInfo, Finder finder, String path) {
		ResourceInfo result = null;
		Object resource = null;

		if (finder instanceof Directory) {
			resource = finder;
		} else {
			ServerResource sr = finder.find(null, null);

			if (sr != null) {
				// The handler instance targeted by this finder.
				Request request = new Request();
				org.restlet.Response response = new org.restlet.Response(
						request);
				sr.setRequest(request);
				sr.setResponse(response);
				sr.updateAllowedMethods();
				resource = sr;
			}
		}

		if (resource != null) {
			result = new ResourceInfo();
			ResourceInfo.describe(applicationInfo, result, resource, path);
		}

		return result;
	}

	/**
	 * Completes the data available about a given Restlet instance.
	 * 
	 * @param applicationInfo
	 *            The parent application.
	 * @param resourceInfo
	 *            The ResourceInfo object to complete.
	 * @param restlet
	 *            The Restlet instance to document.
	 */
	private static ResourceInfo getResourceInfo(
			ApplicationInfo applicationInfo, Restlet restlet, String path) {
		ResourceInfo result = null;

		if (restlet instanceof Finder) {
			result = getResourceInfo(applicationInfo, (Finder) restlet, path);
		} else if (restlet instanceof Router) {
			result = new ResourceInfo();
			result.setPath(path);
			result.setChildResources(getResourceInfos(applicationInfo,
					(Router) restlet));
		} else if (restlet instanceof Filter) {
			result = getResourceInfo(applicationInfo, (Filter) restlet, path);
		}

		return result;
	}

	/**
	 * Returns the APISpark data about the given Route instance.
	 * 
	 * @param applicationInfo
	 *            The parent application.
	 * @param route
	 *            The Route instance to document.
	 * @param basePath
	 *            The base path.
	 * @return The APISpark data about the given Route instance.
	 */
	private static ResourceInfo getResourceInfo(
			ApplicationInfo applicationInfo, Route route, String basePath) {
		ResourceInfo result = null;

		if (route instanceof TemplateRoute) {
			TemplateRoute templateRoute = (TemplateRoute) route;
			String path = templateRoute.getTemplate().getPattern();

			// APISpark requires resource paths to be relative to parent path
			if (path.startsWith("/") && basePath.endsWith("/")) {
				path = path.substring(1);
			}

			result = getResourceInfo(applicationInfo, route.getNext(), path);
		}

		return result;
	}

	/**
	 * Completes the list of ResourceInfo instances for the given Router
	 * instance.
	 * 
	 * @param applicationInfo
	 *            The parent application.
	 * @param router
	 *            The router to document.
	 * @return The list of ResourceInfo instances to complete.
	 */
	private static List<ResourceInfo> getResourceInfos(
			ApplicationInfo applicationInfo, Router router) {
		List<ResourceInfo> result = new ArrayList<ResourceInfo>();

		for (Route route : router.getRoutes()) {
			ResourceInfo resourceInfo = getResourceInfo(applicationInfo, route,
					"/");

			if (resourceInfo != null) {
				result.add(resourceInfo);
			}
		}

		if (router.getDefaultRoute() != null) {
			ResourceInfo resourceInfo = getResourceInfo(applicationInfo,
					router.getDefaultRoute(), "/");
			if (resourceInfo != null) {
				result.add(resourceInfo);
			}
		}

		return result;
	}

	/**
	 * Indicates if the given velue is either null or empty.
	 * 
	 * @param value
	 *            The value.
	 * @return True if the value is either null or empty.
	 */
	private static boolean isEmpty(String value) {
		return value == null || value.isEmpty();
	}

	/**
	 * Main class, invoke this class withour argument to get help instructions.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String ulogin = null;
		String upwd = null;
		String serviceUrl = null;
		String application = null;
		for (int i = 0; i < (args.length); i++) {
			if ("-h".equals(args[i])) {
				printHelp();
				System.exit(0);
			} else if ("-u".equals(args[i])) {
				ulogin = getParameter(args, ++i);
			} else if ("-p".equals(args[i])) {
				upwd = getParameter(args, ++i);
			} else if ("-s".equals(args[i])) {
				serviceUrl = getParameter(args, ++i);
			} else {
				application = args[i];
			}
		}

		if (isEmpty(serviceUrl)) {
			serviceUrl = "https://apispark.com/definitions";
		}

		if (isEmpty(ulogin) || isEmpty(upwd) || isEmpty(application)) {
			printHelp();
			System.exit(1);
		}

		// Validate the application class name
		Class<?> clazz = null;
		try {
			clazz = Class.forName(application);
			if (Application.class.isAssignableFrom(clazz)) {
				Application app = (Application) clazz.getConstructor()
						.newInstance();
				Introspector i = new Introspector(app);
				ClientResource cr = new ClientResource(serviceUrl);
				cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, ulogin,
						upwd);
				cr.post(i.getDefinition(), MediaType.APPLICATION_JSON);
				System.out.println(cr.getLocationRef());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (clazz == null) {
			System.out
					.println("Please provide a valid application class name.");
		}
	}

	/**
	 * Prints the instructions necessary to launch this tool.
	 */
	private static void printHelp() {
		PrintStream o = System.out;

		o.println("SYNOPSIS");
		printSynopsis(o, Introspector.class, "[options] APPLICATION");
		o.println("DESCRIPTION");
		printSentence(
				o,
				"Publish to the APISpark platform the description of your Web API, represented by APPLICATION, the full canonical name of your Restlet application class.");
		printSentence(
				o,
				"If the whole process is successfull, it displays the url of the corresponding documentation.");
		o.println("OPTIONS");
		printOption(o, "-h", "Prints this help");
		printOption(o, "-u", "The mandatory APISpark user login");
		printOption(o, "-p", "The mandatory APISpark user security token");
		printOption(o, "-s",
				"The optional APISpark platform URL (by default https://apispark.com)");
	}

	/**
	 * Displays an option and its description to the console.
	 * 
	 * @param o
	 *            The console stream.
	 * @param option
	 *            The option.
	 * @param strings
	 *            The option's description.
	 */
	private static void printOption(PrintStream o, String option,
			String... strings) {
		printSentence(o, 7, option);
		printSentence(o, 14, strings);
	}

	/**
	 * Formats a list of Strings by lines of 80 characters maximul, and displays
	 * it to the console.
	 * 
	 * @param o
	 *            The console.
	 * @param shift
	 *            The number of characters to shift the list of strings on the
	 *            left.
	 * @param strings
	 *            The list of Strings to display.
	 */
	private static void printSentence(PrintStream o, int shift,
			String... strings) {
		int blockLength = 80 - shift - 1;
		String tab = "";
		for (int i = 0; i < shift; i++) {
			tab = tab.concat(" ");
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < strings.length; i++) {
			if (i > 0) {
				sb.append(" ");
			}
			sb.append(strings[i]);
		}
		String sentence = sb.toString();
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
				length = sentence.substring(index, length).lastIndexOf(' ');
				o.println(sentence.substring(index, index + length));
				index += length + 1;
			}
		}
	}

	/**
	 * Displays a list of String to the console.
	 * 
	 * @param o
	 *            The console stream.
	 * @param strings
	 *            The list of Strings to display.
	 */
	private static void printSentence(PrintStream o, String... strings) {
		printSentence(o, 7, strings);
	}

	/**
	 * Displays the command line.
	 * 
	 * @param o
	 *            The console stream.
	 * @param clazz
	 *            The main class.
	 * @param command
	 *            The command line.
	 */
	private static void printSynopsis(PrintStream o, Class<?> clazz,
			String command) {
		printSentence(o, 7, clazz.getCanonicalName(), command);
	}

	/**
	 * Converts a ApplicationInfo to a {@link Definition} object.
	 * 
	 * @param application
	 *            The {@link ApplicationInfo} instance.
	 * @return The definintion instance.
	 */
	private static Definition toDefinition(ApplicationInfo application) {
		Definition result = null;
		if (application != null) {
			result = new Definition();
			result.setVersion(application.getVersion());
			if (application.getResources().getBaseRef() != null) {
				result.setEndpoint(application.getResources().getBaseRef()
						.toString());
			}

			Contract contract = new Contract();
			result.setContract(contract);
			contract.setDescription(toString(application.getDocumentations()));
			contract.setName(application.getName());
			if (contract.getName() == null || contract.getName().isEmpty()) {
				contract.setName(application.getClass().getName());
				Context.getCurrentLogger().log(
						Level.WARNING,
						"Please provide a name to your application, used "
								+ contract.getName() + " by default.");
			}

			// List of resources.
			contract.setResources(new ArrayList<Resource>());
			Map<String, RepresentationInfo> mapReps = new HashMap<String, RepresentationInfo>();
			addResources(application, contract, application.getResources()
					.getResources(), result.getEndpoint(), mapReps);

			java.util.List<String> protocols = new ArrayList<String>();
			for (ConnectorHelper<Server> helper : Engine.getInstance()
					.getRegisteredServers()) {
				for (Protocol protocol : helper.getProtocols()) {
					if (!protocols.contains(protocol.getName())) {
						protocols.add(protocol.getName());
					}
				}
			}

			// List of representations.
			contract.setRepresentations(new ArrayList<Representation>());
			for (RepresentationInfo ri : application.getRepresentations()) {
				if (!mapReps.containsKey(ri.getIdentifier())) {
					mapReps.put(ri.getIdentifier(), ri);
				}
			}

			for (RepresentationInfo ri : mapReps.values()) {
				Representation rep = new Representation();

				// TODO analyze
				// The models differ : one representation / one variant for
				// Restlet
				// one representation / several variants for APIspark
				rep.setDescription(toString(ri.getDocumentations()));
				rep.setName(ri.getIdentifier());
				Variant variant = new Variant();
				variant.setDataType(ri.getMediaType().getName());
				rep.setVariants(new ArrayList<Variant>());
				rep.getVariants().add(variant);

				rep.setProperties(ri.getProperties());
				rep.setRaw(ri.isRaw());
				contract.getRepresentations().add(rep);
			}

		}
		return result;
	}

	/**
	 * Concats a list of {@link DocumentationInfo} instances as a single String.
	 * 
	 * @param dis
	 *            The list of {@link DocumentationInfo} instances.
	 * @return A String value.
	 */
	private static String toString(List<DocumentationInfo> dis) {
		StringBuilder d = new StringBuilder();
		for (DocumentationInfo doc : dis) {
			d.append(doc.getTextContent());
		}
		return d.toString();
	}

	/** The current Web API definition. */
	private Definition definition;

	/**
	 * Constructor.
	 * 
	 * @param application
	 *            An application to introspect.
	 */
	public Introspector(Application application) {
		definition = toDefinition(getApplicationInfo(application, null));
	}

	/**
	 * Returns the current definition.
	 * 
	 * @return The current definition.
	 */
	public Definition getDefinition() {
		return definition;
	}

	/**
	 * Sets the current definition.
	 * 
	 * @param definition
	 *            The current definition.
	 */
	public void setDefinition(Definition definition) {
		this.definition = definition;
	}
}
