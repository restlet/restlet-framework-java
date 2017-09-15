/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
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

package org.restlet.ext.odata.internal.edm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.ext.odata.Service;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Used to parse a metadata descriptor of a given OData service and generate the
 * associated object's tree.
 * 
 * @author Thierry Boileau
 */
public class MetadataReader extends DefaultHandler {

    /** The list of defined states of this parser. */
    private enum State {
        ASSOCIATION, ASSOCIATION_END, ASSOCIATION_SET, ASSOCIATION_SET_END, COMPLEX_TYPE, COMPLEX_TYPE_PROPERTY, DOCUMENTATION, ENTITY_CONTAINER, ENTITY_SET, ENTITY_TYPE, ENTITY_TYPE_KEY, ENTITY_TYPE_PROPERTY, FUNCTION, FUNCTION_IMPORT, NAVIGATION_PROPERTY, NONE, ON_DELETE, PARAMETER, REFERENTIAL_CONSTRAINT, SCHEMA, USING
    }

    /** List of possible values for the blob reference member. */
    private final String[] blobEditRefValues = { "blobEditReference",
            "blobEditReferenceValue" };

    /** List of possible values for the blob reference member. */
    private final String[] blobRefValues = { "blobReference",
            "blobReferenceValue" };

    /** The current association. */
    private Association currentAssociation;

    /** The current association set. */
    private AssociationSet currentAssociationSet;

    /** The current complex type. */
    private ComplexType currentComplexType;

    /** The current entity container. */
    private EntityContainer currentEntityContainer;

    /** The current entity type. */
    private EntityType currentEntityType;

    /** The current functionn import. */
    private FunctionImport currentFunctionImport;

    /** The metadata objet to update. */
    private Metadata currentMetadata;

    /** The current schema. */
    private Schema currentSchema;

    /** The registered collection of associations. */
    private Map<String, NamedObject> registeredAssociations;

    /** The registered collection of complex types. */
    private Map<String, NamedObject> registeredComplexTypes;

    /** The registered collection of entity containers. */
    private Map<String, EntityContainer> registeredContainers;

    /** The registered collection of entity sets. */
    private Map<String, NamedObject> registeredEntitySets;

    /** The registered collection of entity types. */
    private Map<String, NamedObject> registeredEntityTypes;

    /** The registered collection of namespaces. */
    private List<Namespace> registeredNamespaces;

    /** The current heap of states. */
    private List<State> states;

    /**
     * Constructor.
     * 
     * @param feed
     *            The feed object to update during the parsing.
     */
    public MetadataReader(Metadata metadata) {
        this.states = new ArrayList<State>();
        pushState(State.NONE);
        this.currentMetadata = metadata;
    }

    /**
     * Pick up the first method name among a given list of proposed names.
     * Returns null if the proposed values are already in the given entity type.
     * 
     * @param type
     *            The entity type to check.
     * @param values
     *            The list of proposed values.
     * @return A method name.
     */
    private String chooseAttributeName(EntityType type, String[] values) {
        String result = null;
        int index = 0;
        // Check that one of the possible names is not already set.
        for (int i = 0; i < type.getProperties().size()
                && (index < values.length); i++) {
            Property property = type.getProperties().get(i);
            if (values[index].equals(property.getName())) {
                index++;
            }
        }
        for (int i = 0; i < type.getAssociations().size()
                && (index < values.length); i++) {
            NavigationProperty property = type.getAssociations().get(i);
            if (values[index].equals(property.getName())) {
                index++;
            }
        }

        if (index != values.length) {
            result = values[index];
        }
        return result;
    }

