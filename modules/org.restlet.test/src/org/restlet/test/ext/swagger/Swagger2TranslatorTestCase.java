package org.restlet.test.ext.swagger;

import java.util.ArrayList;
import java.util.List;

import org.restlet.ext.apispark.internal.conversion.Swagger2Translator;
import org.restlet.ext.apispark.internal.model.Contract;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;
import org.restlet.ext.apispark.internal.model.Entity;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;
import org.restlet.test.RestletTestCase;

import com.wordnik.swagger.models.Contact;
import com.wordnik.swagger.models.Info;
import com.wordnik.swagger.models.License;
import com.wordnik.swagger.models.ModelImpl;
import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.Swagger;
import com.wordnik.swagger.models.parameters.BodyParameter;
import com.wordnik.swagger.models.parameters.PathParameter;
import com.wordnik.swagger.models.properties.ArrayProperty;
import com.wordnik.swagger.models.properties.IntegerProperty;
import com.wordnik.swagger.models.properties.RefProperty;
import com.wordnik.swagger.models.properties.StringProperty;

public class Swagger2TranslatorTestCase extends RestletTestCase {

	private Swagger2Translator swagger2Translator = new Swagger2Translator();

	/**
	 * Conversion Rwadef -> Swagger 2.0.
	 */
	public void testGetSwagger() {
		// Given

		// definition
		Definition definition = new Definition();
		definition.setVersion("version");
		definition.setContact("contact");
		definition.setLicense("license");

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
		pathVariable1.setType("String");
		pathVariable1.setDescription("description");

		// resource 1 : path variable 2
		PathVariable pathVariable2 = new PathVariable();
		resource1.getPathVariables().add(pathVariable2);
		pathVariable2.setName("namePathVariable2");
		pathVariable2.setType("String");
		pathVariable2.setDescription("description");

		// resource 1 : operation 1 : inRepresentation
		Entity inRepr = new Entity();
		operation1.setInRepresentation(inRepr);
		inRepr.setType("Integer");

		// resource 1 : operation 1 : queryParameter 1
		QueryParameter queryParameter1 = new QueryParameter();
		operation1.getQueryParameters().add(queryParameter1);
		queryParameter1.setName("nameQueryParameter1");
		queryParameter1.setType("Integer");
		queryParameter1.setDescription("description");

		// resource 1 : operation 1 : queryParameter 2
		QueryParameter queryParameter2 = new QueryParameter();
		operation1.getQueryParameters().add(queryParameter2);
		queryParameter2.setName("nameQueryParameter2");
		queryParameter2.setType("String");
		queryParameter2.setDescription("description");

		// resource 1 : operation 1 : response 1
		Response response1 = new Response();
		operation1.getResponses().add(response1);
		response1.setCode(200);
		response1.setDescription("description");
		Entity response1Entity = new Entity();
		response1.setEntity(response1Entity);
		response1Entity.setArray(true);
		response1Entity.setType("Integer");
		assertTrue(response1.getEntity().isArray());

		// resource 1 : operation 1 : response 2
		Response response2 = new Response();
		operation1.getResponses().add(response2);
		response2.setCode(300);
		response2.setDescription("description");
		Entity response2Entity = new Entity();
		response2.setEntity(response2Entity);
		response2Entity.setArray(false);
		response2Entity.setType("Entity2");
		assertFalse(response2.getEntity().isArray());

		// resource 1 : operation 1 : response 3
		Response response3 = new Response();
		operation1.getResponses().add(response3);
		response3.setCode(400);
		response3.setDescription("description");
		Entity response3Entity = new Entity();
		response3.setEntity(response3Entity);
		response3Entity.setArray(false);
		response3Entity.setType("Integer");
		assertFalse(response3.getEntity().isArray());

		// resource 2
		Resource resource2 = new Resource();
		contract.getResources().add(resource2);
		resource2.setResourcePath("resourcePath2");

		// representation 1
		Representation representation1 = new Representation();
		definition.getContract().getRepresentations().add(representation1);
		representation1.setName("nameRepresentation1");

		// representation 1 : property 1
		Property representation1Property1 = new Property();
		representation1.getProperties().add(representation1Property1);
		representation1Property1.setName("nameRepresentation1Property1");
		representation1Property1.setType("Integer");
		representation1Property1.setMin("1.0");
		representation1Property1.setMax("2.0");
		representation1Property1.setDescription("description");

		// representation 1 : property 2
		Property representation1Property2 = new Property();
		representation1.getProperties().add(representation1Property2);
		representation1Property2.setName("nameRepresentation1Property2");
		representation1Property2.setMaxOccurs(-1);
		representation1Property2.setType("String");
		representation1Property2.setDescription("description");

		// representation 1 : property 3
		Property representation1Property3 = new Property();
		representation1.getProperties().add(representation1Property3);
		representation1Property3.setName("nameRepresentation1Property3");
		representation1Property3.setType("Entity");
		representation1Property3.setDescription("description");

		// representation 2
		Representation representation2 = new Representation();
		definition.getContract().getRepresentations().add(representation2);
		representation2.setName("nameRepresentation2");


		// When
		Swagger swagger = swagger2Translator.getSwaggerFromRoadef(definition);


		// Then
		assertEquals(Float.valueOf(2.0f), swagger.getSwagger());

		Info infoSwagger = swagger.getInfo();
		assertEquals("version", infoSwagger.getVersion());
		Contact contactSwagger = infoSwagger.getContact();
		assertEquals("contact",contactSwagger.getName());
        //TODO uncomment license assertion when translator will handle it
		License license = infoSwagger.getLicense();
		assertEquals("license", license.getUrl());
		assertEquals("contract.name", infoSwagger.getTitle());
		assertEquals("contract.description", infoSwagger.getDescription());
		assertEquals("/basePath", swagger.getBasePath());
		// resource 1
		Path path1 = swagger.getPath("resourcePath1");
		assertNotNull(path1);
		com.wordnik.swagger.models.Operation path1Get = path1.getGet();
		assertNotNull(path1Get);
		assertEquals("nameoperation1",path1Get.getOperationId());
		assertEquals("description", path1Get.getDescription());
		assertEquals(2,path1Get.getProduces().size());
		assertEquals("produce 1", path1Get.getProduces().get(0));
		assertEquals("produce 2", path1Get.getProduces().get(1));
		assertEquals(2,path1Get.getConsumes().size());
		assertEquals("consume 1", path1Get.getConsumes().get(0));
		assertEquals("consume 2", path1Get.getConsumes().get(1));
		// path variable 1
		PathParameter op1PathParameter1 = (PathParameter) path1Get.getParameters().get(0);
		assertEquals("namePathVariable1", op1PathParameter1.getName());
		assertEquals("path",op1PathParameter1.getIn());
		assertEquals("string", op1PathParameter1.getType());
		assertEquals("description", op1PathParameter1.getDescription());
		// path variable 2
		PathParameter op2PathParameter2 = (PathParameter) path1Get.getParameters().get(1);
		assertEquals("namePathVariable2", op2PathParameter2.getName());
		assertEquals("path",op2PathParameter2.getIn());
		assertEquals("string", op2PathParameter2.getType());
		assertEquals("description", op2PathParameter2.getDescription());
		// inRepresentation
		BodyParameter bodyParameter = (BodyParameter) path1Get.getParameters().get(2);
		assertNotNull(bodyParameter);
		ModelImpl schemaBodyParameter = (ModelImpl) bodyParameter.getSchema();
		assertEquals("int", schemaBodyParameter.getType());
		// queryParameter 1
		com.wordnik.swagger.models.parameters.QueryParameter op1QueryParameter1 =
				(com.wordnik.swagger.models.parameters.QueryParameter) path1Get.getParameters().get(3);
		assertEquals("query",op1QueryParameter1.getIn());
		assertEquals("nameQueryParameter1", op1QueryParameter1.getName());
		assertEquals(false, op1QueryParameter1.getRequired());
		assertEquals("int", op1QueryParameter1.getType());
		assertEquals("description", op1QueryParameter1.getDescription());
		// queryParameter 2
		com.wordnik.swagger.models.parameters.QueryParameter op1QueryParameter2 =
				(com.wordnik.swagger.models.parameters.QueryParameter) path1Get.getParameters().get(4);
		assertEquals("query",op1QueryParameter2.getIn());
		assertEquals("nameQueryParameter2", op1QueryParameter2.getName());
		assertEquals(false, op1QueryParameter2.getRequired());
		assertEquals("string", op1QueryParameter2.getType());
		assertEquals("description", op1QueryParameter2.getDescription());
		// response 1
		com.wordnik.swagger.models.Response op1Response1 = path1Get.getResponses().get("200");
		assertNotNull(op1Response1);
		assertEquals("description", op1Response1.getDescription());
		assertTrue(op1Response1.getSchema() instanceof ArrayProperty);
		ArrayProperty op1Response1ArrayProperty = (ArrayProperty) op1Response1.getSchema();
		assertTrue(op1Response1ArrayProperty.getItems() instanceof IntegerProperty);
		// response 2
		com.wordnik.swagger.models.Response op1Response2 = path1Get.getResponses().get("300");
		assertNotNull(op1Response2);
		assertEquals("description", op1Response2.getDescription());
		assertTrue(op1Response2.getSchema() instanceof RefProperty);
		RefProperty op1Response2RefProperty = (RefProperty) op1Response2.getSchema();
		assertEquals("Entity2",op1Response2RefProperty.get$ref());
		// response 3
		com.wordnik.swagger.models.Response op1Response3 = path1Get.getResponses().get("400");
		assertNotNull(op1Response3);
		assertEquals("description", op1Response3.getDescription());
		assertTrue(op1Response3.getSchema() instanceof IntegerProperty);

		// resource 2
		Path path2 = swagger.getPath("resourcePath2");
		assertNotNull(path2);

		// representation 1
		ModelImpl model1 = (ModelImpl) swagger.getDefinitions().get("nameRepresentation1");
		assertEquals("nameRepresentation1", model1.getName());
		// representation 1 : property 1
		assertTrue(model1.getProperties().get("nameRepresentation1Property1") instanceof IntegerProperty);
		IntegerProperty model1Property1 = (IntegerProperty) model1.getProperties().get("nameRepresentation1Property1");
		assertEquals("nameRepresentation1Property1", model1Property1.getName());
		assertEquals("description", model1Property1.getDescription());
		assertEquals(1.0d, model1Property1.getMinimum());
		assertEquals(2.0d, model1Property1.getMaximum());
		// representation 1 : property 2
		assertTrue(model1.getProperties().get("nameRepresentation1Property2") instanceof ArrayProperty);
		ArrayProperty model1Property2 = (ArrayProperty) model1.getProperties().get("nameRepresentation1Property2");
		assertEquals("nameRepresentation1Property2", model1Property2.getName());
		assertEquals("description", model1Property2.getDescription());
		assertTrue(model1Property2.getItems() instanceof StringProperty);
		// representation 1 : property 3
		assertTrue(model1.getProperties().get("nameRepresentation1Property3") instanceof RefProperty);
		RefProperty model1Property3 = (RefProperty) model1.getProperties().get("nameRepresentation1Property3");
		assertEquals("nameRepresentation1Property3", model1Property3.getName());
		assertEquals("description", model1Property3.getDescription());
		assertEquals("Entity", model1Property3.get$ref());

		// representation 2
		ModelImpl model2 = (ModelImpl) swagger.getDefinitions().get("nameRepresentation2");
		assertEquals("nameRepresentation2", model2.getName());

	}

}
