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

package org.restlet.ext.apispark.info;

import java.util.ArrayList;
import java.util.List;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.ext.apispark.Body;
import org.restlet.ext.apispark.Contract;
import org.restlet.ext.apispark.Documentation;
import org.restlet.ext.apispark.Method;
import org.restlet.ext.apispark.Operation;
import org.restlet.ext.apispark.Parameter;
import org.restlet.ext.apispark.PathVariable;
import org.restlet.ext.apispark.Property;
import org.restlet.ext.apispark.Representation;
import org.restlet.ext.apispark.Resource;
import org.restlet.ext.apispark.Response;
import org.restlet.ext.apispark.Variant;
import org.restlet.ext.jackson.JacksonRepresentation;

/**
 * Root of a APISpark description document.<br>
 * 
 * @author Jerome Louvel
 */
public class ApisparkRepresentation extends
		JacksonRepresentation<Documentation> {

	private static Documentation toDocumentation(ApplicationInfo application) {
		Documentation result = null;
		if (application != null) {
			result = new Documentation();
			result.setVersion(application.getVersion());
			if (application.getResources().getBaseRef() != null) {
				result.setEndpoint(application.getResources().getBaseRef()
						.toString());
			}

			Contract contract = new Contract();
			result.setContract(contract);
			contract.setDescription(toString(application.getDocumentations()));
			contract.setName(application.getName());

			// List of representations.
			contract.setRepresentations(new ArrayList<Representation>());
			for (RepresentationInfo ri : application.getRepresentations()) {
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

				rep.setProperties(new ArrayList<Property>());
				for (int i = 0; i < ri.getParameters().size(); i++) {
					ParameterInfo pi = ri.getParameters().get(i);

					Property property = new Property();
					property.setName(pi.getName());
					property.setDescription(toString(pi.getDocumentations()));
					property.setType(pi.getType());

					rep.getProperties().add(property);
				}

				contract.getRepresentations().add(rep);
			}

			// List of resources.
			// TODO Resource path/basePath?
			contract.setResources(new ArrayList<Resource>());
			addResources(application, contract, application.getResources()
					.getResources(), result.getEndpoint());

			java.util.List<String> protocols = new ArrayList<String>();
			for (ConnectorHelper<Server> helper : Engine.getInstance()
					.getRegisteredServers()) {
				for (Protocol protocol : helper.getProtocols()) {
					if (!protocols.contains(protocol.getName())) {
						protocols.add(protocol.getName());
					}
				}
			}

		}
		return result;
	}

	private static void addResources(ApplicationInfo application,
			Contract contract, List<ResourceInfo> resources, String basePath) {
		for (ResourceInfo ri : resources) {

			Resource resource = new Resource();
			resource.setDescription(toString(ri.getDocumentations()));
			resource.setName(ri.getIdentifier());
			if (basePath != null) {
				resource.setResourcePath(basePath + ri.getPath());
			} else {
				resource.setResourcePath(ri.getPath());
			}

			if (!ri.getChildResources().isEmpty()) {
				addResources(application, contract, ri.getChildResources(),
						ri.getPath());
			}

			resource.setOperations(new ArrayList<Operation>());
			for (MethodInfo mi : ri.getMethods()) {

				Operation operation = new Operation();
				operation.setDescription(toString(mi.getDocumentations()));
				operation.setName(mi.getName().getName());
				// TODO complete Method class with mi.getName()
				operation.setMethod(new Method());
				operation.getMethod().setDescription(
						mi.getName().getDescription());
				operation.getMethod().setName(mi.getName().getName());

				// Complete parameters
				operation.setHeaders(new ArrayList<Parameter>());
				operation.setPathVariables(new ArrayList<PathVariable>());
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
						} else if (ParameterStyle.TEMPLATE
								.equals(pi.getStyle())) {
							PathVariable pathVariable = new PathVariable();

							pathVariable.setDescription(toString(pi
									.getDocumentations()));
							pathVariable.setName(pi.getName());

							operation.getPathVariables().add(pathVariable);
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

					operation.setOutRepresentation(body);

					for (ResponseInfo rio : mi.getResponses()) {
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

	private static String toString(List<DocumentationInfo> di) {
		StringBuilder d = new StringBuilder();
		for (DocumentationInfo doc : di) {
			d.append(doc.getTextContent());
		}
		return d.toString();
	}

	/** The root element of the APISpark document. */
	private ApplicationInfo application;

	/**
	 * Constructor.
	 * 
	 * @param application
	 *            The root element of the APISpark document.
	 */
	public ApisparkRepresentation(ApplicationInfo application) {
		super(toDocumentation(application));

		this.application = application;
	}

	/**
	 * Constructor.
	 * 
	 * @param documentation
	 *            The description of the APISpark document.
	 */
	public ApisparkRepresentation(Documentation documentation) {
		super(documentation);
		// Transform contract to ApplicationInfo
	}

	// /**
	// * Constructor.
	// *
	// * @param representation
	// * The XML APISpark document.
	// * @throws IOException
	// */
	// public APISparkRepresentation(Representation representation)
	// throws IOException {
	// super(representation);
	// setMediaType(MediaType.APPLICATION_JSON);
	//
	// // Parse the given document using SAX to produce an ApplicationInfo
	// // instance.
	// // parse(new ContentReader(this));
	// }

	/**
	 * Returns the root element of the APISpark document.
	 * 
	 * @return The root element of the APISpark document.
	 */
	public ApplicationInfo getApplication() {
		return this.application;
	}

	/**
	 * Sets the root element of the APISpark document.
	 * 
	 * @param application
	 *            The root element of the APISpark document.
	 */
	public void setApplication(ApplicationInfo application) {
		this.application = application;
	}

}
