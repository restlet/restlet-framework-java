package org.restlet.ext.apispark.internal.conversion;

import com.wordnik.swagger.models.*;
import com.wordnik.swagger.models.parameters.BodyParameter;
import com.wordnik.swagger.models.parameters.HeaderParameter;
import com.wordnik.swagger.models.parameters.PathParameter;
import com.wordnik.swagger.models.properties.*;
import org.apache.commons.lang.StringUtils;
import org.restlet.ext.apispark.internal.model.*;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.Response;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Translator : RWADEF <-> Swagger 2.0.
 */
public class Swagger2Translator {


    public static final Float SWAGGER_2_0_VERSION = 2.0f;

    /**
     * Rwadef -> Swagger 2.0
     *
     * @param definition Rwadef
     * @return Swagger 2.0
     */
    public Swagger getSwaggerFromRoadef(Definition definition) {

        //definition validation
        Assert.notNull(definition);
        Assert.notNull(definition.getContract());
        Assert.hasLength(definition.getContract().getName());
        Assert.notEmpty(definition.getEndpoints());
        Assert.notNull(definition.getContract().getResources());
        Assert.notNull(definition.getContract().getRepresentations());

        //conversion
        Swagger swagger = new Swagger();
        swagger.setSwagger(SWAGGER_2_0_VERSION); //required

        //fill swagger main attributes
        fillMainAttributes(definition, swagger);

        // fill swagger.info
        fillInfo(definition, swagger); //required

        // fill swagger.paths
        fillPaths(definition, swagger); //required

        // fill swagger.definitions
        fillDefinitions(definition, swagger);

        // TODO add authorization attribute

        return swagger;
    }

    /**
     * Fill swagger main attributes from rwadef definition
     *
     * @param definition rwadef definition
     * @param swagger    swagger definition
     */
    private void fillMainAttributes(Definition definition, Swagger swagger) {
        // basePath
        Endpoint endpoint = definition.getEndpoints().get(0);
        swagger.setHost(endpoint.getDomain());
        swagger.setBasePath(endpoint.getBasePath());
        //TODO all schemes ?

        //Should be any of "http", "https", "ws", "wss"
        swagger.setSchemes(
                Arrays.asList(
                        Scheme.forValue(endpoint.getProtocol())));

        //TODO check all
//        swagger.setConsumes();
//        swagger.setProduces();
        //swagger.parameters
        //swagger.responses
//        swagger.setSecurityDefinition();
    }

    /**
     * Fill swagger "Info" object from rwadef definition
     *
     * @param definition rwadef definition
     * @param swagger    swagger definition
     */
    private void fillInfo(Definition definition, Swagger swagger) {
        Info infoSwagger = new Info();

        infoSwagger.setTitle(definition.getContract().getName()); //required
        infoSwagger.setDescription(definition.getContract().getDescription());
        infoSwagger.setVersion(definition.getVersion()); //required

        Contact contactSwagger = new Contact();
        contactSwagger.setName(definition.getContact());
        infoSwagger.setContact(contactSwagger);


        //TODO license.name required (not in rwadef)
//        License licenseSwagger = new License();
//        licenseSwagger.setName(); //required
//        licenseSwagger.setUrl(definition.getLicense());
//        infoSwagger.setLicense(licenseSwagger);

        swagger.setInfo(infoSwagger); //required
    }

    /**
     * Fill swagger "Paths" objects from rwadef definition
     *
     * @param definition rwadef definition
     * @param swagger    swagger definition
     */
    private void fillPaths(Definition definition, Swagger swagger) {
        Map<String, Path> paths = new LinkedHashMap<String, Path>();

        for (Resource resource : definition.getContract().getResources()) {
            Path pathSwagger = new Path();

            fillPathOperations(resource, pathSwagger);
            paths.put(resource.getResourcePath(), pathSwagger);
            //TODO path.parameters not in swagger bean - not in rwadef either
        }
        swagger.setPaths(paths);
    }

    /**
     * Fill swagger "Paths.Operations" objects from rwadef definition
     *
     * @param resource    rwadef.resource definition
     * @param pathSwagger swagger.path definition
     */
    private void fillPathOperations(Resource resource, Path pathSwagger) {
        for (Operation operation : resource.getOperations()) {

            com.wordnik.swagger.models.Operation operationSwagger =
                    new com.wordnik.swagger.models.Operation();

            String method = operation.getMethod().toLowerCase();
            Path setResult = pathSwagger.set(method, operationSwagger);
            if (setResult == null) {
                //TODO add error message in a conversion results bean
            }

            String description = operation.getDescription();

            operationSwagger.setSummary(StringUtils.substring(description, 0, 120));
            operationSwagger.setDescription(description);
            operationSwagger.setOperationId(operation.getName());
            operationSwagger.setConsumes(operation.getConsumes());
            operationSwagger.setProduces(operation.getProduces());
            //TODO add security
//            operationSwagger.setSecurity();

            fillOperationParameters(resource, operation, operationSwagger);

            fillOperationResponses(operation, operationSwagger);
        }
    }

