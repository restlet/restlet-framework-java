package org.restlet.test.ext.apispark.conversion;

import java.util.List;

import org.junit.Assert;
import org.restlet.ext.apispark.internal.model.Contract;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;
import org.restlet.ext.apispark.internal.model.Header;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.PayLoad;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;
import org.restlet.ext.apispark.internal.model.Section;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class DefinitionComparator {

    public static void compareDefinitions(Definition savedDefinition,
            Definition translatedDefinition) {

        Assert.assertEquals(savedDefinition.getAttribution(), translatedDefinition.getAttribution());
        Assert.assertEquals(savedDefinition.getSpecVersion(), translatedDefinition.getSpecVersion());
        Assert.assertEquals(savedDefinition.getTermsOfService(), translatedDefinition.getTermsOfService());
        Assert.assertEquals(savedDefinition.getVersion(), translatedDefinition.getVersion());

        // Contact
        org.restlet.ext.apispark.internal.model.Contact savedContact = savedDefinition.getContact();
        org.restlet.ext.apispark.internal.model.Contact translatedContact = translatedDefinition.getContact();

        if (!checkOnlyOneNull(savedContact, translatedContact, "contact")
                && !areBothNull(savedContact, translatedContact)) {
            Assert.assertEquals(savedContact.getEmail(), translatedContact.getEmail());
            Assert.assertEquals(savedContact.getName(), translatedContact.getName());
            Assert.assertEquals(savedContact.getUrl(), translatedContact.getUrl());
        }

        // License
        org.restlet.ext.apispark.internal.model.License savedLicense = savedDefinition.getLicense();
        org.restlet.ext.apispark.internal.model.License translatedLicense = translatedDefinition.getLicense();

        if (!checkOnlyOneNull(savedLicense, translatedLicense, "license")
                && !areBothNull(savedLicense, translatedLicense)) {
            Assert.assertEquals(savedLicense.getName(), translatedLicense.getName());
            Assert.assertEquals(savedLicense.getUrl(), translatedLicense.getUrl());
        }

        compareStringLists(savedDefinition.getKeywords(), translatedDefinition.getKeywords());

        compareEndpoints(savedDefinition, translatedDefinition);
        compareContracts(savedDefinition.getContract(), translatedDefinition.getContract());
    }

    private static boolean checkOnlyOneNull(Object saved, Object translated, String objectType) {
        if (saved != null && translated == null
                || saved == null && translated != null) {
            Assert.fail("One " + objectType + " is null:\nsaved " + objectType + ":\n" + saved +
                    "\n:translated" + objectType + ":\n" + translated);
            return true;

        } else {
            return false;
        }
    }

    private static boolean areBothNull(Object saved, Object translated) {
        return saved == null && translated == null;
    }

    private static void compareEndpoints(Definition savedDefinition, Definition translatedDefinition) {
        if (assertBothNull(savedDefinition.getEndpoints(), translatedDefinition.getEndpoints())) {
            return;
        }

        ImmutableMap<String, Endpoint> savedEndpoints = Maps.uniqueIndex(
                savedDefinition.getEndpoints(),
                new Function<Endpoint, String>() {
                    public String apply(Endpoint endpoint) {
                        return endpoint.computeUrl();
                    }
                });
        ImmutableMap<String, Endpoint> translatedEndpoints = Maps.uniqueIndex(
                translatedDefinition.getEndpoints(),
                new Function<Endpoint, String>() {
                    public String apply(Endpoint endpoint) {
                        return endpoint.computeUrl();
                    }
                });
        Assert.assertEquals(savedEndpoints.size(), translatedEndpoints.size());
        for (String key : savedEndpoints.keySet()) {
            Endpoint savedEndpoint = savedEndpoints.get(key);
            Endpoint translatedEndpoint = translatedEndpoints.get(key);
            Assert.assertNotNull(savedEndpoint);
            Assert.assertNotNull(translatedEndpoint);
            Assert.assertEquals(savedEndpoint.getAuthenticationProtocol(), translatedEndpoint.getAuthenticationProtocol());
            Assert.assertEquals(savedEndpoint.getBasePath(), translatedEndpoint.getBasePath());
            Assert.assertEquals(savedEndpoint.getDomain(), translatedEndpoint.getDomain());
            Assert.assertEquals(savedEndpoint.getProtocol(), translatedEndpoint.getProtocol());
            Assert.assertEquals(savedEndpoint.getPort(), translatedEndpoint.getPort());
        }
    }

    private static void compareContracts(Contract savedContract, Contract translatedContract) {
        Assert.assertEquals(savedContract.getDescription(), translatedContract.getDescription());
        Assert.assertEquals(savedContract.getName(), translatedContract.getName());

        compareSections(savedContract, translatedContract);
        compareRepresentations(savedContract, translatedContract);
        compareResources(savedContract, translatedContract);
    }

    private static void compareResources(Contract savedContract, Contract translatedContract) {
        if (assertBothNull(savedContract.getResources(), translatedContract.getResources())) {
            return;
        }

        ImmutableMap<String, Resource> savedResources = Maps.uniqueIndex(
                savedContract.getResources(),
                new Function<Resource, String>() {
                    public String apply(Resource resource) {
                        return resource.getResourcePath();
                    }
                });
        ImmutableMap<String, Resource> translatedResources = Maps.uniqueIndex(
                translatedContract.getResources(),
                new Function<Resource, String>() {
                    public String apply(Resource resource) {
                        return resource.getResourcePath();
                    }
                });

        Assert.assertEquals(savedResources.size(), translatedResources.size());
        for (String key : savedResources.keySet()) {
            Resource savedResource = savedResources.get(key);
            Resource translatedResource = translatedResources.get(key);
            Assert.assertNotNull(savedResource);
            Assert.assertNotNull(translatedResource);

            Assert.assertEquals(savedResource.getDescription(), translatedResource.getDescription());
            Assert.assertEquals(savedResource.getAuthenticationProtocol(),
                    translatedResource.getAuthenticationProtocol());
            Assert.assertEquals(savedResource.getName(), translatedResource.getName());
            compareStringLists(savedResource.getSections(), translatedResource.getSections());

            comparePathVariables(savedResource, translatedResource);
            compareOperations(savedResource, translatedResource);
        }
    }

    private static void comparePathVariables(Resource savedResource, Resource translatedResource) {
        if (assertBothNull(savedResource.getPathVariables(), translatedResource.getPathVariables())) {
            return;
        }

        ImmutableMap<String, PathVariable> savedPathVariables = Maps.uniqueIndex(
                savedResource.getPathVariables(),
                new Function<PathVariable, String>() {
                    public String apply(PathVariable pathVariable) {
                        return pathVariable.getName();
                    }
                });
        ImmutableMap<String, PathVariable> translatedPathVariables = Maps.uniqueIndex(
                translatedResource.getPathVariables(),
                new Function<PathVariable, String>() {
                    public String apply(PathVariable pathVariable) {
                        return pathVariable.getName();
                    }
                });

        Assert.assertEquals(savedPathVariables.size(), translatedPathVariables.size());
        for (String key1 : savedPathVariables.keySet()) {
            PathVariable savedPathVariable = savedPathVariables.get(key1);
            PathVariable translatedPathVariable = translatedPathVariables.get(key1);
            Assert.assertNotNull(savedPathVariable);
            Assert.assertNotNull(translatedPathVariable);

            Assert.assertEquals(savedPathVariable.getDescription(), translatedPathVariable.getDescription());
            Assert.assertEquals(savedPathVariable.getExample(), translatedPathVariable.getExample());
            Assert.assertEquals(savedPathVariable.getType(), translatedPathVariable.getType());
            Assert.assertEquals(savedPathVariable.isRequired(), translatedPathVariable.isRequired());
        }
    }

    private static void compareOperations(Resource savedResource, Resource translatedResource) {
        if (assertBothNull(savedResource.getOperations(), translatedResource.getOperations())) {
            return;
        }

        ImmutableMap<String, Operation> savedOperations = Maps.uniqueIndex(
                savedResource.getOperations(),
                new Function<Operation, String>() {
                    public String apply(Operation operation) {
                        return operation.getName();
                    }
                });
        ImmutableMap<String, Operation> translatedOperations = Maps.uniqueIndex(
                translatedResource.getOperations(),
                new Function<Operation, String>() {
                    public String apply(Operation operation) {
                        return operation.getName();
                    }
                });

        Assert.assertEquals(savedOperations.size(), translatedOperations.size());
        for (String key : savedOperations.keySet()) {
            Operation savedOperation = savedOperations.get(key);
            Operation translatedOperation = translatedOperations.get(key);
            Assert.assertNotNull(savedOperation);
            Assert.assertNotNull(translatedOperation);

            Assert.assertEquals(savedOperation.getDescription(), translatedOperation.getDescription());
            Assert.assertEquals(savedOperation.getMethod(), translatedOperation.getMethod());

            compareHeaders(savedOperation.getHeaders(), translatedOperation.getHeaders());
            compareQueryParameters(savedOperation, translatedOperation);
            comparePayloads(savedOperation.getInputPayLoad(), translatedOperation.getInputPayLoad());
            compareResponses(savedOperation, translatedOperation);

            compareStringLists(savedOperation.getProduces(), translatedOperation.getProduces());
            compareStringLists(savedOperation.getConsumes(), translatedOperation.getConsumes());
        }
    }

    private static void compareQueryParameters(Operation savedOperation, Operation translatedOperation) {
        if (assertBothNull(savedOperation.getQueryParameters(), translatedOperation.getQueryParameters())) {
            return;
        }

        ImmutableMap<String, QueryParameter> savedQueryParameters = Maps.uniqueIndex(
                savedOperation.getQueryParameters(),
                new Function<QueryParameter, String>() {
                    public String apply(QueryParameter header) {
                        return header.getName();
                    }
                });
        ImmutableMap<String, QueryParameter> translatedQueryParameters = Maps.uniqueIndex(
                translatedOperation.getQueryParameters(),
                new Function<QueryParameter, String>() {
                    public String apply(QueryParameter header) {
                        return header.getName();
                    }
                });

        Assert.assertEquals(savedQueryParameters.size(), translatedQueryParameters.size());
        for (String key : savedQueryParameters.keySet()) {
            QueryParameter savedQueryParameter = savedQueryParameters.get(key);
            QueryParameter translatedQueryParameter = translatedQueryParameters.get(key);
            Assert.assertNotNull(savedQueryParameter);
            Assert.assertNotNull(translatedQueryParameter);

            Assert.assertEquals(savedQueryParameter.getDefaultValue(), translatedQueryParameter.getDefaultValue());
            Assert.assertEquals(savedQueryParameter.getDescription(), translatedQueryParameter.getDescription());
            Assert.assertEquals(savedQueryParameter.getType(), translatedQueryParameter.getType());
            Assert.assertEquals(savedQueryParameter.getExample(), translatedQueryParameter.getExample());
            Assert.assertEquals(savedQueryParameter.getSeparator(), translatedQueryParameter.getSeparator());
            Assert.assertEquals(savedQueryParameter.isRequired(), translatedQueryParameter.isRequired());
            Assert.assertEquals(savedQueryParameter.isAllowMultiple(), translatedQueryParameter.isAllowMultiple());
            compareStringLists(savedQueryParameter.getEnumeration(), translatedQueryParameter.getEnumeration());
        }
    }

    private static void compareRepresentations(Contract savedContract, Contract translatedContract) {
        if (assertBothNull(savedContract.getRepresentations(), translatedContract.getRepresentations())) {
            return;
        }

        ImmutableMap<String, Representation> savedRepresentations = Maps.uniqueIndex(
                savedContract.getRepresentations(),
                new Function<Representation, String>() {
                    public String apply(Representation representation) {
                        return representation.getName();
                    }
                });
        ImmutableMap<String, Representation> translatedRepresentations = Maps.uniqueIndex(
                translatedContract.getRepresentations(),
                new Function<Representation, String>() {
                    public String apply(Representation representation) {
                        return representation.getName();
                    }
                });

        Assert.assertEquals(savedRepresentations.size(), translatedRepresentations.size());
        for (String key : savedRepresentations.keySet()) {
            Representation savedRepresentation = savedRepresentations.get(key);
            Representation translatedRepresentation = translatedRepresentations.get(key);
            Assert.assertNotNull(savedRepresentation);
            Assert.assertNotNull(translatedRepresentation);
            Assert.assertEquals(savedRepresentation.getDescription(), translatedRepresentation.getDescription());
            Assert.assertEquals(savedRepresentation.getExtendedType(), translatedRepresentation.getExtendedType());

            compareStringLists(savedRepresentation.getSections(), translatedRepresentation.getSections());
            compareProperties(savedRepresentation.getProperties(), translatedRepresentation.getProperties());
        }
    }

    private static void compareSections(Contract savedContract, Contract translatedContract) {
        if (assertBothNull(savedContract.getSections(), translatedContract.getSections())) {
            return;
        }

        ImmutableMap<String, Section> savedSections = Maps.uniqueIndex(savedContract.getSections(),
                new Function<Section, String>() {
                    public String apply(Section section) {
                        return section.getName();
                    }
                });
        ImmutableMap<String, Section> translatedSections = Maps.uniqueIndex(translatedContract.getSections(),
                new Function<Section, String>() {
                    public String apply(Section section) {
                        return section.getName();
                    }
                });

        Assert.assertEquals(savedSections.size(), translatedSections.size());
        for (String key : savedSections.keySet()) {
            Section savedSection = savedSections.get(key);
            Section translatedSection = translatedSections.get(key);
            Assert.assertNotNull(savedSection);
            Assert.assertNotNull(translatedSection);
            Assert.assertEquals(savedSection.getDescription(), translatedSection.getDescription());
        }
    }

    private static void compareProperties(List<Property> savedPropertiesList,
            List<Property> translatedPropertiesList) {
        if (assertBothNull(savedPropertiesList, translatedPropertiesList)) {
            return;
        }

        ImmutableMap<String, Property> savedProperties = Maps.uniqueIndex(
                savedPropertiesList,
                new Function<Property, String>() {
                    public String apply(Property property) {
                        return property.getName();
                    }
                });
        ImmutableMap<String, Property> translatedProperties = Maps.uniqueIndex(
                translatedPropertiesList,
                new Function<Property, String>() {
                    public String apply(Property property) {
                        return property.getName();
                    }
                });
        
        Assert.assertEquals(savedProperties.size(), translatedProperties.size());
        for (String key : savedProperties.keySet()) {
            Property savedProperty = savedProperties.get(key);
            Property translatedProperty = translatedProperties.get(key);
            Assert.assertNotNull(savedProperty);
            Assert.assertNotNull(translatedProperty);

            Assert.assertEquals(savedProperty.getDefaultValue(), translatedProperty.getDefaultValue());
            Assert.assertEquals(savedProperty.getDescription(), translatedProperty.getDescription());
            Assert.assertEquals(savedProperty.getExample(), translatedProperty.getExample());
            Assert.assertEquals(savedProperty.getMax(), translatedProperty.getMax());
            Assert.assertEquals(savedProperty.getMin(), translatedProperty.getMin());
            Assert.assertEquals(savedProperty.getType(), translatedProperty.getType());
            Assert.assertEquals(savedProperty.getMaxOccurs(), translatedProperty.getMaxOccurs());
            Assert.assertEquals(savedProperty.getMinOccurs(), translatedProperty.getMinOccurs());

            compareProperties(savedProperty.getProperties(), translatedProperty.getProperties());
            compareStringLists(savedProperty.getEnumeration(), translatedProperty.getEnumeration());
        }
    }

    private static void compareResponses(Operation savedOperation, Operation translatedOperation) {
        if (assertBothNull(savedOperation.getResponses(), translatedOperation.getResponses())) {
            return;
        }

        ImmutableMap<Integer, Response> savedResponses = Maps.uniqueIndex(
                savedOperation.getResponses(),
                new Function<Response, Integer>() {
                    public Integer apply(Response response) {
                        return response.getCode();
                    }
                });
        ImmutableMap<Integer, Response> translatedResponses = Maps.uniqueIndex(
                translatedOperation.getResponses(),
                new Function<Response, Integer>() {
                    public Integer apply(Response response) {
                        return response.getCode();
                    }
                });

        Assert.assertEquals(savedResponses.size(), translatedResponses.size());
        for (Integer key : savedResponses.keySet()) {
            Response savedResponse = savedResponses.get(key);
            Response translatedResponse = translatedResponses.get(key);
            Assert.assertNotNull(savedResponse);
            Assert.assertNotNull(translatedResponse);

            Assert.assertEquals(savedResponse.getDescription(), translatedResponse.getDescription());

            // both don't exist in Swagger => can't be retrieved
            // assertEquals(savedResponse.getMessage(), translatedResponse.getMessage());
            // assertEquals(savedResponse.getName(), translatedResponse.getName());

            compareHeaders(savedResponse.getHeaders(), translatedResponse.getHeaders());
            comparePayloads(savedResponse.getOutputPayLoad(), translatedResponse.getOutputPayLoad());
        }
    }

    private static void comparePayloads(PayLoad savedPayload, PayLoad translatedPayload) {
        if (assertBothNull(savedPayload, translatedPayload)) {
            return;
        }

        Assert.assertEquals(savedPayload.getDescription(), translatedPayload.getDescription());
        Assert.assertEquals(savedPayload.getType(), translatedPayload.getType());
        Assert.assertEquals(savedPayload.isArray(), translatedPayload.isArray());
    }

    private static void compareHeaders(List<Header> savedHeadersList, List<Header> translatedHeadersList) {
        if (assertBothNull(savedHeadersList, translatedHeadersList)) {
            return;
        }

        ImmutableMap<String, Header> savedHeaders = Maps.uniqueIndex(
                savedHeadersList,
                new Function<Header, String>() {
                    public String apply(Header header) {
                        return header.getName();
                    }
                });
        ImmutableMap<String, Header> translatedHeaders = Maps.uniqueIndex(
                translatedHeadersList,
                new Function<Header, String>() {
                    public String apply(Header header) {
                        return header.getName();
                    }
                });

        Assert.assertEquals(savedHeaders.size(), translatedHeaders.size());
        for (String key : savedHeaders.keySet()) {
            Header savedHeader = savedHeaders.get(key);
            Header translatedHeader = translatedHeaders.get(key);
            Assert.assertNotNull(savedHeader);
            Assert.assertNotNull(translatedHeader);

            Assert.assertEquals(savedHeader.getDefaultValue(), translatedHeader.getDefaultValue());
            Assert.assertEquals(savedHeader.getDescription(), translatedHeader.getDescription());
            Assert.assertEquals(savedHeader.getType(), translatedHeader.getType());
            Assert.assertEquals(savedHeader.isRequired(), translatedHeader.isRequired());
            Assert.assertEquals(savedHeader.isAllowMultiple(), translatedHeader.isAllowMultiple());

        }
    }

    public static void compareStringLists(List<String> savedList,
            List<String> translatedList) {
        if (assertBothNull(savedList, translatedList)) {
            return;
        }

        for (String value : savedList) {
            Assert.assertTrue(translatedList.contains(value));
        }

        for (String value : translatedList) {
            Assert.assertTrue(savedList.contains(value));
        }
    }

    /**
     * Asserts that the given objects are both null. Returns true if it is the case, false otherwise.
     * Fails if one is null and not the other.
     * 
     * @param savedObject
     *            The object from the saved definition.
     * @param translatedObject
     *            The object from the translated definition.
     * @return True if both the objects are null, false otherwise.
     */
    private static boolean assertBothNull(Object savedObject, Object translatedObject) {
        if (savedObject == null || translatedObject == null) {
            Assert.assertNull(savedObject);
            Assert.assertNull(translatedObject);
            return true;
        }
        return false;
    }
}
