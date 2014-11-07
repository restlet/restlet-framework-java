package org.restlet.test.ext.apispark.conversion.swagger.v2_0;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.restlet.data.MediaType;
import org.restlet.ext.apispark.internal.conversion.swagger.v2_0.Swagger2Translator;
import org.restlet.ext.apispark.internal.model.Contract;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;
import org.restlet.ext.apispark.internal.model.License;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.PayLoad;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.representation.FileRepresentation;
import org.restlet.test.RestletTestCase;

import com.wordnik.swagger.models.Contact;
import com.wordnik.swagger.models.Info;
import com.wordnik.swagger.models.Model;
import com.wordnik.swagger.models.ModelImpl;
import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.models.parameters.BodyParameter;
import com.wordnik.swagger.models.parameters.Parameter;
import com.wordnik.swagger.models.parameters.PathParameter;
import com.wordnik.swagger.models.properties.ArrayProperty;
import com.wordnik.swagger.models.properties.IntegerProperty;
import com.wordnik.swagger.models.properties.RefProperty;
import com.wordnik.swagger.models.properties.StringProperty;
import com.wordnik.swagger.util.SwaggerLoader;

public class Swagger2TranslatorTestCase extends RestletTestCase {

    /**
     * Conversion Rwadef -> Swagger 2.0.
     */
    public void testGetSwagger1() {
        // Given

        // definition
        Definition definition = new Definition();
        definition.setVersion("version");
        org.restlet.ext.apispark.internal.model.Contact contact = new org.restlet.ext.apispark.internal.model.Contact();
        contact.setName("contact");
        definition.setContact(contact);
        License license = new License();
        license.setName("license");
        definition.setLicense(license);

        // endpoint
        Endpoint endpoint = new Endpoint();
        definition.getEndpoints().add(endpoint);
        endpoint.setProtocol("protocol");
        endpoint.setDomain("domain");
        endpoint.setPort(999);
        endpoint.setBasePath("/basePath");

        // contract
        Contract contract = new Contract();
        definition.setContract(contract);
        contract.setName("contract.name");
        contract.setDescription("contract.description");

        // resource 1
        Resource resource1 = new Resource();
        contract.getResources().add(resource1);
        resource1.setResourcePath("resourcePath1");

        // resource 1 : operation 1
        Operation operation1 = new Operation();
        resource1.getOperations().add(operation1);
        operation1.setMethod("get");
        operation1.setName("nameoperation1");
        operation1.setDescription("description");

        // resource 1 : operation 1 : produces
        List<String> op1Produces = new ArrayList<String>();
        operation1.setProduces(op1Produces);
        op1Produces.add("produce 1");
        op1Produces.add("produce 2");

        // resource 1 : operation 1 : consumes
        List<String> op1Consumes = new ArrayList<String>();
        operation1.setConsumes(op1Consumes);
        op1Consumes.add("consume 1");
        op1Consumes.add("consume 2");

        // resource 1 : path variable 1
        PathVariable pathVariable1 = new PathVariable();
        resource1.getPathVariables().add(pathVariable1);
        pathVariable1.setName("namePathVariable1");
        pathVariable1.setType("string");
        pathVariable1.setDescription("description");

        // resource 1 : path variable 2
        PathVariable pathVariable2 = new PathVariable();
        resource1.getPathVariables().add(pathVariable2);
        pathVariable2.setName("namePathVariable2");
        pathVariable2.setType("string");
        pathVariable2.setDescription("description");

        // resource 1 : operation 1 : inRepresentation
        PayLoad inRepr = new PayLoad();
        operation1.setInputPayLoad(inRepr);
        inRepr.setType("integer");

        // resource 1 : operation 1 : queryParameter 1
        QueryParameter queryParameter1 = new QueryParameter();
        operation1.getQueryParameters().add(queryParameter1);
        queryParameter1.setName("nameQueryParameter1");
        queryParameter1.setType("integer");
        queryParameter1.setDescription("description");

        // resource 1 : operation 1 : queryParameter 2
        QueryParameter queryParameter2 = new QueryParameter();
        operation1.getQueryParameters().add(queryParameter2);
        queryParameter2.setName("nameQueryParameter2");
        queryParameter2.setType("string");
        queryParameter2.setDescription("description");

        // resource 1 : operation 1 : response 1
        Response response1 = new Response();
        operation1.getResponses().add(response1);
        response1.setCode(200);
        response1.setDescription("description");
        PayLoad response1Entity = new PayLoad();
        response1.setOutputPayLoad(response1Entity);
        response1Entity.setArray(true);
        response1Entity.setType("integer");
        assertTrue(response1.getOutputPayLoad().isArray());

        // resource 1 : operation 1 : response 2
        Response response2 = new Response();
        operation1.getResponses().add(response2);
        response2.setCode(300);
        response2.setDescription("description");
        PayLoad response2Entity = new PayLoad();
        response2.setOutputPayLoad(response2Entity);
        response2Entity.setArray(false);
        response2Entity.setType("Entity2");
        assertFalse(response2.getOutputPayLoad().isArray());

        // resource 1 : operation 1 : response 3
        Response response3 = new Response();
        operation1.getResponses().add(response3);
        response3.setCode(400);
        response3.setDescription("description");
        PayLoad response3Entity = new PayLoad();
        response3.setOutputPayLoad(response3Entity);
        response3Entity.setArray(false);
        response3Entity.setType("integer");
        assertFalse(response3.getOutputPayLoad().isArray());

        // resource 2
        Resource resource2 = new Resource();
        contract.getResources().add(resource2);
        resource2.setResourcePath("resourcePath2");

        // representation 1
        Representation representation1 = new Representation();
        definition.getContract().getRepresentations().add(representation1);
        representation1.setIdentifier("com.restlet.nameRepresentation1");
        representation1.setName("nameRepresentation1");

        // representation 1 : property 1
        Property representation1Property1 = new Property();
        representation1.getProperties().add(representation1Property1);
        representation1Property1
                .setName("com.restlet.nameRepresentation1Property1");
        representation1Property1.setType("integer");
        representation1Property1.setMin("1.0");
        representation1Property1.setMax("2.0");
        representation1Property1.setDescription("description");

        // representation 1 : property 2
        Property representation1Property2 = new Property();
        representation1.getProperties().add(representation1Property2);
        representation1Property2
                .setName("com.restlet.nameRepresentation1Property2");
        representation1Property2.setMaxOccurs(-1);
        representation1Property2.setType("string");
        representation1Property2.setDescription("description");

        // representation 1 : property 3
        Property representation1Property3 = new Property();
        representation1.getProperties().add(representation1Property3);
        representation1Property3
                .setName("com.restlet.nameRepresentation1Property3");
        representation1Property3.setType("Entity");
        representation1Property3.setDescription("description");

        // representation 2
        Representation representation2 = new Representation();
        definition.getContract().getRepresentations().add(representation2);
        representation2.setIdentifier("com.restlet.nameRepresentation2");
        representation2.setName("nameRepresentation2");

        // When
        Swagger swagger = Swagger2Translator.getSwagger(definition);

        // Then
        assertEquals("2.0", swagger.getSwagger());

        Info infoSwagger = swagger.getInfo();
        assertEquals("version", infoSwagger.getVersion());
        Contact contactSwagger = infoSwagger.getContact();
        assertEquals("contact", contactSwagger.getName());
        // TODO uncomment license assertion when translator will handle it
        // License license = infoSwagger.getLicense();
        // assertEquals("license", license.getUrl());
        assertEquals("contract.name", infoSwagger.getTitle());
        assertEquals("contract.description", infoSwagger.getDescription());
        assertEquals("/basePath", swagger.getBasePath());
        // resource 1
        Path path1 = swagger.getPath("resourcePath1");
        assertNotNull(path1);
        com.wordnik.swagger.models.Operation path1Get = path1.getGet();
        assertNotNull(path1Get);
        assertEquals("nameoperation1", path1Get.getOperationId());
        assertEquals("description", path1Get.getDescription());
        assertEquals(2, path1Get.getProduces().size());
        assertEquals("produce 1", path1Get.getProduces().get(0));
        assertEquals("produce 2", path1Get.getProduces().get(1));
        assertEquals(2, path1Get.getConsumes().size());
        assertEquals("consume 1", path1Get.getConsumes().get(0));
        assertEquals("consume 2", path1Get.getConsumes().get(1));
        // path variable 1
        PathParameter op1PathParameter1 = (PathParameter) path1Get
                .getParameters().get(0);
        assertEquals("namePathVariable1", op1PathParameter1.getName());
        assertEquals("path", op1PathParameter1.getIn());
        assertEquals("string", op1PathParameter1.getType());
        assertEquals("description", op1PathParameter1.getDescription());
        // path variable 2
        PathParameter op2PathParameter2 = (PathParameter) path1Get
                .getParameters().get(1);
        assertEquals("namePathVariable2", op2PathParameter2.getName());
        assertEquals("path", op2PathParameter2.getIn());
        assertEquals("string", op2PathParameter2.getType());
        assertEquals("description", op2PathParameter2.getDescription());
        // inRepresentation
        BodyParameter bodyParameter = (BodyParameter) path1Get.getParameters()
                .get(2);
        assertNotNull(bodyParameter);
        ModelImpl schemaBodyParameter = (ModelImpl) bodyParameter.getSchema();
        assertEquals("integer", schemaBodyParameter.getType());
        // queryParameter 1
        com.wordnik.swagger.models.parameters.QueryParameter op1QueryParameter1 = (com.wordnik.swagger.models.parameters.QueryParameter) path1Get
                .getParameters().get(3);
        assertEquals("query", op1QueryParameter1.getIn());
        assertEquals("nameQueryParameter1", op1QueryParameter1.getName());
        assertEquals(false, op1QueryParameter1.getRequired());
        assertEquals("integer", op1QueryParameter1.getType());
        assertEquals("description", op1QueryParameter1.getDescription());
        // queryParameter 2
        com.wordnik.swagger.models.parameters.QueryParameter op1QueryParameter2 = (com.wordnik.swagger.models.parameters.QueryParameter) path1Get
                .getParameters().get(4);
        assertEquals("query", op1QueryParameter2.getIn());
        assertEquals("nameQueryParameter2", op1QueryParameter2.getName());
        assertEquals(false, op1QueryParameter2.getRequired());
        assertEquals("string", op1QueryParameter2.getType());
        assertEquals("description", op1QueryParameter2.getDescription());
        // response 1
        com.wordnik.swagger.models.Response op1Response1 = path1Get
                .getResponses().get("200");
        assertNotNull(op1Response1);
        assertEquals("description", op1Response1.getDescription());
        assertTrue(op1Response1.getSchema() instanceof ArrayProperty);
        ArrayProperty op1Response1ArrayProperty = (ArrayProperty) op1Response1
                .getSchema();
        assertTrue(op1Response1ArrayProperty.getItems() instanceof IntegerProperty);
        // response 2
        com.wordnik.swagger.models.Response op1Response2 = path1Get
                .getResponses().get("300");
        assertNotNull(op1Response2);
        assertEquals("description", op1Response2.getDescription());
        assertTrue(op1Response2.getSchema() instanceof RefProperty);
        RefProperty op1Response2RefProperty = (RefProperty) op1Response2
                .getSchema();
        assertEquals("#/definitions/Entity2", op1Response2RefProperty.get$ref());
        // response 3
        com.wordnik.swagger.models.Response op1Response3 = path1Get
                .getResponses().get("400");
        assertNotNull(op1Response3);
        assertEquals("description", op1Response3.getDescription());
        assertTrue(op1Response3.getSchema() instanceof IntegerProperty);

        // resource 2
        Path path2 = swagger.getPath("resourcePath2");
        assertNotNull(path2);

        // representation 1
        ModelImpl model1 = (ModelImpl) swagger.getDefinitions().get(
                "com.restlet.nameRepresentation1");
        assertEquals("com.restlet.nameRepresentation1", model1.getName());
        // representation 1 : property 1
        assertTrue(model1.getProperties().get(
                "com.restlet.nameRepresentation1Property1") instanceof IntegerProperty);
        IntegerProperty model1Property1 = (IntegerProperty) model1
                .getProperties()
                .get("com.restlet.nameRepresentation1Property1");
        assertEquals("com.restlet.nameRepresentation1Property1",
                model1Property1.getName());
        assertEquals("description", model1Property1.getDescription());
        assertEquals(1.0d, model1Property1.getMinimum());
        assertEquals(2.0d, model1Property1.getMaximum());
        // representation 1 : property 2
        assertTrue(model1.getProperties().get(
                "com.restlet.nameRepresentation1Property2") instanceof ArrayProperty);
        ArrayProperty model1Property2 = (ArrayProperty) model1.getProperties()
                .get("com.restlet.nameRepresentation1Property2");
        assertEquals("com.restlet.nameRepresentation1Property2",
                model1Property2.getName());
        assertEquals("description", model1Property2.getDescription());
        assertTrue(model1Property2.getItems() instanceof StringProperty);
        // representation 1 : property 3
        assertTrue(model1.getProperties().get(
                "com.restlet.nameRepresentation1Property3") instanceof RefProperty);
        RefProperty model1Property3 = (RefProperty) model1.getProperties().get(
                "com.restlet.nameRepresentation1Property3");
        assertEquals("com.restlet.nameRepresentation1Property3",
                model1Property3.getName());
        assertEquals("description", model1Property3.getDescription());
        assertEquals("#/definitions/Entity", model1Property3.get$ref());

        // representation 2
        ModelImpl model2 = (ModelImpl) swagger.getDefinitions().get(
                "com.restlet.nameRepresentation2");
        assertEquals("com.restlet.nameRepresentation2", model2.getName());

    }

