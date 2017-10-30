package org.restlet.ext.platform.internal.conversion.swagger.v2_0;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.ext.platform.internal.conversion.ConversionUtils;
import org.restlet.ext.platform.internal.model.Contract;
import org.restlet.ext.platform.internal.model.Definition;
import org.restlet.ext.platform.internal.model.Endpoint;
import org.restlet.ext.platform.internal.model.Header;
import org.restlet.ext.platform.internal.model.PathVariable;
import org.restlet.ext.platform.internal.model.PayLoad;
import org.restlet.ext.platform.internal.model.Representation;
import org.restlet.ext.platform.internal.model.Resource;
import org.restlet.ext.platform.internal.model.Section;

import com.wordnik.swagger.models.ArrayModel;
import com.wordnik.swagger.models.Info;
import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.Operation;
import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.RefModel;
import com.wordnik.swagger.models.Response;
import com.wordnik.swagger.models.Scheme;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.models.Tag;
import com.wordnik.swagger.models.auth.ApiKeyAuthDefinition;
import com.wordnik.swagger.models.auth.BasicAuthDefinition;
import com.wordnik.swagger.models.auth.OAuth2Definition;
import com.wordnik.swagger.models.auth.SecuritySchemeDefinition;
import com.wordnik.swagger.models.parameters.BodyParameter;
import com.wordnik.swagger.models.parameters.HeaderParameter;
import com.wordnik.swagger.models.parameters.Parameter;
import com.wordnik.swagger.models.parameters.PathParameter;
import com.wordnik.swagger.models.parameters.QueryParameter;
import com.wordnik.swagger.models.parameters.RefParameter;
import com.wordnik.swagger.models.properties.ArrayProperty;
import com.wordnik.swagger.models.properties.Property;
import com.wordnik.swagger.models.properties.RefProperty;

/**
 * Translator : RWADef <- Swagger 2.0.
 */
public class Swagger2Reader {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(Swagger2Reader.class.getName());

    private static void fillDeclaredParameters(Swagger swagger, Definition definition,
            Map<String, Object> parameters) {
        if (swagger.getParameters() == null) {
            return;
        }

        for (String key : swagger.getParameters().keySet()) {
            Parameter swaggerParameter = swagger.getParameters().get(key);
            if (swaggerParameter instanceof QueryParameter) {
                org.restlet.ext.platform.internal.model.QueryParameter queryParameter =
                        new org.restlet.ext.platform.internal.model.QueryParameter();
                fillRwadefQueryParameter(queryParameter, (QueryParameter) swaggerParameter);
                parameters.put(key, queryParameter);

            } else if (swaggerParameter instanceof PathParameter) {
                PathVariable pathVariable = new PathVariable();
                fillRwadefPathVariable(pathVariable, (PathParameter) swaggerParameter);
                parameters.put(key, pathVariable);

            } else if (swaggerParameter instanceof HeaderParameter) {
                Header header = new Header();
                fillRwadefHeader(header, (HeaderParameter) swaggerParameter);
                parameters.put(key, header);

            } else if (swaggerParameter instanceof BodyParameter) {
                PayLoad payload = new PayLoad();
                fillPayload((BodyParameter) swaggerParameter, payload);
                parameters.put(key, payload);
            } else {
                LOGGER.warning("The type of the parameter " + key + " was not recognized: "
                        + swaggerParameter.getClass().getName());
            }
        }
    }

    private static void fillInputPayload(Operation swaggerOperation,
            org.restlet.ext.platform.internal.model.Operation operation,
            Contract contract) {
        BodyParameter bodyParameter = SwaggerUtils.getInputPayload(swaggerOperation);

        if (bodyParameter != null) {
            PayLoad payload = new PayLoad();
            fillPayload(bodyParameter, payload);

            Representation representation = contract.getRepresentation(payload.getType());
            if (representation != null) {
                representation.addSections(swaggerOperation.getTags());
            }
            operation.setInputPayLoad(payload);
        }
    }

