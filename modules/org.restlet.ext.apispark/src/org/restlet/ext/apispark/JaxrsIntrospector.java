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

import java.io.IOException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.Encoded;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

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
import org.restlet.ext.apispark.internal.info.PropertyInfo;
import org.restlet.ext.apispark.internal.info.RepresentationInfo;
import org.restlet.ext.apispark.internal.info.RequestInfo;
import org.restlet.ext.apispark.internal.info.ResourceInfo;
import org.restlet.ext.apispark.internal.info.ResponseInfo;
import org.restlet.ext.apispark.internal.model.Body;
import org.restlet.ext.apispark.internal.model.Contract;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Header;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;
import org.restlet.ext.apispark.internal.reflect.ReflectUtils;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Directory;
import org.restlet.resource.Finder;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;

/**
 * Publish the documentation of a Restlet-based Application to the APISpark
 * console.
 * 
 * @author Thierry Boileau
 */
public class JaxrsIntrospector {

	/** Internal logger. */
	protected static Logger LOGGER = Context.getCurrentLogger();

	private static void addRepresentation(MethodInfo method, FormParam formparam) {
		if (formparam != null) {
			// gives an indication of the expected entity
			RepresentationInfo ri = null;
			// Gives an indication on the kind of representation handled
			for (RepresentationInfo r : method.getRequest()
					.getRepresentations()) {
				if (r.getMediaType().equals(MediaType.APPLICATION_WWW_FORM)) {
					ri = r;
					break;
				}
			}
			if (ri == null) {
				// TODO identify using the method's name, and the resource
				// path
				ri = new RepresentationInfo();
				ri.setIdentifier(method.getMethod().getName());
				ri.setName(method.getMethod().getName());
				ri.setMediaType(MediaType.APPLICATION_WWW_FORM);
				method.getRequest().getRepresentations().add(ri);
			}
			ParameterInfo pi = new ParameterInfo(formparam.value(),
					ParameterStyle.PLAIN, "body parameter: "
							+ formparam.value());
			method.getParameters().add(pi);
		}
	}

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
			if (ri.getPath() != null) {
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
							resource.setResourcePath(basePath + "/"
									+ ri.getPath());
						}
					}
				} else {
					if (ri.getPath().startsWith("/")) {
						resource.setResourcePath(ri.getPath());
					} else {
						resource.setResourcePath("/" + ri.getPath());
					}
				}
			}

			if (!ri.getChildResources().isEmpty()) {
				addResources(application, contract, ri.getChildResources(),
						resource.getResourcePath(), mapReps);
			}
			LOGGER.info("Resource " + ri.getPath() + " added.");

			if (ri.getMethods().isEmpty()) {
				LOGGER.warning("Resource " + ri.getIdentifier()
						+ " has no methods.");
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
				LOGGER.info("Method " + mi.getMethod().getName() + " added.");
				Operation operation = new Operation();
				operation.setDescription(toString(mi.getDocumentations()));
				operation.setName(mi.getMethod().getName());
				// TODO complete Method class with mi.getName()
				operation.setMethod(mi.getMethod().getName());

				// Fill fields produces/consumes
				String mediaType = null;
				if (mi.getRequest() != null
						&& mi.getRequest().getRepresentations() != null) {
					List<RepresentationInfo> consumed = mi.getRequest()
							.getRepresentations();
					for (RepresentationInfo reprInfo : consumed) {
						mediaType = reprInfo.getMediaType().getName();
						operation.getConsumes().add(mediaType);
					}
				}

				if (mi.getResponse() != null
						&& mi.getResponse().getRepresentations() != null) {
					List<RepresentationInfo> produced = mi.getResponse()
							.getRepresentations();
					for (RepresentationInfo reprInfo : produced) {
						mediaType = reprInfo.getMediaType().getName();
						operation.getProduces().add(mediaType);
					}
				}

				// Complete parameters
				operation.setHeaders(new ArrayList<Header>());
				operation.setQueryParameters(new ArrayList<QueryParameter>());
				if (mi.getRequest() != null) {
					for (ParameterInfo pi : mi.getRequest().getParameters()) {
						if (ParameterStyle.HEADER.equals(pi.getStyle())) {
							Header header = new Header();
							header.setAllowMultiple(pi.isRepeating());
							header.setDefaultValue(pi.getDefaultValue());
							header.setDescription(toString(
									pi.getDocumentations(),
									pi.getDefaultValue()));
							header.setName(pi.getName());
							header.setPossibleValues(new ArrayList<String>());
							header.setRequired(pi.isRequired());

							operation.getHeaders().add(header);
						} else if (ParameterStyle.QUERY.equals(pi.getStyle())) {
							QueryParameter queryParameter = new QueryParameter();
							queryParameter.setAllowMultiple(pi.isRepeating());
							queryParameter
									.setDefaultValue(pi.getDefaultValue());
							queryParameter.setDescription(toString(
									pi.getDocumentations(),
									pi.getDefaultValue()));
							queryParameter.setName(pi.getName());
							queryParameter
									.setPossibleValues(new ArrayList<String>());
							queryParameter.setRequired(pi.isRequired());

							operation.getQueryParameters().add(queryParameter);
						}
					}
				}
				for (ParameterInfo pi : mi.getParameters()) {
					if (ParameterStyle.HEADER.equals(pi.getStyle())) {
						Header header = new Header();
						header.setAllowMultiple(pi.isRepeating());
						header.setDefaultValue(pi.getDefaultValue());
						header.setDescription(toString(pi.getDocumentations(),
								pi.getDefaultValue()));
						header.setName(pi.getName());
						header.setPossibleValues(new ArrayList<String>());
						header.setRequired(pi.isRequired());

						operation.getHeaders().add(header);
					} else if (ParameterStyle.QUERY.equals(pi.getStyle())) {
						QueryParameter queryParameter = new QueryParameter();
						queryParameter.setAllowMultiple(pi.isRepeating());
						queryParameter.setDefaultValue(pi.getDefaultValue());
						queryParameter.setDescription(toString(
								pi.getDocumentations(), pi.getDefaultValue()));
						queryParameter.setName(pi.getName());
						queryParameter
								.setPossibleValues(new ArrayList<String>());
						queryParameter.setRequired(pi.isRequired());

						operation.getQueryParameters().add(queryParameter);
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
							.get(0).getName());

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
								.getRepresentations().get(0).getName());
					}
					operation.setOutRepresentation(body);

					for (ResponseInfo rio : mi.getResponses()) {
						addRepresentations(mapReps, rio.getRepresentations());

						if (!rio.getStatuses().isEmpty()) {
							Status status = rio.getStatuses().get(0);
							// TODO analyze
							// The models differ : one representation / one
							// variant
							// for Restlet one representation / several variants
							// for APIspark
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
	 * Returns an instance of what must be a subclass of {@link Application}.
	 * Returns null in case of errors.
	 * 
	 * @param className
	 *            The name of the application class.
	 * @return An instance of what must be a subclass of {@link Application}.
	 */
	private static javax.ws.rs.core.Application getApplication(String className) {
		javax.ws.rs.core.Application result = null;

		if (className == null) {
			return result;
		}

		Class<?> clazz = null;
		try {
			clazz = Class.forName(className);
			if (javax.ws.rs.core.Application.class.isAssignableFrom(clazz)) {
				result = (javax.ws.rs.core.Application) clazz.getConstructor()
						.newInstance();
			} else {
				LOGGER.log(Level.SEVERE, className
						+ " does not seem to a valid subclass of "
						+ Application.class.getName() + " class.");
			}
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.SEVERE, "Cannot locate the application class.", e);
		} catch (InstantiationException e) {
			LOGGER.log(Level.SEVERE,
					"Cannot instantiate the application class.", e);
		} catch (IllegalAccessException e) {
			LOGGER.log(Level.SEVERE,
					"Cannot instantiate the application class.", e);
		} catch (IllegalArgumentException e) {
			LOGGER.log(
					Level.SEVERE,
					"Check that the application class has an empty constructor.",
					e);
		} catch (InvocationTargetException e) {
			LOGGER.log(Level.SEVERE,
					"Cannot instantiate the application class.", e);
		} catch (NoSuchMethodException e) {
			LOGGER.log(
					Level.SEVERE,
					"Check that the application class has an empty constructor.",
					e);
		} catch (SecurityException e) {
			LOGGER.log(Level.SEVERE,
					"Cannot instantiate the application class.", e);
		}

		return result;
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
			javax.ws.rs.core.Application application, Reference baseRef) {
		ApplicationInfo applicationInfo = new ApplicationInfo();

		for (Class<?> clazz : application.getClasses()) {
			scan(clazz, applicationInfo, baseRef);
		}
		for (Object singleton : application.getSingletons()) {
			if (singleton != null) {
				scan(singleton.getClass(), applicationInfo, baseRef);
			}
		}
		applicationInfo.getResources().setBaseRef(baseRef);

		return applicationInfo;
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

	private static String getPath(Path rootPath, Path relativePath) {
		return getPath(((rootPath != null) ? rootPath.value() : null),
				((relativePath != null) ? relativePath.value() : null));
	}

	private static String getPath(String rootPath, String relativePath) {
		String result = null;

		if (rootPath == null) {
			rootPath = "/";
		} else if (!rootPath.startsWith("/")) {
			rootPath += "/" + rootPath;
		}

		if (rootPath.endsWith("/")) {
			if (relativePath.startsWith("/")) {
				result = rootPath + relativePath.substring(1);
			} else {
				result = rootPath + relativePath;
			}
		} else {
			if (relativePath.startsWith("/")) {
				result = rootPath + relativePath;
			} else {
				result = rootPath + "/" + relativePath;
			}
		}

		return result;
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

		if (router != null) {
			for (Route route : router.getRoutes()) {
				ResourceInfo resourceInfo = getResourceInfo(applicationInfo,
						route, "/");

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
	 * Main class, invoke this class without argument to get help instructions.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String ulogin = null;
		String upwd = null;
		String serviceUrl = null;
		String appName = null;
		String definitionId = null;

		LOGGER.fine("Get parameters");
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
			} else if ("-d".equals(args[i])) {
				definitionId = getParameter(args, ++i);
			} else {
				appName = args[i];
			}
		}

		LOGGER.fine("Check parameters");
		if (isEmpty(serviceUrl)) {
			serviceUrl = "https://apispark.com/";
		}
		if (!serviceUrl.endsWith("/")) {
			serviceUrl += "/";
		}

		if (isEmpty(ulogin) || isEmpty(upwd) || isEmpty(appName)) {
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
		javax.ws.rs.core.Application application = getApplication(appName);

		if (application != null) {
			LOGGER.fine("Instantiate introspector");
			JaxrsIntrospector i = new JaxrsIntrospector(application);

			try {
				ClientResource cr = new ClientResource(serviceUrl
						+ "definitions");
				cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, ulogin,
						upwd);
				LOGGER.fine("Generate documentation");
				Definition definition = i.getDefinition();
				JacksonRepresentation<Definition> jr = new JacksonRepresentation<Definition>(
						definition);
				try {
					jr.write(System.out);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if (definitionId == null) {
					LOGGER.fine("Create a new documentation");
					cr.post(definition, MediaType.APPLICATION_JSON);
				} else {
					cr.addSegment(definitionId);
					LOGGER.fine("Update the documentation of "
							+ cr.getReference().toString());
					cr.put(definition, MediaType.APPLICATION_JSON);
				}

				LOGGER.fine("Display result");
				System.out.println("Process successfully achieved.");
				// This is not printed by a logger which may be muted.
				if (cr.getResponseEntity() != null
						&& cr.getResponseEntity().isAvailable()) {
					try {
						cr.getResponseEntity().write(System.out);
						System.out.println();
					} catch (IOException e) {
						// [PENDING] analysis
						LOGGER.warning("Request successfully achieved by the server, but it's response cannot be printed");
					}
				}
				if (cr.getLocationRef() != null) {
					System.out
							.println("Your Web API documentation is accessible at this URL: "
									+ cr.getLocationRef());
				}
			} catch (ResourceException e) {
				// TODO Should we detail by status?
				if (e.getStatus().isConnectorError()) {
					LOGGER.severe("Cannot reach the remote service, could you check your network connection?");
					LOGGER.severe("Could you check that the following service is up? "
							+ serviceUrl);
				} else if (e.getStatus().isClientError()) {
					LOGGER.severe("Check that you provide valid credentials, or valid service url.");
				} else if (e.getStatus().isServerError()) {
					LOGGER.severe("The server side encounters some issues, please try later.");
				}
			}
		} else {
			LOGGER.severe("Please provide a valid application class name.");
		}
	}

	/**
	 * Prints the instructions necessary to launch this tool.
	 */
	private static void printHelp() {
		PrintStream o = System.out;

		o.println("SYNOPSIS");
		printSynopsis(o, JaxrsIntrospector.class, "[options] APPLICATION");
		o.println("DESCRIPTION");
		printSentence(
				o,
				"Publish to the APISpark platform the description of your Web API, represented by APPLICATION,",
				"the full name of your Restlet application class.");
		printSentence(
				o,
				"If the whole process is successfull, it displays the url of the corresponding documentation.");
		o.println("OPTIONS");
		printOption(o, "-h", "Prints this help.");
		printOption(o, "-u", "The mandatory APISpark user name.");
		printOption(o, "-p", "The mandatory APISpark user secret key.");
		printOption(o, "-s",
				"The optional APISpark platform URL (by default https://apispark.com).");
		printOption(o, "-c",
				"The optional full name of your Restlet Component class.",
				"This allows to collect some other data, such as the endpoint.");
		printOption(
				o,
				"-d",
				"The optional identifier of an existing definition hosted by APISpark you want to update with this new documentation.");
		o.println("LOGGING");
		printSentence(
				o,
				"You can get a detailled log of the process using the JDK's API.",
				"See the official documentation: http://docs.oracle.com/javase/7/docs/technotes/guides/logging/overview.html",
				"Here is the name of the used Logger: "
						+ JaxrsIntrospector.class.getName());
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
		printSentence(o, 7, clazz.getName(), command);
	}

	private static void scan(Annotation[] annotations, Class<?> parameterClass,
			Type parameterType, ApplicationInfo info, ResourceInfo resource,
			MethodInfo method, Consumes consumes) {
		// Indicates that this parameter is instantiated from annotation
		boolean valueComputed = false;
		// TODO sounds like there are several level of parameters, be careful

		// Introduced by Jax-rs 2.0
		// BeanParam
		for (Annotation annotation : annotations) {
			if (annotation instanceof CookieParam) {
				valueComputed = true;
				CookieParam cookieparam = (CookieParam) annotation;
				if (cookieparam != null) {
					ParameterInfo pi = new ParameterInfo(cookieparam.value(),
							ParameterStyle.COOKIE, "Cookie parameter: "
									+ cookieparam.value());
					method.getRequest().getParameters().add(pi);
				}
			} else if (annotation instanceof DefaultValue) {
				DefaultValue defaultvalue = (DefaultValue) annotation;
				System.out.println("param: " + parameterClass.getName());
				if (defaultvalue != null) {
					System.out.println("defaultvalue " + defaultvalue.value());
				}
			} else if (annotation instanceof Encoded) {
				// ? valueComputed = true;
				Encoded encoded = (Encoded) annotation;
				// TODO what "encoded" is designed for?
			} else if (annotation instanceof FormParam) {
				valueComputed = true;
				FormParam formparam = (FormParam) annotation;
				addRepresentation(method, formparam);
			} else if (annotation instanceof HeaderParam) {
				valueComputed = true;
				HeaderParam headerparam = (HeaderParam) annotation;
				if (headerparam != null) {
					ParameterInfo pi = new ParameterInfo(headerparam.value(),
							ParameterStyle.HEADER, "header parameter: "
									+ headerparam.value());
					method.getParameters().add(pi);
				}
			} else if (annotation instanceof MatrixParam) {
				valueComputed = true;
				MatrixParam matrixparam = (MatrixParam) annotation;
				if (matrixparam != null) {
					ParameterInfo pi = new ParameterInfo(matrixparam.value(),
							ParameterStyle.MATRIX, "matrix parameter: "
									+ matrixparam.value());
					method.getParameters().add(pi);
				}
			} else if (annotation instanceof PathParam) {
				valueComputed = true;
				PathParam pathparam = (PathParam) annotation;
				if (pathparam != null) {
					boolean found = false;
					for (ParameterInfo pi : resource.getParameters()) {
						if (pi.getName().equals(pathparam.value())) {
							found = true;
							break;
						}
					}
					if (!found) {
						ParameterInfo pi = new ParameterInfo(pathparam.value(),
								ParameterStyle.TEMPLATE, "Path parameter: "
										+ pathparam.value());
						resource.getParameters().add(pi);
					}
				}
			} else if (annotation instanceof QueryParam) {
				valueComputed = true;
				QueryParam queryparam = (QueryParam) annotation;
				if (queryparam != null) {
					ParameterInfo pi = new ParameterInfo(queryparam.value(),
							ParameterStyle.QUERY, "Query parameter: "
									+ queryparam.value());
					method.getParameters().add(pi);
				}
			} else if (annotation instanceof javax.ws.rs.core.Context) {
				valueComputed = true;
				javax.ws.rs.core.Context context = (javax.ws.rs.core.Context) annotation;
				System.out.println("context: " + context);
			}
		}

		if (!valueComputed) {
			// We make the assumption this represents the body...
			if (parameterClass != null && !Void.class.equals(parameterClass)) {
				String[] mediaTypes = null;
				if (consumes == null || consumes.value() == null
						|| consumes.value().length == 0) {
					// We assume this can't really happen...
					// Perhaps, we should rely on Produces annotations?
					mediaTypes = new String[1];
					mediaTypes[0] = MediaType.APPLICATION_ALL.getName();
				} else {
					mediaTypes = consumes.value();
				}
				for (String consume : mediaTypes) {
					Variant variant = new Variant(MediaType.valueOf(consume));
					RepresentationInfo representationInfo = null;

					representationInfo = RepresentationInfo.describe(method,
							parameterClass, parameterType, variant);
					if (method.getRequest() == null) {
						method.setRequest(new RequestInfo());
					}
					method.getRequest().getRepresentations()
							.add(representationInfo);
				}
			}
		}
	}

	private static void scan(Class<?> clazz, ApplicationInfo info,
			Reference baseRef) {
		info.getResources().setBaseRef(baseRef);

		// List of common annotations, defined at the level of the class, or at
		// the level of the fields.
		List<CookieParam> cookieparamList = new ArrayList<CookieParam>();
		List<FormParam> formparamList = new ArrayList<FormParam>();
		List<HeaderParam> headerparamList = new ArrayList<HeaderParam>();
		List<MatrixParam> matrixparamList = new ArrayList<MatrixParam>();
		List<PathParam> pathparamList = new ArrayList<PathParam>();
		List<QueryParam> queryparamList = new ArrayList<QueryParam>();
		List<javax.ws.rs.core.Context> contextList = new ArrayList<javax.ws.rs.core.Context>();

		// Introduced by Jax-rs 2.0
		// ConstrainedTo ct = clazz.getAnnotation(ConstrainedTo.class);
		// value = RuntimeType.SERVER

		Consumes c = clazz.getAnnotation(Consumes.class);
		Encoded e = clazz.getAnnotation(Encoded.class);
		System.out.println("encoded " + e);
		Path path = clazz.getAnnotation(Path.class);
		Produces p = clazz.getAnnotation(Produces.class);

		// TODO list all inherited fields
		Field[] fields = clazz.getDeclaredFields();
		if (fields != null) {
			for (Field field : fields) {
				// Apply the values gathered at fields level at the method
				// level.
				scan(field, cookieparamList, formparamList, headerparamList,
						matrixparamList, pathparamList, queryparamList,
						contextList);
			}
		}

		Method[] methods = clazz.getDeclaredMethods();
		for (Method method : methods) {
			scan(method, info, path, c, p, cookieparamList, formparamList,
					headerparamList, matrixparamList, pathparamList,
					queryparamList, contextList);
		}
	}

	private static void scan(Field field, List<CookieParam> cookieparamList,
			List<FormParam> formparamList, List<HeaderParam> headerparamList,
			List<MatrixParam> matrixparamList, List<PathParam> pathparamList,
			List<QueryParam> queryparamList,
			List<javax.ws.rs.core.Context> contextList) {
		// Introduced by Jax-rs 2.0
		// BeanParam beanparam = field.getAnnotation(BeanParam.class);
		CookieParam cookieparam = field.getAnnotation(CookieParam.class);
		if (cookieparam != null) {
			cookieparamList.add(cookieparam);
		}
		DefaultValue defaultvalue = field.getAnnotation(DefaultValue.class);
		if (defaultvalue != null) {
			// System.out.println("defaultvalue " + defaultvalue.value());
		}

		Encoded encoded = field.getAnnotation(Encoded.class);
		// System.out.println("encoded " + encoded);

		FormParam formparam = field.getAnnotation(FormParam.class);
		if (formparam != null) {
			System.out.println("formparam " + formparam.value());
			formparamList.add(formparam);
		}

		HeaderParam headerparam = field.getAnnotation(HeaderParam.class);
		if (headerparam != null) {
			headerparamList.add(headerparam);
		}

		MatrixParam matrixparam = field.getAnnotation(MatrixParam.class);
		if (matrixparam != null) {
			matrixparamList.add(matrixparam);
		}
		PathParam pathparam = field.getAnnotation(PathParam.class);
		if (pathparam != null) {
			pathparamList.add(pathparam);
		}
		QueryParam queryparam = field.getAnnotation(QueryParam.class);
		if (queryparam != null) {
			queryparamList.add(queryparam);
		}

		javax.ws.rs.core.Context context = field
				.getAnnotation(javax.ws.rs.core.Context.class);
		System.out.println("context " + context);
	}

	private static void scan(Method method, ApplicationInfo info, Path cPath,
			Consumes cConsumes, Produces cProduces,
			List<CookieParam> cookieparamList, List<FormParam> formparamList,
			List<HeaderParam> headerparamList,
			List<MatrixParam> matrixparamList, List<PathParam> pathparamList,
			List<QueryParam> queryparamList,
			List<javax.ws.rs.core.Context> contextList) {
		MethodInfo mi = new MethodInfo();
		// TODO set documentation?

		// TODO enhance
		for (FormParam formparam : formparamList) {
			addRepresentation(mi, formparam);
		}

		// "Path" decides on which resource to put this method
		Path path = method.getAnnotation(Path.class);
		String fullPath = getPath(cPath, path);

		ResourceInfo resource = null;
		for (ResourceInfo ri : info.getResources().getResources()) {
			if (fullPath.equals(ri.getPath())) {
				resource = ri;
				break;
			}
		}
		if (resource == null) {
			resource = new ResourceInfo();
			// TODO how to set the identifier?
			resource.setIdentifier(fullPath);
			resource.setPath(fullPath);
			info.getResources().getResources().add(resource);
		}
		resource.getMethods().add(mi);

		PathParam pathparam = method.getAnnotation(PathParam.class);
		if (pathparam != null) {
			pathparamList.add(pathparam);
			ParameterInfo pi = new ParameterInfo(pathparam.value(),
					ParameterStyle.TEMPLATE, "Path parameter: "
							+ pathparam.value());
			pi.setRequired(true);
			resource.getParameters().add(pi);
		} else {
			// let's check that parameters are rightly specified
			Template template = new Template(fullPath);
			for (String var : template.getVariableNames()) {
				boolean found = false;
				for (ParameterInfo pi : resource.getParameters()) {
					if (pi.getStyle().equals(ParameterStyle.TEMPLATE)
							&& var.equals(pi.getName())) {
						found = true;
						break;
					}
				}
				if (!found) {
					ParameterInfo pi = new ParameterInfo(var,
							ParameterStyle.TEMPLATE, "Path parameter: " + var);
					pi.setRequired(true);
					resource.getParameters().add(pi);
				}
			}
		}

		// Introduced by Jax-rs 2.0
		// BeanParam beanparam = method.getAnnotation(BeanParam.class);

		CookieParam cookieparam = method.getAnnotation(CookieParam.class);
		if (cookieparam != null) {
			ParameterInfo pi = new ParameterInfo(cookieparam.value(),
					ParameterStyle.COOKIE, "Cookie parameter: "
							+ cookieparam.value());
			mi.getParameters().add(pi);
		}
		// TODO what encoded is designed for?
		Encoded encoded = method.getAnnotation(Encoded.class);

		FormParam formparam = method.getAnnotation(FormParam.class);
		addRepresentation(mi, formparam);

		HeaderParam headerparam = method.getAnnotation(HeaderParam.class);
		if (headerparam != null) {
			ParameterInfo pi = new ParameterInfo(headerparam.value(),
					ParameterStyle.HEADER, "Header parameter: "
							+ cookieparam.value());
			mi.getParameters().add(pi);
		}
		MatrixParam matrixparam = method.getAnnotation(MatrixParam.class);
		if (matrixparam != null) {
			ParameterInfo pi = new ParameterInfo(matrixparam.value(),
					ParameterStyle.MATRIX, "Matrix parameter: "
							+ cookieparam.value());
			mi.getParameters().add(pi);
		}
		QueryParam queryparam = method.getAnnotation(QueryParam.class);
		if (queryparam != null) {
			ParameterInfo pi = new ParameterInfo(queryparam.value(),
					ParameterStyle.QUERY, "Query parameter: "
							+ cookieparam.value());
			mi.getParameters().add(pi);
		}

		DefaultValue defaultvalue = method.getAnnotation(DefaultValue.class);
		if (defaultvalue != null) {
			System.out.println("defaultvalue " + defaultvalue.value());
			// TODO method.getAnnotation(DefaultValue.class);?
		}

		DELETE delete = method.getAnnotation(DELETE.class);
		GET get = method.getAnnotation(GET.class);
		HEAD head = method.getAnnotation(HEAD.class);
		OPTIONS options = method.getAnnotation(OPTIONS.class);
		POST post = method.getAnnotation(POST.class);
		PUT put = method.getAnnotation(PUT.class);
		HttpMethod httpmethod = method.getAnnotation(HttpMethod.class);
		if (delete != null) {
			mi.setMethod(org.restlet.data.Method.DELETE);
		} else if (get != null) {
			mi.setMethod(org.restlet.data.Method.GET);
		} else if (head != null) {
			mi.setMethod(org.restlet.data.Method.HEAD);
		} else if (httpmethod != null) {
			mi.setMethod(org.restlet.data.Method.valueOf(httpmethod.value()));
		} else if (options != null) {
			mi.setMethod(org.restlet.data.Method.OPTIONS);
		} else if (post != null) {
			mi.setMethod(org.restlet.data.Method.POST);
		} else if (put != null) {
			mi.setMethod(org.restlet.data.Method.PUT);
		}

		Produces produces = method.getAnnotation(Produces.class);
		if (produces == null) {
			produces = cProduces;
		}

		Class<?> outputClass = method.getReturnType();
		if (produces != null && outputClass != null
				&& !Void.class.equals(outputClass)) {
			for (String produce : produces.value()) {
				Variant variant = new Variant(MediaType.valueOf(produce));
				RepresentationInfo representationInfo = null;

				if (javax.ws.rs.core.Response.class
						.isAssignableFrom(outputClass)) {
					// We can't interpret such responses
					representationInfo = new RepresentationInfo(variant);
					representationInfo
							.setType(org.restlet.representation.Representation.class);
					representationInfo.setIdentifier(representationInfo
							.getType().getCanonicalName());
					representationInfo.setName(representationInfo.getType()
							.getSimpleName());
					representationInfo.setRaw(true);
				} else {
					representationInfo = RepresentationInfo
							.describe(mi, outputClass,
									method.getGenericReturnType(), variant);
				}
				mi.getResponse().getRepresentations().add(representationInfo);
			}
		}

		// Cope with the incoming representation
		Consumes consumes = method.getAnnotation(Consumes.class);
		if (consumes == null) {
			consumes = cConsumes;
		}
		Annotation[][] parameterAnnotations = method.getParameterAnnotations();
		Class<?>[] parameterTypes = method.getParameterTypes();
		int i = 0;
		for (Annotation[] annotations : parameterAnnotations) {
			Class<?> parameterType = parameterTypes[i];
			scan(annotations, parameterType,
					method.getGenericParameterTypes()[i], info, resource, mi,
					consumes);
			i++;
		}

		// Introduced by Jax-rs 2.0,
		// Context context = method.getAnnotation(Context.class);
	}

	private static void scanAnnotation() {
		// HttpMethod x
		// NameBinding x
	}

	private static void scanConstructor() {
		// Encoded x
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
				LOGGER.log(Level.WARNING,
						"Please provide a name to your application, used "
								+ contract.getName() + " by default.");
			}
			LOGGER.info("Contract " + contract.getName() + " added.");

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
						LOGGER.info("Protocol " + protocol.getName()
								+ " added.");
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
			// This first phase discovers representations related to annotations
			// Let's cope with the inheritance chain, and complex properties
			List<RepresentationInfo> toBeAdded = new ArrayList<RepresentationInfo>();
			// Initialize the list of classes to be anaylized
			for (RepresentationInfo ri : mapReps.values()) {
				if (ri.isRaw()) {
					continue;
				}
				if (ri.isCollection()
						&& !mapReps.containsKey(ri.getType().getName())) {
					// Check if the type has been described.
					RepresentationInfo r = new RepresentationInfo(
							ri.getMediaType());
					r.setType(ri.getType());
					toBeAdded.add(r);
				}
				// Parent class
				Class<?> parentType = ri.getType().getSuperclass();
				if (parentType != null && ReflectUtils.isJdkClass(parentType)) {
					// TODO This type must introspected too, as it will reveal
					// other representation
					parentType = null;
				}
				if (parentType != null
						&& !mapReps.containsKey(parentType.getName())) {
					RepresentationInfo r = new RepresentationInfo(
							ri.getMediaType());
					r.setType(parentType);
					toBeAdded.add(r);
				}
				for (PropertyInfo pi : ri.getProperties()) {
					if (pi.getType() != null
							&& !mapReps.containsKey(pi.getType().getName())
							&& !toBeAdded.contains(pi.getType())) {
						RepresentationInfo r = new RepresentationInfo(
								ri.getMediaType());
						r.setType(pi.getType());
						toBeAdded.add(r);
					}
				}
			}
			// Second phase, discover classes and loop while classes are unknown
			while (!toBeAdded.isEmpty()) {
				RepresentationInfo[] tab = new RepresentationInfo[toBeAdded
						.size()];
				toBeAdded.toArray(tab);
				toBeAdded.clear();
				for (int i = 0; i < tab.length; i++) {
					RepresentationInfo current = tab[i];
					if (!current.isRaw()
							&& !ReflectUtils.isJdkClass(current.getType())) {
						if (!mapReps.containsKey(current.getName())) {
							// TODO clearly something is wrong here. We should
							// list all representations when discovering the
							// method.
							RepresentationInfo ri = RepresentationInfo
									.introspect(current.getType(), null,
											current.getMediaType());
							mapReps.put(ri.getIdentifier(), ri);
							// have a look at the parent type

							Class<?> parentType = ri.getType().getSuperclass();
							if (parentType != null
									&& ReflectUtils.isJdkClass(parentType)) {
								// TODO This type must introspected too, as it
								// will reveal
								// other representation
								parentType = null;
							}
							if (parentType != null
									&& !mapReps.containsKey(parentType
											.getName())) {
								RepresentationInfo r = new RepresentationInfo(
										ri.getMediaType());
								r.setType(parentType);
								toBeAdded.add(r);
							}
							for (PropertyInfo pi : ri.getProperties()) {
								if (pi.getType() != null
										&& !mapReps.containsKey(pi.getType()
												.getName())
										&& !toBeAdded.contains(pi.getType())) {
									RepresentationInfo r = new RepresentationInfo(
											ri.getMediaType());
									r.setType(pi.getType());
									toBeAdded.add(r);
								}
							}
						}
					}
				}
			}

			for (RepresentationInfo ri : mapReps.values()) {
				if (ReflectUtils.isJdkClass(ri.getType())) {
					// Filter the representations we want to expose.
					// TODO find a better way to express such filter
					continue;
				}
				LOGGER.info("Representation " + ri.getName() + " added.");
				Representation rep = new Representation();

				// TODO analyze
				// The models differ : one representation / one variant for
				// Restlet
				// one representation / several variants for APIspark
				rep.setDescription(toString(ri.getDocumentations()));
				rep.setName(ri.getName());

				rep.setProperties(new ArrayList<Property>());
				for (PropertyInfo pi : ri.getProperties()) {
					LOGGER.info("Property " + pi.getName() + " added.");
					Property p = new Property();
					p.setDefaultValue(pi.getDefaultValue());
					p.setDescription(pi.getDescription());
					p.setMax(pi.getMax());
					p.setMaxOccurs(pi.getMaxOccurs());
					p.setMin(pi.getMin());
					p.setMinOccurs(pi.getMinOccurs());
					p.setName(pi.getName());
					p.setPossibleValues(pi.getPossibleValues());
					if (pi.getType() != null) {
						// TODO: handle primitive type, etc
						p.setType(pi.getType().getSimpleName());
					}

					p.setUniqueItems(pi.isUniqueItems());

					rep.getProperties().add(p);
				}

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
		return toString(dis, "");
	}

	/**
	 * Concats a list of {@link DocumentationInfo} instances as a single String.
	 * 
	 * @param dis
	 *            The list of {@link DocumentationInfo} instances.
	 * @return A String value.
	 */
	private static String toString(List<DocumentationInfo> dis,
			String defaultValue) {
		if (dis != null && !dis.isEmpty()) {
			StringBuilder d = new StringBuilder();
			for (DocumentationInfo doc : dis) {
				if (doc.getTextContent() != null) {
					d.append(doc.getTextContent());
				}
			}
			if (d.length() > 0) {
				return d.toString();
			}
		}

		return defaultValue;
	}

	/** The current Web API definition. */
	private Definition definition;

	/**
	 * Constructor.
	 * 
	 * @param application
	 *            An application to introspect.
	 */
	public JaxrsIntrospector(javax.ws.rs.core.Application application) {
		definition = toDefinition(getApplicationInfo(application, null));

		if (definition != null) {
			LOGGER.fine("Look for the endpoint.");
			String endpoint = null;
			ApplicationPath ap = application.getClass().getAnnotation(
					ApplicationPath.class);
			if (ap != null) {
				endpoint = ap.value();
			}
			definition.setEndpoint(endpoint);
		}
	}

	/**
	 * Returns the current definition.
	 * 
	 * @return The current definition.
	 */
	private Definition getDefinition() {
		return definition;
	}
}