    private void compareSwaggerBeans(Swagger savedSwagger,
            Swagger translatedSwagger) {

        compareSwaggerMainAttributes(savedSwagger, translatedSwagger);

        // Info
        Info savedInfo = savedSwagger.getInfo();
        Info translatedInfo = translatedSwagger.getInfo();
        compareSwaggerInfo(savedInfo, translatedInfo);

        // Contact
        Contact savedContact = savedInfo.getContact();
        Contact translatedContact = translatedInfo.getContact();
        compareSwaggerContact(savedContact, translatedContact);

        // License
        com.wordnik.swagger.models.License savedLicense = savedInfo
                .getLicense();
        com.wordnik.swagger.models.License translatedLicense = translatedInfo
                .getLicense();
        assertEquals(savedLicense.getUrl(), translatedLicense.getUrl());
        assertEquals(savedLicense.getName(), translatedLicense.getName());

        // Models
        Map<String, Model> savedModels = savedSwagger.getDefinitions();
        Map<String, Model> translatedModels = translatedSwagger
                .getDefinitions();
        for (Entry<String, Model> entry : savedModels.entrySet()) {
            Model savedModel = savedModels.get(entry.getKey());
            Model translatedModel = translatedModels.get(entry.getKey());
            assertNotNull(savedModel);
            assertNotNull(translatedModel);
            compareSwaggerModels(savedModel, translatedModel);
        }

        // Paths
        Map<String, Path> savedPaths = savedSwagger.getPaths();
        Map<String, Path> translatedPaths = translatedSwagger.getPaths();
        for (Entry<String, Path> entry : savedPaths.entrySet()) {
            Path savedPath = savedPaths.get(entry.getKey());
            Path translatedPath = translatedPaths.get(entry.getKey());
            assertNotNull(savedPath);
            assertNotNull(translatedPath);
            compareSwaggerPaths(savedPath, translatedPath);
        }

    }