    private static void fillOutputPayload(Response swaggerResponse,
            org.restlet.ext.platform.internal.model.Response response,
            Operation swaggerOperation, Contract contract, 
            Map<String, Object> parameters) {
        Property property = swaggerResponse.getSchema();
        if (property == null) {
            return;
        }

        PayLoad payload = null;
        
        if (property instanceof RefProperty) {
            RefProperty refProperty = (RefProperty) property;
            Object declaredPayload = parameters.get(refProperty.get$ref());
            if (declaredPayload != null
                    && declaredPayload instanceof PayLoad) {
                payload = (PayLoad) declaredPayload;
            }

        }

        if (payload == null) {
            payload = new PayLoad();
            payload.setDescription(property.getDescription());
            payload.setArray(property instanceof ArrayProperty);
            payload.setType(SwaggerTypes.toDefinitionType(property));

        }

        Representation representation = contract.getRepresentation(payload.getType());
        if (representation != null) {
            representation.addSections(swaggerOperation.getTags());
        }

        response.setOutputPayLoad(payload);
    }

    private static void fillPayload(BodyParameter bodyParameter, PayLoad payload) {
        Model model = bodyParameter.getSchema();
        payload.setDescription(model.getDescription());

        if (model instanceof ArrayModel) {
            ArrayModel arrayModel = (ArrayModel) model;
            payload.setArray(true);
            payload.setType(SwaggerTypes.toDefinitionType(arrayModel.getItems()));

        } else if (model instanceof RefModel) {
            RefModel refModel = (RefModel) model;
            payload.setType(refModel.getSimpleRef());

        } else {
            // FIXME: should we fail ?
            LOGGER.warning("Unsupported input payload type: " + model.getClass().getSimpleName());
        }
    }

    private static void fillRepresentations(Swagger swagger, Contract contract) {
        if (swagger.getDefinitions() == null) {
            return;
        }

        for (String key : swagger.getDefinitions().keySet()) {
            Model model = swagger.getDefinitions().get(key);
            Representation representation = new Representation();
            representation.setDescription(model.getDescription());
            representation.setName(key);
            representation.setRaw(false);
            // TODO: example not implemented in RWADef (built from properties examples)
            fillRwadefProperties(model, representation);
            contract.getRepresentations().add(representation);
        }
    }

    private static void fillResources(Swagger swagger, Contract contract,
            List<String> produces, List<String> consumes,
            Map<String, Object> parameters) {
        if (swagger.getPaths() == null) {
            return;
        }

        for (String key : swagger.getPaths().keySet()) {
            Resource resource = new Resource();
            Path path = swagger.getPath(key);

            // TODO: description not implemented in Swagger 2.0
            resource.setName(ConversionUtils.processResourceName(key));
            resource.setResourcePath(key);
            for (Operation operation : path.getOperations()) {
                resource.addSections(operation.getTags());
            }

            if (path.getParameters() != null) {
                for (Parameter parameter : path.getParameters()) {
                    PathVariable pathVariable = null;
                    if (parameter instanceof PathParameter) {
                        pathVariable = new PathVariable();
                        fillRwadefPathVariable(pathVariable, (PathParameter) parameter);
                    } else if (parameter instanceof RefParameter) {
                        RefParameter refParameter = (RefParameter) parameter;
                        Object savedParameter = parameters.get(refParameter.getSimpleRef());
                        if (savedParameter instanceof PathVariable) {
                            pathVariable = (PathVariable) savedParameter;
                        }
                    }

                    if (pathVariable != null) {
                        resource.getPathVariables().add(pathVariable);
                    }
                }
            }
            fillRwadefOperations(path, resource, contract, produces, consumes, parameters);

            contract.getResources().add(resource);
        }

    }

    private static void fillRwadefEndpoints(Swagger swagger, Definition definition) {
        String authenticationProtocol = null;

        if (swagger.getSecurityDefinitions() != null) {
            for (String key : swagger.getSecurityDefinitions().keySet()) {
                SecuritySchemeDefinition securityDefinition = swagger.getSecurityDefinitions().get(key);
                if (securityDefinition instanceof BasicAuthDefinition) {
                    authenticationProtocol = ChallengeScheme.HTTP_BASIC.getName();
                } else if (securityDefinition instanceof OAuth2Definition) {
                    authenticationProtocol = ChallengeScheme.HTTP_OAUTH.getName();
                } else if (securityDefinition instanceof ApiKeyAuthDefinition) {
                    authenticationProtocol = ChallengeScheme.CUSTOM.getName();
                }
            }
        }

        if (swagger.getSchemes() != null
            && swagger.getHost() != null) {
            for (Scheme scheme : swagger.getSchemes()) {
                Endpoint endpoint = new Endpoint(scheme.toString().toLowerCase() + "://" + swagger.getHost()
                        + (swagger.getBasePath() == null ? "" : swagger.getBasePath()));
                endpoint.setAuthenticationProtocol(authenticationProtocol);
                definition.getEndpoints().add(endpoint);
            }
        }
    }

