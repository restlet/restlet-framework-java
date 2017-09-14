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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.restlet.data.Reference;
import org.restlet.ext.xml.SaxRepresentation;
import org.restlet.representation.Representation;

/**
 * Represents the metadata of an OData service.
 * 
 * @author Thierry Boileau
 */
public class Metadata extends SaxRepresentation {

    /** The list of entity containers. */
    private List<EntityContainer> containers;

    /** The list of declared property mappings. */
    private List<Mapping> mappings;

    /** The URI of the metadata resource. */
    private Reference metadataRef;

    /** The data services schemas. */
    private List<Schema> schemas;

    /**
     * Constructor.
     * 
     * @param metadata
     *            The representation of the metadata.
     * @param metadataRef
     *            Its optional URI.
     * @throws IOException
     */
    public Metadata(Representation metadata, Reference metadataRef)
            throws IOException {
        super(metadata);
        setNamespaceAware(true);
        this.metadataRef = metadataRef;
        parse(new MetadataReader(this));
    }

    /**
     * Returns the Association object that corresponds to the given property of
     * the given entity type.
     * 
     * @param type
     *            The entity type.
     * @param propertyName
     *            The property name.
     * @return the Association object that corresponds to the given property of
     *         the given entity type.
     */
    public AssociationEnd getAssociation(EntityType type, String propertyName) {
        AssociationEnd result = null;

        for (NavigationProperty association : type.getAssociations()) {
            if (association.getNormalizedName().equals(propertyName)) {
                result = association.getToRole();
                break;
            }
        }

        return result;
    }

    /**
     * Returns the complectType that corresponds to a given entity class.
     * 
     * @param entityClass
     *            The entity class.
     * @return The ComplexType that corresponds to a given entity class.
     */
    public ComplexType getComplexType(Class<?> entityClass) {
        ComplexType result = null;

        // Try to match the entity class names (without package);
        String className = entityClass.getName();
        int index = className.lastIndexOf(".");
        if (index != -1) {
            className = className.substring(index + 1);
        }

        for (Iterator<Schema> iec = getSchemas().iterator(); result == null
                && iec.hasNext();) {
            Schema schema = iec.next();
            for (Iterator<ComplexType> ies = schema.getComplexTypes()
                    .iterator(); result == null && ies.hasNext();) {
                ComplexType type = ies.next();
                if (type.getClassName().equals(className)) {
                    result = type;
                }
            }
        }

        return result;
    }

    /**
     * Returns the list of entity containers.
     * 
     * @return The list of entity containers.
     */
    public List<EntityContainer> getContainers() {
        if (containers == null) {
            containers = new ArrayList<EntityContainer>();
        }
        return containers;
    }

    /**
     * Returns the subpath of the entitySet of the given entity type.
     * 
     * @param entity
     *            The entity.
     * @return The subpath of the entitySet of the given entity type.
     */
    public String getEntitySetSubpath(EntityType entityType) {
        String result = null;

        if (entityType == null) {
            return result;
        }

        // Try to match the entity class names (without package);
        for (EntityContainer entityContainer : getContainers()) {
            for (EntitySet entitySet : entityContainer.getEntities()) {
                EntityType type = entitySet.getType();
                if (type.equals(entityType)) {
                    result = "/" + entitySet.getName();
                }
            }
        }

        return result;
    }

    /**
     * Returns the entityType that corresponds to a given entity class.
     * 
     * @param entityClass
     *            The entity class.
     * @return The entityType that corresponds to a given entity class.
     */
    public EntityType getEntityType(Class<?> entityClass) {
        EntityType result = null;

        // Try to match the entity class names (without package);
        String className = entityClass.getName();
        int index = className.lastIndexOf(".");
        if (index != -1) {
            className = className.substring(index + 1);
        }

        for (Iterator<EntityContainer> iec = getContainers().iterator(); result == null
                && iec.hasNext();) {
            EntityContainer entityContainer = iec.next();
            for (Iterator<EntitySet> ies = entityContainer.getEntities()
                    .iterator(); result == null && ies.hasNext();) {
                EntitySet entitySet = ies.next();
                EntityType type = entitySet.getType();
                if (type.getClassName().equals(className)) {
                    result = type;
                }
            }
        }

        return result;
    }