    private void fillOperationParameters(Resource resource, Operation operation, com.wordnik.swagger.models.Operation operationSwagger) {

        //Path parameters
        for (PathVariable pathVariable : resource.getPathVariables()) {
            PathParameter pathParameterSwagger = new PathParameter();
            pathParameterSwagger.setType(toSwaggerType(pathVariable.getType())); //required
            pathParameterSwagger.setName(pathVariable.getName()); //required
            pathParameterSwagger.setDescription(pathVariable.getDescription());
            operationSwagger.addParameter(pathParameterSwagger);
        }

        //Body
        if (operation.getInRepresentation() != null) {
            BodyParameter bodyParameterSwagger = new BodyParameter();
            bodyParameterSwagger.setName("body");
            //TODO missing
//            bodyParameterSwagger.setDescription();

            //FIXME !!
            Model schema;
            Entity inRepr = operation.getInRepresentation();
            if ("Representation".equals(inRepr.getType())) {
                //TODO no FormDataParameters
                schema = null;
            } else {

                if (inRepr.isArray()) {
                    ArrayModel arrayModel = new ArrayModel();
                    arrayModel.setType("array");
                    //primitive or ref type
                    arrayModel.setItems(newPropertyForType(inRepr.getType()));
                    schema = arrayModel;
                } else {
                    if (isPrimitiveType(inRepr.getType())) {
                        ModelImpl modelImpl = new ModelImpl();
                        modelImpl.setType(toSwaggerType(inRepr.getType()));
                        schema = modelImpl;
                    } else {
                        RefModel refModel = new RefModel();
                        refModel.set$ref(inRepr.getType());
                        schema = refModel;
                    }
                }
            }
            //TODO RefModel
            bodyParameterSwagger.setSchema(schema);
            operationSwagger.addParameter(bodyParameterSwagger);
        }

        //query parameters
        for (QueryParameter queryParameter : operation.getQueryParameters()) {
            com.wordnik.swagger.models.parameters.QueryParameter queryParameterSwagger =
                    new com.wordnik.swagger.models.parameters.QueryParameter();
            queryParameterSwagger.setRequired(queryParameter.isRequired());
            if (queryParameter.isAllowMultiple()) {
                queryParameterSwagger.setType("array");
                queryParameterSwagger.setItems(newPropertyForType(queryParameter.getType()));
            } else {
                queryParameterSwagger.setType(toSwaggerType(queryParameter.getType()));
            }
            queryParameterSwagger.setName(queryParameter.getName());
            queryParameterSwagger.setDescription(queryParameter.getDescription());
            //TODO attributes above are not present in swagger bean
            // queryParameterSwagger.setDefaultValue(queryParameter.getDefault());
            // queryParameterSwagger.setEnumeration(queryParameter.getEnumeration());
            // queryParameterSwagger.setCollectionFormat();
            operationSwagger.addParameter(queryParameterSwagger);
        }

        for (Header header : operation.getHeaders()) {
            HeaderParameter headerParameterSwagger = new HeaderParameter();
            headerParameterSwagger.setRequired(header.isRequired());
            headerParameterSwagger.setType(toSwaggerType(header.getType()));
            headerParameterSwagger.setName(header.getName());
            headerParameterSwagger.setDescription(header.getDescription());
            //TODO attributes above are not present in swagger bean
            // headerParameterSwagger.setDefaultValue(header.getDefault());
            // headerParameterSwagger.setEnumeration(header.getEnumeration());
            operationSwagger.addParameter(headerParameterSwagger);
        }
    }

    private void fillOperationResponses(Operation operation, com.wordnik.swagger.models.Operation operationSwagger) {
        for (Response reponse : operation.getResponses()) {
            /* Response -> Response */
            com.wordnik.swagger.models.Response responseSwagger =
                    new com.wordnik.swagger.models.Response();

            responseSwagger.setDescription(reponse.getDescription()); //required

            // Response Schema
            if (reponse.getEntity() != null) {
                Entity entity = reponse.getEntity();

                if ("file".equals(entity.getType().toLowerCase())) {
                    //TODO no FileProperty in swagger beans
                } else if (entity.isArray()) {
                    ArrayProperty arrayProperty = new ArrayProperty();
                    arrayProperty.setItems(newPropertyForType(entity.getType()));
                    responseSwagger.setSchema(arrayProperty);
                } else {
                    responseSwagger.setSchema(newPropertyForType(entity.getType()));
                }
            }

            operationSwagger.addResponse(
                    String.valueOf(reponse.getCode()),
                    responseSwagger);

        }
        //TODO check that at least one success code is present
    }