    private static void fillRwadefGeneralInformation(Swagger swagger, Definition definition,
            List<String> produces, List<String> consumes) {

        Info info = swagger.getInfo();

        // Contact
        if (info.getContact() != null) {
            org.restlet.ext.platform.internal.model.Contact contact = new org.restlet.ext.platform.internal.model.Contact();
            contact.setEmail(info.getContact().getEmail());
            contact.setName(info.getContact().getName());
            contact.setUrl(info.getContact().getUrl());
            definition.setContact(contact);
        }

        if (info.getLicense() != null) {
            // License
            org.restlet.ext.platform.internal.model.License license = new org.restlet.ext.platform.internal.model.License();
            license.setName(info.getLicense().getName());
            license.setUrl(info.getLicense().getUrl());
            definition.setLicense(license);
        }

        // Contract
        Contract contract = new Contract();
        contract.setDescription(info.getDescription());
        contract.setName(info.getTitle());
        definition.setContract(contract);

        // Media types
        if (swagger.getProduces() != null) {
            produces.addAll(swagger.getProduces());
        }
        if (swagger.getConsumes() != null) {
            consumes.addAll(swagger.getConsumes());
        }

        // General
        definition.setVersion(info.getVersion());
        definition.setTermsOfService(info.getTermsOfService());
        // TODO: attribution and keywords not implemented in Swagger 2.0
        definition.setAttribution(null);
    }

    private static void fillRwadefHeader(Header header, HeaderParameter swaggerHeader) {
        header.setName(swaggerHeader.getName());
        header.setRequired(swaggerHeader.getRequired());
        header.setDescription(swaggerHeader.getDescription());
        header.setAllowMultiple(true);
        header.setDefaultValue(swaggerHeader.getDefaultValue());
        // TODO: example not implemented in Swagger 2.0

        SwaggerTypeFormat swaggerTypeFormat = new SwaggerTypeFormat(
                swaggerHeader.getType(),
                swaggerHeader.getFormat(),
                swaggerHeader.getItems());
        header.setType(SwaggerTypes.toDefinitionPrimitiveType(swaggerTypeFormat));
    }

    private static void fillRwadefOperation(Operation swaggerOperation, Resource resource,
            Contract contract, String methodName,
            List<String> produces, List<String> consumes,
            Map<String, Object> parameters) {
        if (swaggerOperation == null) {
            return;
        }

        org.restlet.ext.platform.internal.model.Operation operation =
                new org.restlet.ext.platform.internal.model.Operation();

        operation.addProduces(produces);
        operation.addProduces(swaggerOperation.getProduces());

        operation.addConsumes(consumes);
        operation.addConsumes(swaggerOperation.getConsumes());

        operation.setDescription(swaggerOperation.getDescription());
        operation.setMethod(methodName);
        operation.setName(swaggerOperation.getOperationId());

        fillRwadefParameters(swaggerOperation, operation, resource, parameters);
        fillRwadefResponses(swaggerOperation, operation, contract, parameters);
        fillInputPayload(swaggerOperation, operation, contract);

        resource.getOperations().add(operation);
    }

    private static void fillRwadefOperations(Path path, Resource resource,
            Contract contract, List<String> produces, List<String> consumes,
            Map<String, Object> parameters) {

        fillRwadefOperation(path.getGet(), resource, contract, Method.GET.getName(),
                produces, consumes, parameters);
        fillRwadefOperation(path.getPost(), resource, contract, Method.POST.getName(),
                produces, consumes, parameters);
        fillRwadefOperation(path.getPut(), resource, contract, Method.PUT.getName(),
                produces, consumes, parameters);
        fillRwadefOperation(path.getDelete(), resource, contract, Method.DELETE.getName(),
                produces, consumes, parameters);
        fillRwadefOperation(path.getOptions(), resource, contract, Method.OPTIONS.getName(),
                produces, consumes, parameters);
        fillRwadefOperation(path.getPatch(), resource, contract, Method.PATCH.getName(),
                produces, consumes, parameters);
    }