    /**
     * Returns the String representation of the value of the key of the given
     * entity that should be used in all URIs.
     * 
     * @param type
     *            The type descriptor of the object.
     * @param entity
     *            The entity.
     * @return The value of the key of the given entity or null in case of
     *         error.
     */
    public String getKeyValue(EntityType type, Object entity) {
        StringBuffer result = new StringBuffer();

        if (type.getKeys() != null && !type.getKeys().isEmpty()) {
            if (type.getKeys().size() == 1) {
                Property key = type.getKeys().get(0);
                String keyName = key.getNormalizedName();
                String getterName = "get"
                        + keyName.substring(0, 1).toUpperCase()
                        + keyName.substring(1);
                try {
                    Method getter = entity.getClass().getDeclaredMethod(
                            getterName, (Class[]) null);
                    Object value = getter.invoke(entity, (Object[]) null);
                    String strValue = TypeUtils.toEdmKey(value, key.getType());
                    if (strValue != null) {
                        result.append(strValue);
                    } else {
                        result.append("''");
                    }
                } catch (Exception e) {
                    // Nothing
                }
            } else {
                Iterator<Property> it = type.getKeys().iterator();
                while (it.hasNext()) {
                    Property key = it.next();
                    String keyName = key.getNormalizedName();
                    result.append(key.getName()).append("=");
                    String getterName = "get"
                            + keyName.substring(0, 1).toUpperCase()
                            + keyName.substring(1);
                    try {
                        Method getter = entity.getClass().getDeclaredMethod(
                                getterName, (Class[]) null);
                        Object value = getter.invoke(entity, (Object[]) null);
                        String strValue = TypeUtils.toEdmKey(value,
                                key.getType());
                        if (strValue != null) {
                            result.append(strValue);
                        } else {
                            result.append("''");
                        }
                    } catch (Exception e) {
                        // Nothing
                    }
                    if (it.hasNext()) {
                        result.append(",");
                    }
                }
            }
        }

        return result.toString();
    }

    /**
     * Returns the value of the key of the given entity that should be used in
     * all URIs.
     * 
     * @param entity
     *            The entity.
     * @return The value of the key of the given entity or null in case of
     *         error.
     */
    public String getKeyValue(Object entity) {
        String result = null;
        if (entity != null) {
            result = getKeyValue(getEntityType(entity.getClass()), entity);
        }

        return result;
    }

    /**
     * Returns the list of declared property mappings.
     * 
     * @return The list of declared property mappings.
     */
    public List<Mapping> getMappings() {
        if (mappings == null) {
            mappings = new ArrayList<Mapping>();
        }
        return mappings;
    }

    /**
     * Returns the URI of the metadata resource.
     * 
     * @return The URI of the metadata resource.
     */
    public Reference getMetadataRef() {
        return metadataRef;
    }

    /**
     * Returns the EDM type of the given property of the given entity.
     * 
     * @param entity
     *            The entity.
     * @param propertyName
     *            The name of the property.
     * @return The EDM type of the given property of the given entity.
     */
    public Property getProperty(Object entity, String propertyName) {
        Property result = null;
        if (entity != null) {
            EntityType et = getEntityType(entity.getClass());
            if (et != null) {
                for (Property property : et.getProperties()) {
                    if (property.getName().equals(propertyName)
                            || property.getNormalizedName().equals(propertyName)) {
                        result = property;
                        break;
                    }
                }
            } else {
                ComplexType ct = getComplexType(entity.getClass());
                if (ct != null) {
                    for (Property property : ct.getProperties()) {
                        if (property.getName().equals(propertyName)
                                || property.getNormalizedName().equals(propertyName)) {
                            result = property;
                            break;
                        }
                    }
                }
            }

        }

        return result;
    }

    /**
     * Returns the data service schema.
     * 
     * @return The data service schema.
     */
    public List<Schema> getSchemas() {
        if (schemas == null) {
            schemas = new ArrayList<Schema>();
        }
        return schemas;
    }

    /**
     * According to the metadata of the service, returns the path of the given
     * entity relatively to the current WCF service.
     * 
     * @param entity
     *            The entity.
     * @return The path of the given entity relatively to the current WCF
     *         service.
     */
    public String getSubpath(Object entity) {
        String result = null;

        if (entity == null) {
            return result;
        }

        // Try to match the entity class names (without package);
        String entityClass = entity.getClass().getName();
        int index = entityClass.lastIndexOf(".");
        if (index != -1) {
            entityClass = entityClass.substring(index + 1);
        }

        for (EntityContainer entityContainer : getContainers()) {
            for (EntitySet entitySet : entityContainer.getEntities()) {
                EntityType type = entitySet.getType();

                if (type.getClassName().equals(entityClass)) {
                    String value = getKeyValue(type, entity);
                    if (value != null) {
                        result = "/" + entitySet.getName() + "(" + value + ")";
                    }
                }
            }
        }

        return result;
    }

    /**
     * According to the metadata of the service, returns the path of the given
     * entity's property relatively to the current WCF service.
     * 
     * @param entity
     *            The entity.
     * @param propertyName
     *            The name of the property.
     * @return The path of the given entity's property relatively to the current
     *         WCF service.
     */
    public String getSubpath(Object entity, String propertyName) {
        return getSubpath(entity) + "/" + propertyName;
    }

    /**
     * According to the metadata of the service, returns the relative path of
     * the given target entity linked to the source entity via the source
     * property.
     * 
     * @param source
     *            The source entity to update.
     * @param sourceProperty
     *            The name of the property of the source entity.
     * @param target
     *            The entity linked to the source entity.
     * @return
     */
    public String getSubpath(Object source, String sourceProperty, Object target) {
        return getSubpath(source) + "/" + sourceProperty + "("
                + getKeyValue(target) + ")";
    }

    /**
     * Sets the list of entity containers.
     * 
     * @param containers
     *            The list of entity containers.
     */
    public void setContainers(List<EntityContainer> containers) {
        this.containers = containers;
    }

    /**
     * Sets the data service schemas
     * 
     * @param schemas
     *            The data service schemas.
     */
    public void setSchemas(List<Schema> schemas) {
        this.schemas = schemas;
    }

}