    private void compareSwaggerPaths(Path savedPath, Path translatedPath) {
        compareSwaggerOperations(savedPath.getGet(), translatedPath.getGet());
        compareSwaggerOperations(savedPath.getOptions(),
                translatedPath.getOptions());
        compareSwaggerOperations(savedPath.getPatch(),
                translatedPath.getPatch());
        compareSwaggerOperations(savedPath.getPost(), translatedPath.getPost());
        compareSwaggerOperations(savedPath.getPut(), translatedPath.getPut());
        compareSwaggerOperations(savedPath.getDelete(),
                translatedPath.getDelete());
    }

    private void compareSwaggerOperations(
            com.wordnik.swagger.models.Operation savedOperation,
            com.wordnik.swagger.models.Operation translatedOperation) {
        if (savedOperation != null || translatedOperation != null) {
            assertNotNull(savedOperation);
            assertNotNull(translatedOperation);
            assertEquals(savedOperation.getDescription(),
                    translatedOperation.getDescription());
            assertEquals(savedOperation.getOperationId(),
                    translatedOperation.getOperationId());
            assertEquals(savedOperation.getSummary(),
                    translatedOperation.getSummary());

            // Consumes
            List<String> savedConsumes = savedOperation.getConsumes();
            List<String> translatedConsumes = translatedOperation.getConsumes();
            compareStringLists(savedConsumes, translatedConsumes);

            // Produces
            List<String> savedProduces = savedOperation.getProduces();
            List<String> translatedProduces = translatedOperation.getProduces();
            compareStringLists(savedProduces, translatedProduces);

            // Parameters
            List<com.wordnik.swagger.models.parameters.Parameter> savedParameters = savedOperation
                    .getParameters();
            List<com.wordnik.swagger.models.parameters.Parameter> translatedParameters = translatedOperation
                    .getParameters();
            if (savedParameters != null || translatedParameters != null) {
                assertNotNull(savedParameters);
                assertNotNull(translatedParameters);
                for (com.wordnik.swagger.models.parameters.Parameter savedParameter : savedParameters) {
                    com.wordnik.swagger.models.parameters.Parameter translatedParameter = getParameterFromList(
                            translatedParameters, savedParameter.getName());
                    assertNotNull(savedParameter);
                    assertNotNull(translatedParameter);
                    compareSwaggerParameters(savedParameter,
                            translatedParameter);
                }
            }

            // Responses
            Map<String, com.wordnik.swagger.models.Response> savedResponses = savedOperation
                    .getResponses();
            Map<String, com.wordnik.swagger.models.Response> translatedResponses = translatedOperation
                    .getResponses();
            if (savedResponses != null || translatedResponses != null) {
                assertNotNull(savedResponses);
                assertNotNull(translatedResponses);
                for (Entry<String, com.wordnik.swagger.models.Response> entry : savedResponses
                        .entrySet()) {
                    com.wordnik.swagger.models.Response savedResponse = savedResponses
                            .get(entry.getKey());
                    com.wordnik.swagger.models.Response translatedResponse = translatedResponses
                            .get(entry.getKey());
                    assertNotNull(savedResponse);
                    assertNotNull(translatedResponse);
                    compareSwaggerResponses(savedResponse, translatedResponse);
                }
            }

            // TODO add security and schemes (not retrieved during conversion
            // yet)

            List<String> savedTags = savedOperation.getTags();
            List<String> translatedTags = translatedOperation.getTags();
            compareStringLists(savedTags, translatedTags);
        }
    }