    /**
     * Fill swagger "Definitions" objects from rwadef definition
     *
     * @param definition rwadef definition
     * @param swagger    swagger definition
     */
    private void fillDefinitions(Definition definition, Swagger swagger) {
        for (Representation representationRwadef : definition.getContract().getRepresentations()) {
            if (isPrimitiveType(representationRwadef.getName())) {
                continue;
            }

            /* Representation -> Model */
            ModelImpl modelSwagger = new ModelImpl();
            modelSwagger.setName(representationRwadef.getName());
            swagger.addDefinition(modelSwagger.getName(), modelSwagger);

            /* Property -> Property */
            if (representationRwadef.getProperties() != null) {
                for (Property propertyRwadef : representationRwadef.getProperties()) {

                    com.wordnik.swagger.models.properties.Property propertySwagger;

                    // property type
                    if (
                            propertyRwadef.getMaxOccurs() != null &&
                                    ((propertyRwadef.getMaxOccurs() > 1) || (propertyRwadef.getMaxOccurs() == -1))
                            ) {
                        ArrayProperty arrayProperty = new ArrayProperty();
                        arrayProperty.setItems(newPropertyForType(propertyRwadef.getType()));
                        propertySwagger = arrayProperty;
                    } else {
                        propertySwagger = newPropertyForType(propertyRwadef.getType());
                    }

                    modelSwagger.property(propertyRwadef.getName(), propertySwagger);

                    propertySwagger.setName(propertyRwadef.getName());
                    // min and max
                    if (propertySwagger instanceof AbstractNumericProperty) {
                        AbstractNumericProperty abstractNumericProperty = (AbstractNumericProperty) propertySwagger;
                        try {
                            abstractNumericProperty.setMinimum(Double.valueOf(propertyRwadef.getMin()));
                        } catch (NumberFormatException e) {
                            // TODO Add log
                        }
                        try {
                            abstractNumericProperty.setMaximum(Double.valueOf(propertyRwadef.getMax()));
                        } catch (NumberFormatException e) {
                            // TODO Add log
                        }
                    }

                    propertySwagger.setDescription(propertyRwadef.getDescription());

                }
            }

        }
    }

    /**
     * Indicates if the given type is a primitive type.
     *
     * @param type The type to be analysed
     * @return A boolean of value true if the given type is primitive, false
     * otherwise.
     */
    private boolean isPrimitiveType(String type) {
        if ("string".equals(type.toLowerCase())
                || "int".equals(type.toLowerCase())
                || "integer".equals(type.toLowerCase())
                || "long".equals(type.toLowerCase())
                || "float".equals(type.toLowerCase())
                || "double".equals(type.toLowerCase())
                || "date".equals(type.toLowerCase())
                || "boolean".equals(type.toLowerCase())
                || "bool".equals(type.toLowerCase())) {
            return true;
        }
        return false;
    }

    /**
     * Returns the primitive types as Swagger expects them
     *
     * @param type The type name to Swaggerize
     * @return The Swaggerized type
     */
    //TODO to move in SwaggerUtils
    private String toSwaggerType(String type) {
        //TODO exhaustive liste of rwadef type
        if ("Integer".equals(type)) {
            return "int";
        } else if ("String".equals(type)) {
            return "string";
        } else if ("Boolean".equals(type)) {
            return "boolean";
        } else {
            //TODO check all possibles values
            return type;
        }
    }


    /**
     * Get new property for Swagger 2.0 for the primitive type of Rwadef.
     *
     * @param type Type Rwadef
     * @return Type Swagger
     */
    //TODO to move in SwaggerUtils
    private com.wordnik.swagger.models.properties.Property newPropertyForType(String type) {
        if ("string".equals(type.toLowerCase())) {
            return new StringProperty();
        } else if ("int".equals(type.toLowerCase())) {
            return new IntegerProperty();
        } else if ("integer".equals(type.toLowerCase())) {
            return new IntegerProperty();
        } else if ("long".equals(type.toLowerCase())) {
            return new LongProperty();
        } else if ("float".equals(type.toLowerCase())) {
            return new FloatProperty();
        } else if ("double".equals(type.toLowerCase())) {
            return new DoubleProperty();
        } else if ("date".equals(type.toLowerCase())) {
            return new DateProperty();
        } else if ("boolean".equals(type.toLowerCase())) {
            return new BooleanProperty();
        } else if ("bool".equals(type.toLowerCase())) {
            return new BooleanProperty();
        }
        // Reference to a representation
        return new RefProperty(type);
    }

}