    private static void fillRwadefParameters(Operation swaggerOperation,
                                             org.restlet.ext.platform.internal.model.Operation operation, Resource resource,
                                             Map<String, Object> parameters) {
        if (swaggerOperation.getParameters() == null) {
            return;
        }

        for (Parameter swaggerParameter : swaggerOperation.getParameters()) {

            if (swaggerParameter instanceof RefParameter) {
                RefParameter refParameter = (RefParameter) swaggerParameter;
                Object rwadefParameter = parameters.get(refParameter.getSimpleRef());

                if (rwadefParameter == null) {
                    LOGGER.warning("The parameter " + refParameter.getName()
                            + " was not found in the list of declared parameters.");
                } else if (rwadefParameter instanceof Header) {
                    operation.getHeaders().add((Header) rwadefParameter);
                } else if (rwadefParameter instanceof PathVariable) {
                    resource.addPathVariable((PathVariable) rwadefParameter);

                } else if (rwadefParameter instanceof org.restlet.ext.platform.internal.model.QueryParameter) {
                    operation.getQueryParameters().add(
                            (org.restlet.ext.platform.internal.model.QueryParameter) rwadefParameter);

                } else if (rwadefParameter instanceof PayLoad) {
                    operation.setInputPayLoad((PayLoad) rwadefParameter);

                } else {
                    LOGGER.warning("The type of declared parameter " + refParameter.getName() + ", " +
                            rwadefParameter.getClass() + " is not supported.");
                }

            } else if (swaggerParameter instanceof QueryParameter) {
                org.restlet.ext.platform.internal.model.QueryParameter queryParameter =
                        new org.restlet.ext.platform.internal.model.QueryParameter();
                QueryParameter swaggerQueryParameter = (QueryParameter) swaggerParameter;

                fillRwadefQueryParameter(queryParameter, swaggerQueryParameter);
                operation.getQueryParameters().add(queryParameter);
            } else if (swaggerParameter instanceof PathParameter) {
                org.restlet.ext.platform.internal.model.PathVariable pathVariable =
                        new org.restlet.ext.platform.internal.model.PathVariable();
                PathParameter swaggerPathVariable = (PathParameter) swaggerParameter;

                fillRwadefPathVariable(pathVariable, swaggerPathVariable);
                if (resource.getPathVariable(pathVariable.getName()) == null) {
                    resource.getPathVariables().add(pathVariable);
                }
            } else if (swaggerParameter instanceof HeaderParameter) {
                Header header = new Header();
                HeaderParameter swaggerHeader = new HeaderParameter();

                fillRwadefHeader(header, swaggerHeader);
                operation.getHeaders().add(header);
            } else {
                if (!(swaggerParameter instanceof BodyParameter)) {
                    LOGGER.warning("Unsupported parameter type for " + swaggerParameter.getName() +
                            " of type " + swaggerParameter.getClass().getName());
                }
            }
        }
    }

    private static void fillRwadefPathVariable(PathVariable pathVariable, PathParameter swaggerPathVariable) {
        pathVariable.setName(swaggerPathVariable.getName());
        pathVariable.setRequired(swaggerPathVariable.getRequired());
        pathVariable.setDescription(swaggerPathVariable.getDescription());
        // TODO: example not implemented in Swagger 2.0

        SwaggerTypeFormat swaggerTypeFormat = new SwaggerTypeFormat(
                swaggerPathVariable.getType(),
                swaggerPathVariable.getFormat(),
                swaggerPathVariable.getItems());
        pathVariable.setType(SwaggerTypes.toDefinitionPrimitiveType(swaggerTypeFormat));
    }

    private static void fillRwadefProperties(Model model, Representation representation) {
        if (model.getProperties() == null) {
            return;
        }

        for (String key : model.getProperties().keySet()) {
            org.restlet.ext.platform.internal.model.Property property =
                    new org.restlet.ext.platform.internal.model.Property();
            Property swaggerProperty = model.getProperties().get(key);

            property.setDefaultValue(swaggerProperty.getDefault());
            property.setDescription(swaggerProperty.getDescription());
            // TODO: enumeration not implemented in Swagger 2.0
            property.setExample(swaggerProperty.getExample());
            property.setRequired(swaggerProperty.getRequired());
            property.setList(swaggerProperty instanceof ArrayProperty);
            property.setName(key);
            // TODO: sub-properties not implemented in Swagger 2.0
            // TODO: uniqueItems not implemented in Swagger 2.0
            property.setUniqueItems(false);

            if (swaggerProperty instanceof ArrayProperty) {
                ArrayProperty arrayProperty = (ArrayProperty) swaggerProperty;
                property.setExample(arrayProperty.getItems().getExample());
            }

            property.setType(SwaggerTypes.toDefinitionType(swaggerProperty));

            representation.getProperties().add(property);
        }
    }