    private void compareSwaggerResponses(
            com.wordnik.swagger.models.Response savedResponse,
            com.wordnik.swagger.models.Response translatedResponse) {
        assertEquals(savedResponse.getDescription(),
                translatedResponse.getDescription());
        compareMapOfStrings(savedResponse.getExamples(),
                translatedResponse.getExamples());
        compareSwaggerProperties(savedResponse.getSchema(),
                translatedResponse.getSchema());
    }

    private void compareSwaggerParameters(Parameter savedParameter,
            Parameter translatedParameter) {
        assertEquals(savedParameter.getDescription(),
                translatedParameter.getDescription());
        assertEquals(savedParameter.getIn(), translatedParameter.getIn());
        assertEquals(savedParameter.getName(), translatedParameter.getName());
        assertEquals(savedParameter.getRequired(),
                translatedParameter.getRequired());

    }

    private com.wordnik.swagger.models.parameters.Parameter getParameterFromList(
            List<com.wordnik.swagger.models.parameters.Parameter> list,
            String parameterName) {
        for (com.wordnik.swagger.models.parameters.Parameter parameter : list) {
            if (parameter.getName().equals(parameterName)) {
                return parameter;
            }
        }
        return null;
    }

    private void compareMapOfStrings(Map<String, String> savedMap,
            Map<String, String> translatedMap) {
        if (savedMap != null || translatedMap != null) {
            assertNotNull(savedMap);
            assertNotNull(translatedMap);
            for (Entry<String, String> entry : savedMap.entrySet()) {
                assertEquals(savedMap.get(entry.getKey()),
                        translatedMap.get(entry.getKey()));
            }
            for (Entry<String, String> entry : translatedMap.entrySet()) {
                assertEquals(savedMap.get(entry.getKey()),
                        translatedMap.get(entry.getKey()));
            }
        }
    }