    /**
     * Explores the given attributes in order to get a declared property
     * mapping.
     * 
     * @param type
     *            The type of the mapped property.
     * @param property
     *            The property that declares the mapping (is null if the mapping
     *            is declared on the entity type tag).
     * @param metadata
     *            The metadata instance to update.
     * @param attributes
     *            The XML attributes to parse.
     */
    private void discoverMapping(EntityType type, Property property,
            Metadata metadata, Attributes attributes) {
        String contentKind = null;
        String nsPrefix = null;
        String nsUri = null;
        String propertyPath = null;
        String valuePath = null;
        boolean keepInContent = true;

        contentKind = attributes.getValue(
                Service.WCF_DATASERVICES_METADATA_NAMESPACE, "FC_ContentKind");
        if (contentKind == null) {
            contentKind = "text";
        }

        nsPrefix = attributes.getValue(
                Service.WCF_DATASERVICES_METADATA_NAMESPACE, "FC_NsPrefix");
        nsUri = attributes.getValue(
                Service.WCF_DATASERVICES_METADATA_NAMESPACE, "FC_NsUri");

        String str = attributes
                .getValue(Service.WCF_DATASERVICES_METADATA_NAMESPACE,
                        "FC_KeepInContent");
        if (str != null) {
            keepInContent = Boolean.parseBoolean(str);
        }

        if (property == null) {
            // mapping declared on the entity type, the "FC_SourcePath"
            // attribute is mandatory.
            propertyPath = attributes.getValue(
                    Service.WCF_DATASERVICES_METADATA_NAMESPACE,
                    "FC_SourcePath");
        } else {
            propertyPath = property.getName();
        }

        valuePath = attributes.getValue(
                Service.WCF_DATASERVICES_METADATA_NAMESPACE, "FC_TargetPath");

        if (propertyPath != null && valuePath != null && !keepInContent) {
            // The mapping is really defined between a property and an XML
            // element, and the value is only available in a customized part of
            // the feed.
            if ((nsUri == null && nsPrefix == null)
                    || (nsUri != null && nsPrefix != null)) {
                // The mapping is correctly declared (either in an ATOM or a
                // customized XML element).
                metadata.getMappings().add(
                        new Mapping(type, nsPrefix, nsUri, propertyPath,
                                valuePath, contentKind));
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        // Update references.
        for (Schema schema : currentMetadata.getSchemas()) {
            // - associations.ends.type
            for (Association association : schema.getAssociations()) {
                // association type
                for (AssociationEnd end : association.getEnds()) {
                    end.setType((EntityType) resolve(end.getType(),
                            registeredEntityTypes, schema));
                }
            }
            for (EntityType entityType : schema.getEntityTypes()) {
                // entityType.key
                if (entityType.getKeys() != null) {
                    List<Property> props = entityType.getKeys();
                    entityType.setKeys(new ArrayList<Property>());
                    for (Property prop : props) {
                        for (Property property : entityType.getProperties()) {
                            if (property.equals(prop)) {
                                entityType.getKeys().add(property);
                                break;
                            }
                        }
                    }
                }
                // entityType.associations
                for (NavigationProperty navigationProperty : entityType
                        .getAssociations()) {
                    navigationProperty.setRelationship((Association) resolve(
                            navigationProperty.getRelationship(),
                            registeredAssociations, schema));

                    if (navigationProperty.getRelationship() != null) {
                        // association's roles.
                        for (AssociationEnd end : navigationProperty
                                .getRelationship().getEnds()) {
                            if (end.getRole().equals(
                                    navigationProperty.getFromRole().getRole())) {
                                navigationProperty.setFromRole(end);
                            } else if (end.getRole().equals(
                                    navigationProperty.getToRole().getRole())) {
                                navigationProperty.setToRole(end);
                            }
                        }
                    } else {
                        navigationProperty.setFromRole(null);
                        navigationProperty.setToRole(null);
                    }
                }
                // entityType.baseType
                entityType
                        .setBaseType((EntityType) resolve(
                                entityType.getBaseType(),
                                registeredEntityTypes, schema));
            }
            for (ComplexType complexType : schema.getComplexTypes()) {
                // complexType.baseType
                complexType.setBaseType((ComplexType) resolve(
                        complexType.getBaseType(), registeredComplexTypes,
                        schema));
            }
        }
        for (EntityContainer container : currentMetadata.getContainers()) {
            // - entityContainer.extended
            if (container.getExtended() != null) {
                container.setExtended(registeredContainers.get(container
                        .getExtended().getName()));
            }

            for (AssociationSet associationSet : container.getAssociations()) {
                // - associationSet.association
                associationSet.setAssociation((Association) resolve(
                        associationSet.getAssociation(),
                        registeredAssociations, container.getSchema()));
                // - associationSet.ends.entitySet
                for (AssociationSetEnd end : associationSet.getEnds()) {
                    for (EntitySet entitySet : container.getEntities()) {
                        if (entitySet.equals(end.getType())) {
                            end.setType(entitySet);
                            break;
                        }
                    }
                }
            }
            // - entityContainer.entitySet.entityType
            for (EntitySet entitySet : container.getEntities()) {
                entitySet.setType((EntityType) resolve(entitySet.getType(),
                        registeredEntityTypes, container.getSchema()));
            }
            // - entityContainer.functionImport.entitySet
            for (FunctionImport functionImport : container.getFunctionImports()) {
                functionImport.setEntitySet((EntitySet) resolve(
                        functionImport.getEntitySet(), registeredEntitySets,
                        container.getSchema()));
            }
        }

        for (Schema schema : currentMetadata.getSchemas()) {
            for (EntityType entityType : schema.getEntityTypes()) {
                // entityType.complexTypes
                for (ComplexProperty property : entityType
                        .getComplexProperties()) {
                    ComplexType type = (ComplexType) resolve(
                            property.getComplexType(), registeredComplexTypes,
                            schema);
                    if (type != null) {
                        property.setComplexType(type);
                    }
                }
            }
            for (ComplexType complexType : schema.getComplexTypes()) {
                // entityType.complexTypes
                for (ComplexProperty property : complexType
                        .getComplexProperties()) {
                    ComplexType type = (ComplexType) resolve(
                            property.getComplexType(), registeredComplexTypes,
                            schema);
                    if (type != null) {
                        property.setComplexType(type);
                    }
                }
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String name)
            throws SAXException {

        if ("schema".equalsIgnoreCase(localName)) {
            popState();
            currentSchema = null;
        } else if ("using".equalsIgnoreCase(localName)) {
            popState();
        } else if ("documentation".equalsIgnoreCase(localName)) {
            popState();
        } else if ("entityType".equalsIgnoreCase(localName)) {
            if (currentEntityType.isBlob()) {
                String memberName = chooseAttributeName(currentEntityType,
                        blobRefValues);

                if (memberName == null) {
                    // Should not happen
                    currentEntityType.setBlob(false);
                } else {
                    Property property = new Property(memberName);
                    currentEntityType.setBlobValueRefProperty(property);
                }
                // Sets the name of the property of the generated class that
                // contains the reference of resource that is able to update the
                // blob value.
                memberName = chooseAttributeName(currentEntityType,
                        blobEditRefValues);

                if (memberName == null) {
                    // Should not happen
                    currentEntityType.setBlob(false);
                } else {
                    Property property = new Property(memberName);
                    currentEntityType.setBlobValueEditRefProperty(property);
                }
            }
            popState();
            currentEntityType = null;
        } else if ("key".equalsIgnoreCase(localName)) {
            popState();
        } else if ("property".equalsIgnoreCase(localName)) {
            popState();
        } else if ("navigationProperty".equalsIgnoreCase(localName)) {
            popState();
        } else if ("complexType".equalsIgnoreCase(localName)) {
            popState();
        } else if ("association".equalsIgnoreCase(localName)) {
            popState();
            currentAssociation = null;
        } else if ("end".equalsIgnoreCase(localName)) {
            popState();
        } else if ("onDelete".equalsIgnoreCase(localName)) {
            popState();
        } else if ("referentialConstraint".equalsIgnoreCase(localName)) {
            popState();
        } else if ("functionImport".equalsIgnoreCase(localName)) {
            currentFunctionImport = null;
            popState();
        } else if ("function".equalsIgnoreCase(localName)) {
            popState();
        } else if ("entityContainer".equalsIgnoreCase(localName)) {
            popState();
            currentEntityContainer = null;
        } else if ("entitySet".equalsIgnoreCase(localName)) {
            popState();
        } else if ("associationSet".equalsIgnoreCase(localName)) {
            popState();
            currentAssociationSet = null;
        } else if ("parameter".equalsIgnoreCase(localName)) {
            popState();
        }
    }

    /**
     * Returns the current state when processing the document.
     * 
     * @return the current state when processing the document.
     */
    private State getState() {
        final State result = this.states.get(0);
        return result;
    }

    /**
     * Drops the current state from the stack and returns it. This state becomes
     * the former current state.
     * 
     * @return the former current state.
     */
    private State popState() {
        return this.states.remove(0);
    }

    /**
     * Adds the given state.
     * 
     * @param state
     *            The given state.
     */
    private void pushState(State state) {
        this.states.add(0, state);
    }

    /**
     * Finds a namedObject inside a register.
     * 
     * @param namedObject
     *            The namedObject to find.
     * @param register
     *            The register.
     * @param schema
     *            The schema of the named object.
     * @return The namedObject if found inside the register, null otherwise.
     */
    private NamedObject resolve(NamedObject namedObject,
            Map<String, NamedObject> register, Schema currentSchema) {
        NamedObject result = null;
        if (namedObject != null && namedObject.getName() != null) {
            String key = null;
            int index = namedObject.getName().lastIndexOf(".");
            if (index != -1) {
                // Objects are named via the namespace alias or full name
                String nsName = namedObject.getName().substring(0, index);
                for (Namespace namespace : registeredNamespaces) {
                    if (nsName.equals(namespace.getAlias())
                            || nsName.equals(namespace.getName())) {
                        key = namespace.getName()
                                + namedObject.getName().substring(index);
                        break;
                    }
                }
            } else {
                key = currentSchema.getNamespace().getName() + "."
                        + namedObject.getName();
            }

            result = register.get(key);
        }

        return result;
    }

    @Override
    public void startDocument() throws SAXException {
        registeredComplexTypes = new HashMap<String, NamedObject>();
        registeredEntityTypes = new HashMap<String, NamedObject>();
        registeredAssociations = new HashMap<String, NamedObject>();
        registeredEntitySets = new HashMap<String, NamedObject>();
        registeredNamespaces = new ArrayList<Namespace>();
        registeredContainers = new HashMap<String, EntityContainer>();
    }

    @Override
    public void startElement(String uri, String localName, String name,
            Attributes attrs) throws SAXException {

        if ("schema".equalsIgnoreCase(localName)) {
            pushState(State.SCHEMA);
            currentSchema = new Schema();
            this.currentMetadata.getSchemas().add(currentSchema);

            Namespace namespace = new Namespace(attrs.getValue("Namespace"));
            namespace.setAlias(attrs.getValue("Alias"));
            this.currentSchema.setNamespace(namespace);
            registeredNamespaces.add(namespace);
        } else if ("using".equalsIgnoreCase(localName)) {
            pushState(State.USING);
            Namespace namespace = new Namespace(attrs.getValue("Namespace"));
            namespace.setAlias(attrs.getValue("Alias"));
            this.currentSchema.getReferencedNamespaces().add(namespace);
        } else if ("documentation".equalsIgnoreCase(localName)) {
            pushState(State.DOCUMENTATION);
        } else if ("entityType".equalsIgnoreCase(localName)) {
            pushState(State.ENTITY_TYPE);
            currentEntityType = new EntityType(attrs.getValue("Name"));
            currentEntityType.setSchema(this.currentSchema);
            currentEntityType.setAbstractType(Boolean.parseBoolean(attrs
                    .getValue("Abstract")));
            currentEntityType.setBlob(Boolean.parseBoolean(attrs.getValue(
                    Service.WCF_DATASERVICES_METADATA_NAMESPACE, "HasStream")));
            String value = attrs.getValue("BaseType");
            if (value != null) {
                currentEntityType.setBaseType(new EntityType(value));
            }
            this.currentSchema.getEntityTypes().add(currentEntityType);

            // Check the declaration of a property mapping.
            discoverMapping(currentEntityType, null, currentMetadata, attrs);
            // register the new type.
            registeredEntityTypes.put(currentSchema.getNamespace().getName()
                    + "." + currentEntityType.getName(), currentEntityType);
        } else if ("key".equalsIgnoreCase(localName)) {
            pushState(State.ENTITY_TYPE_KEY);
        } else if ("propertyRef".equalsIgnoreCase(localName)) {
            if (getState() == State.ENTITY_TYPE_KEY) {
                if (currentEntityType.getKeys() == null) {
                    currentEntityType.setKeys(new ArrayList<Property>());
                }
                currentEntityType.getKeys().add(
                        new Property(attrs.getValue("Name")));
            }
        } else if ("property".equalsIgnoreCase(localName)) {
            String type = attrs.getValue("Type");
            Property property;
            if (type.toLowerCase().startsWith("edm.")) {
                property = new Property(attrs.getValue("Name"));
                property.setType(new Type(attrs.getValue("Type")));
            } else {
                ComplexProperty p = new ComplexProperty(attrs.getValue("Name"));
                p.setComplexType(new ComplexType(attrs.getValue("Type")));
                property = p;
            }

            property.setDefaultValue(attrs.getValue("Default"));
            // If no value is specified, the nullable facet defaults to true.
            // cf
            // http://www.odata.org/documentation/odata-v3-documentation/common-schema-definition-language-csdl/#531_The_edmNullable_Attribute
            String nullable = attrs.getValue("Nullable");
            if (nullable == null) {
                property.setNullable(true);
            } else {
                property.setNullable(Boolean.parseBoolean(nullable));
            }
            // ConcurrencyMode
            if ("fixed".equalsIgnoreCase(attrs.getValue("ConcurrencyMode"))) {
                property.setConcurrent(true);
            } else {
                property.setConcurrent(false);
            }

            property.setGetterAccess(attrs.getValue("GetterAccess"));
            property.setSetterAccess(attrs.getValue("SetterAccess"));
            String str = attrs.getValue(
                    Service.WCF_DATASERVICES_METADATA_NAMESPACE, "MimeType");
            if (str != null) {
                property.setMediaType(MediaType.valueOf(str));
            }

            if (getState() == State.ENTITY_TYPE) {
                pushState(State.ENTITY_TYPE_PROPERTY);
                if (property instanceof ComplexProperty) {
                    this.currentEntityType.getComplexProperties().add(
                            (ComplexProperty) property);
                } else {
                    this.currentEntityType.getProperties().add(property);
                }
            } else {
                pushState(State.COMPLEX_TYPE_PROPERTY);
                if (property instanceof ComplexProperty) {
                    this.currentComplexType.getComplexProperties().add(
                            (ComplexProperty) property);
                } else {
                    this.currentComplexType.getProperties().add(property);
                }
            }

            // Check the declaration of a property mapping.
            discoverMapping(this.currentEntityType, property, currentMetadata,
                    attrs);
        } else if ("navigationProperty".equalsIgnoreCase(localName)) {
            pushState(State.NAVIGATION_PROPERTY);
            NavigationProperty property = new NavigationProperty(
                    attrs.getValue("Name"));
            property.setFromRole(new AssociationEnd(attrs.getValue("FromRole")));
            property.setRelationship(new Association(attrs
                    .getValue("Relationship")));
            property.setToRole(new AssociationEnd(attrs.getValue("ToRole")));
            currentEntityType.getAssociations().add(property);
        } else if ("complexType".equalsIgnoreCase(localName)) {
            pushState(State.COMPLEX_TYPE);
            currentComplexType = new ComplexType(attrs.getValue("Name"));
            currentComplexType.setSchema(this.currentSchema);

            String value = attrs.getValue("BaseType");
            if (value != null) {
                currentComplexType.setBaseType(new ComplexType(value));
            }
            this.currentSchema.getComplexTypes().add(currentComplexType);
            // register the new type.
            registeredComplexTypes.put(currentSchema.getNamespace().getName()
                    + "." + currentComplexType.getName(), currentComplexType);
        } else if ("association".equalsIgnoreCase(localName)) {
            pushState(State.ASSOCIATION);
            currentAssociation = new Association(attrs.getValue("Name"));
            currentSchema.getAssociations().add(currentAssociation);
            registeredAssociations.put(currentSchema.getNamespace().getName()
                    + "." + currentAssociation.getName(), currentAssociation);
        } else if ("end".equalsIgnoreCase(localName)) {
            if (getState() == State.ASSOCIATION) {
                pushState(State.ASSOCIATION_END);
                AssociationEnd end = new AssociationEnd(attrs.getValue("Role"));
                end.setMultiplicity(attrs.getValue("Multiplicity"));
                end.setType(new EntityType(attrs.getValue("Type")));
                currentAssociation.getEnds().add(end);
            } else {
                pushState(State.ASSOCIATION_SET_END);
                AssociationSetEnd end = new AssociationSetEnd(
                        attrs.getValue("Role"));
                end.setType(new EntitySet(attrs.getValue("EntitySet")));
                currentAssociationSet.getEnds().add(end);
            }
        } else if ("onDelete".equalsIgnoreCase(localName)) {
            pushState(State.ON_DELETE);
        } else if ("referentialConstraint".equalsIgnoreCase(localName)) {
            pushState(State.REFERENTIAL_CONSTRAINT);
        } else if ("functionImport".equalsIgnoreCase(localName)) {
            currentFunctionImport = new FunctionImport(attrs.getValue("Name"));
            currentFunctionImport.setReturnType(attrs.getValue("ReturnType"));
            currentFunctionImport.setEntitySet(new EntitySet(attrs
                    .getValue("EntitySet")));
            currentFunctionImport.setMethodAccess(attrs
                    .getValue("MethodAccess"));
            currentFunctionImport.setMetadata(currentMetadata);

            String str = attrs.getValue(
                    Service.WCF_DATASERVICES_METADATA_NAMESPACE, "HttpMethod");
            if (str != null) {
                currentFunctionImport.setMethod(Method.valueOf(str));
            }

            if (State.ENTITY_CONTAINER == getState()) {
                currentEntityContainer.getFunctionImports().add(
                        currentFunctionImport);
            }

            pushState(State.FUNCTION_IMPORT);
        } else if ("parameter".equalsIgnoreCase(localName)) {
            if (State.FUNCTION_IMPORT == getState()) {
                Parameter parameter = new Parameter(attrs.getValue("Name"));
                parameter.setType(attrs.getValue("Type"));
                parameter.setMode(attrs.getValue("Mode"));
                String str = attrs.getValue("MaxLength");
                if (str != null) {
                    parameter.setMaxLength(Integer.parseInt(str));
                }
                str = attrs.getValue("Precision");
                if (str != null) {
                    parameter.setPrecision(Integer.parseInt(str));
                }
                str = attrs.getValue("Scale");
                if (str != null) {
                    parameter.setScale(Integer.parseInt(str));
                }

                currentFunctionImport.getParameters().add(parameter);
            }
            pushState(State.PARAMETER);
        } else if ("function".equalsIgnoreCase(localName)) {
            pushState(State.FUNCTION);
        } else if ("entityContainer".equalsIgnoreCase(localName)) {
            pushState(State.ENTITY_CONTAINER);
            currentEntityContainer = new EntityContainer(attrs.getValue("Name"));
            currentEntityContainer.setDefaultEntityContainer(Boolean
                    .parseBoolean(attrs.getValue(
                            Service.WCF_DATASERVICES_METADATA_NAMESPACE,
                            "IsDefaultEntityContainer")));
            String value = attrs.getValue("Extends");
            if (value != null) {
                currentEntityContainer.setExtended(new EntityContainer(value));
            }
            currentEntityContainer.setSchema(currentSchema);
            currentMetadata.getContainers().add(currentEntityContainer);
            registeredContainers.put(currentSchema.getNamespace().getName()
                    + "." + currentEntityContainer.getName(),
                    currentEntityContainer);
        } else if ("entitySet".equalsIgnoreCase(localName)) {
            pushState(State.ENTITY_SET);
            EntitySet entitySet = new EntitySet(attrs.getValue("Name"));
            registeredEntitySets.put(currentSchema.getNamespace().getName()
                    + "." + entitySet.getName(), entitySet);
            entitySet.setType(new EntityType(attrs.getValue("EntityType")));
            currentEntityContainer.getEntities().add(entitySet);
        } else if ("associationSet".equalsIgnoreCase(localName)) {
            pushState(State.ASSOCIATION_SET);
            currentAssociationSet = new AssociationSet(attrs.getValue("Name"));
            currentAssociationSet.setAssociation(new Association(attrs
                    .getValue("Association")));
            currentEntityContainer.getAssociations().add(currentAssociationSet);
        }
    }
}