    /**
     * Fills the given RWADef query parameter information from the given Swagger query parameter.
     * 
     * @param queryParameter
     *            The RWADef query parameter.
     * @param swaggerQueryParameter
     *            The Swagger query parameter.
     */
    private static void fillRwadefQueryParameter(org.restlet.ext.platform.internal.model.QueryParameter queryParameter,
            QueryParameter swaggerQueryParameter) {

        // TODO: allowMultiple not implemented in Swagger 2.0
        queryParameter.setAllowMultiple(true);
        queryParameter.setDefaultValue(swaggerQueryParameter.getDefaultValue());
        queryParameter.setDescription(swaggerQueryParameter.getDescription());
        queryParameter.setEnumeration(swaggerQueryParameter.getEnum());
        // TODO: example not implemented in Swagger 2.0
        queryParameter.setName(swaggerQueryParameter.getName());
        queryParameter.setRequired(swaggerQueryParameter.getRequired());
        queryParameter.setSeparator(SwaggerUtils.getSeparator(swaggerQueryParameter.getCollectionFormat()));

        SwaggerTypeFormat swaggerTypeFormat = new SwaggerTypeFormat(
                swaggerQueryParameter.getType(),
                swaggerQueryParameter.getFormat(),
                swaggerQueryParameter.getItems());
        queryParameter.setType(SwaggerTypes.toDefinitionPrimitiveType(swaggerTypeFormat));
    }

    private static void fillRwadefResponses(Operation swaggerOperation,
            org.restlet.ext.platform.internal.model.Operation operation,
            Contract contract, Map<String, Object> parameters) {
        if (swaggerOperation == null) {
            return;
        }

        if (swaggerOperation.getResponses() != null) {
            for (String key : swaggerOperation.getResponses().keySet()) {
                Response swaggerResponse = swaggerOperation.getResponses().get(key);
                org.restlet.ext.platform.internal.model.Response response =
                        new org.restlet.ext.platform.internal.model.Response();

                int statusCode;
                try {
                    statusCode = Integer.parseInt(key);
                    response.setCode(statusCode);
                } catch (Exception e) {
                    // TODO: what to do with "Default" responses ?
                    LOGGER.warning("Response " + key + " for operation " + swaggerOperation.getOperationId() +
                            " could not be retrieved because its key is not a valid status code.");
                    continue;
                }

                response.setDescription(swaggerResponse.getDescription());
                response.setName(ConversionUtils.generateResponseName(statusCode));

                fillOutputPayload(swaggerResponse, response, swaggerOperation, contract, parameters);

                operation.getResponses().add(response);
            }
        }
    }

    private static void fillSections(Swagger swagger, Contract contract) {
        if (swagger.getTags() == null) {
            return;
        }

        for (Tag tag : swagger.getTags()) {
            Section section = new Section();
            section.setName(tag.getName());
            section.setDescription(tag.getDescription());
            contract.getSections().add(section);
        }
    }

    /**
     * Translates a Swagger definition to a Restlet Web API Definition
     * 
     * @param Swagger
     *            The translated Swagger 2.0 definition
     * 
     * @return The Restlet Web API definition
     */
    public static Definition translate(Swagger swagger) {

        // conversion
        Definition definition = new Definition();

        // fill RWADef main attributes
        fillRwadefEndpoints(swagger, definition);

        List<String> produces = new ArrayList<>();
        List<String> consumes = new ArrayList<>();
        fillRwadefGeneralInformation(swagger, definition, produces, consumes);

        // fill definition.sections
        Contract contract = definition.getContract();
        fillSections(swagger, contract);

        // Get declared parameters
        Map<String, Object> parameters = new LinkedHashMap<>();
        fillDeclaredParameters(swagger, definition, parameters);

        // fill definition.representations
        fillRepresentations(swagger, contract);

        // fill definition.resources
        fillResources(swagger, contract, produces, consumes, parameters);

        for (Representation representation : contract.getRepresentations()) {
            representation.addSectionsToProperties(contract);
        }

        return definition;
    }
}