    private void compareStringLists(List<String> savedList,
            List<String> translatedList) {
        for (String value : savedList) {
            assertTrue(translatedList.contains(value));
        }
        for (String value : translatedList) {
            assertTrue(savedList.contains(value));
        }
    }

    private void compareSwaggerMainAttributes(Swagger savedSwagger,
            Swagger translatedSwagger) {
        assertEquals(savedSwagger.getBasePath(),
                translatedSwagger.getBasePath());
        assertEquals(savedSwagger.getConsumes(),
                translatedSwagger.getConsumes());
        assertEquals(savedSwagger.getHost(), translatedSwagger.getHost());
        assertEquals(savedSwagger.getSwagger(), translatedSwagger.getSwagger());
        assertEquals(savedSwagger.getConsumes(),
                translatedSwagger.getConsumes());
    }

    private void compareSwaggerInfo(Info savedInfo, Info translatedInfo) {
        assertEquals(savedInfo.getDescription(),
                translatedInfo.getDescription());
        assertEquals(savedInfo.getTermsOfService(),
                translatedInfo.getTermsOfService());
        assertEquals(savedInfo.getTitle(), translatedInfo.getTitle());
        assertEquals(savedInfo.getVersion(), translatedInfo.getVersion());
    }

    private void compareSwaggerContact(Contact savedContact,
            Contact translatedContact) {
        assertEquals(savedContact.getEmail(), translatedContact.getEmail());
        assertEquals(savedContact.getName(), translatedContact.getName());
        assertEquals(savedContact.getUrl(), translatedContact.getUrl());
    }

    private void compareSwaggerModels(Model savedModel, Model translatedModel) {
        assertEquals(savedModel.getDescription(),
                translatedModel.getDescription());
        assertEquals(savedModel.getExample(), translatedModel.getExample());
        Map<String, com.wordnik.swagger.models.properties.Property> savedProperties = savedModel
                .getProperties();
        Map<String, com.wordnik.swagger.models.properties.Property> translatedProperties = translatedModel
                .getProperties();
        for (Entry<String, com.wordnik.swagger.models.properties.Property> entry : savedProperties
                .entrySet()) {
            com.wordnik.swagger.models.properties.Property savedProperty = savedProperties
                    .get(entry.getKey());
            com.wordnik.swagger.models.properties.Property translatedProperty = translatedProperties
                    .get(entry.getKey());
            assertNotNull(savedProperty);
            assertNotNull(translatedProperty);
            compareSwaggerProperties(savedProperty, translatedProperty);
        }
    }

    private void compareSwaggerProperties(
            com.wordnik.swagger.models.properties.Property savedProperty,
            com.wordnik.swagger.models.properties.Property translatedProperty) {
        if (savedProperty != null || translatedProperty != null) {
            assertNotNull(savedProperty);
            assertNotNull(translatedProperty);
            assertEquals(savedProperty.getExample(),
                    translatedProperty.getExample());
            assertEquals(savedProperty.getDescription(),
                    translatedProperty.getDescription());
            assertEquals(savedProperty.getFormat(),
                    translatedProperty.getFormat());
            // TODO fails, investigate (seems like name is filled during
            // translation but not during deserialization
            // assertEquals(savedProperty.getName(),
            // translatedProperty.getName());
            assertEquals(savedProperty.getRequired(),
                    translatedProperty.getRequired());
            assertEquals(savedProperty.getTitle(),
                    translatedProperty.getTitle());
            assertEquals(savedProperty.getType(), translatedProperty.getType());
            assertEquals(savedProperty.getPosition(),
                    translatedProperty.getPosition());
        }
    }

    public void testGetSwagger2() throws IOException {
        Definition savedDefinition = new JacksonRepresentation<Definition>(
                new FileRepresentation(getClass().getResource("refImpl.rwadef")
                        .getFile(), MediaType.APPLICATION_JSON),
                Definition.class).getObject();

        Swagger translatedSwagger = Swagger2Translator
                .getSwagger(savedDefinition);

        URL refImpl = getClass().getResource("refImpl.swagger");
        Swagger savedSwagger = new SwaggerLoader()
                .read(refImpl.getFile());

        compareSwaggerBeans(savedSwagger, translatedSwagger);
    }

}
