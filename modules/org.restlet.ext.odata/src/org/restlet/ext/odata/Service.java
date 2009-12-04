/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.odata;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.atom.Content;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.odata.internal.edm.AssociationEnd;
import org.restlet.ext.odata.internal.edm.EntityType;
import org.restlet.ext.odata.internal.edm.Metadata;
import org.restlet.ext.odata.internal.edm.Property;
import org.restlet.ext.odata.internal.edm.Type;
import org.restlet.ext.odata.internal.reflect.ReflectUtils;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.ext.xml.SaxRepresentation;
import org.restlet.ext.xml.XmlWriter;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Acts as a manager for a specific remote WCF data service. WCF Data Services
 * are stateless, but the Service is not. State on the client is maintained
 * between interactions in order to support features such as update management.<br>
 * <br>
 * This Java class is more or less equivalent to the WCF DataServiceContext
 * class.
 * 
 * @author Jerome Louvel
 * @see <a
 *      href="http://msdn.microsoft.com/en-us/library/system.data.services.client.dataservicecontext.aspx">DataServiceContext
 *      Class on MSDN</a>
 */
public class Service {

    /** The credentials used to authenticate requests. */
    private ChallengeResponse credentials;

    /** The latest request sent to the service. */
    private Request latestRequest;

    /** The response to the latest request. */
    private Response latestResponse;

    /** The internal logger. */
    private Logger logger;

    /** The metadata of the WCF service. */
    private Metadata metadata;

    /** The reference of the WCF service. */
    private Reference serviceRef;

    /**
     * Constructor.
     * 
     * @param serviceRef
     *            The reference to the WCF service.
     */
    public Service(Reference serviceRef) {
        this.serviceRef = serviceRef;
    }

    /**
     * Constructor.
     * 
     * @param serviceUri
     *            The URI of the WCF service.
     */
    public Service(String serviceUri) {
        this(new Reference(serviceUri));
    }

    /**
     * Adds an entity to an entity set.
     * 
     * @param entitySetName
     *            The path of the entity set relatively to the service URI.
     * @param entity
     *            The entity to put.
     * @throws Exception
     */
    public void addEntity(String entitySetName, Object entity) throws Exception {
        Entry entry = new Entry();
        entry.setContent(toContent(entity));

        ClientResource resource = new ClientResource(new Reference(serviceRef
                .toString()
                + entitySetName));
        resource.setChallengeResponse(getCredentials());
        resource.post(entry);
        this.latestRequest = resource.getRequest();
        this.latestResponse = resource.getResponse();
        if (!resource.getStatus().isSuccess()) {
            throw new ResourceException(resource.getStatus(),
                    "Can't add entity to this entity set "
                            + resource.getReference());
        }
    }

    /**
     * Adds an association between the source and the target entity via the
     * given property name.
     * 
     * @param source
     *            The source entity to update.
     * @param sourceProperty
     *            The name of the property of the source entity.
     * @param target
     *            The entity to add to the source entity.
     * @throws Exception
     */
    public void addLink(Object source, String sourceProperty, Object target)
            throws Exception {
        if (getMetadata() == null) {
            return;
        }

        addEntity(getSubpath(source, sourceProperty), target);
    }

    /**
     * Creates a query to a specific entity hosted by this service.
     * 
     * @param <T>
     *            The class of the target entity.
     * @param subpath
     *            The path to this entity relatively to the service URI.
     * @param entityClass
     *            The target class of the entity.
     * @return A query object.
     */
    public <T> Query<T> createQuery(String subpath, Class<T> entityClass) {
        return new Query<T>(this, subpath, entityClass);
    }

    /**
     * Deletes an entity.
     * 
     * @param entity
     *            The entity to delete
     * @throws ResourceException
     */
    public void deleteEntity(Object entity) throws ResourceException {
        if (getMetadata() == null) {
            return;
        }
        ClientResource resource = new ClientResource(new Reference(serviceRef
                .toString()
                + getSubpath(entity)));
        resource.setChallengeResponse(getCredentials());

        resource.delete();
        this.latestRequest = resource.getRequest();
        this.latestResponse = resource.getResponse();
    }

    /**
     * Deletes an entity.
     * 
     * @param entitySubpath
     *            The path of the entity to delete
     * @throws ResourceException
     */
    public void deleteEntity(String entitySubpath) throws ResourceException {
        ClientResource resource = new ClientResource(new Reference(serviceRef
                .toString()
                + entitySubpath));
        resource.setChallengeResponse(getCredentials());

        resource.delete();
        this.latestRequest = resource.getRequest();
        this.latestResponse = resource.getResponse();
    }

    /**
     * Removes the association between a source entity and a target entity via
     * the given property name.
     * 
     * @param source
     *            The source entity to update.
     * @param sourceProperty
     *            The name of the property of the source entity.
     * @param target
     *            The entity to delete from the source entity.
     * @throws ResourceException
     */
    public void deleteLink(Object source, String sourceProperty, Object target)
            throws ResourceException {
        if (getMetadata() == null) {
            return;
        }
        deleteEntity(getSubpath(source, sourceProperty, target));
    }

    /**
     * Returns the credentials used to authenticate requests.
     * 
     * @return The credentials used to authenticate requests.
     */
    public ChallengeResponse getCredentials() {
        return credentials;
    }

    /**
     * Returns the latest request sent to the service.
     * 
     * @return The latest request sent to the service.
     */
    public Request getLatestRequest() {
        return latestRequest;
    }

    /**
     * Returns the response to the latest request.
     * 
     * @return The response to the latest request.
     */
    public Response getLatestResponse() {
        return latestResponse;
    }

    /**
     * Returns the current logger.
     * 
     * @return The current logger.
     */
    private Logger getLogger() {
        if (logger == null) {
            logger = Context.getCurrentLogger();
        }
        return logger;
    }

    /**
     * Returns the metadata document related to the current service.
     * 
     * @return The metadata document related to the current service.
     */
    protected Metadata getMetadata() {
        if (metadata == null) {
            String sRef = serviceRef.toString();
            if (!sRef.endsWith("/")) {
                sRef += "/";
            }
            ClientResource resource = new ClientResource(sRef + "$metadata");
            resource.setChallengeResponse(getCredentials());

            try {
                getLogger().log(
                        Level.INFO,
                        "Get the metadata for " + serviceRef + " at "
                                + resource.getReference());
                Representation rep = resource.get(MediaType.APPLICATION_XML);

                if (resource.getStatus().isSuccess()) {
                    this.metadata = new Metadata(rep, resource.getReference());
                } else {
                    getLogger().log(
                            Level.SEVERE,
                            "Can't get the metadata for " + serviceRef
                                    + " (response's status: "
                                    + resource.getStatus() + ")");
                }
            } catch (Exception e) {
                getLogger().log(Level.SEVERE,
                        "Can't get the metadata for " + serviceRef, e);
            }
        }

        return metadata;
    }

    /**
     * Returns the reference to the WCF service.
     * 
     * @return The reference to the WCF service.
     */
    public Reference getServiceRef() {
        return serviceRef;
    }

    /**
     * Updates the given entity object with the value of the specified property.
     * 
     * @param entity
     *            The entity to update.
     * @param propertyName
     *            The name of the property.
     */
    public void loadProperty(Object entity, String propertyName) {
        if (getMetadata() == null || entity == null) {
            return;
        }

        EntityType type = getMetadata().getEntityType(entity.getClass());
        AssociationEnd association = getMetadata().getAssociation(type,
                propertyName);

        if (association != null) {
            EntityType propertyEntityType = association.getType();
            try {
                Class<?> propertyClass = ReflectUtils.getSimpleClass(entity,
                        propertyName);
                if (propertyClass == null) {
                    propertyClass = Type.getJavaClass(propertyEntityType);
                }
                Iterator<?> iterator = createQuery(
                        getSubpath(entity, propertyName), propertyClass)
                        .iterator();

                ReflectUtils.setProperty(entity, propertyName, association
                        .isToMany(), iterator, propertyClass);
            } catch (Exception e) {
                getLogger().log(
                        Level.WARNING,
                        "Can't set the property " + propertyName + " of "
                                + entity.getClass() + " for the service"
                                + serviceRef, e);
            }
        } else {
            String ref = getServiceRef().toString()
                    + getSubpath(entity, propertyName);
            try {
                ClientResource resource = new ClientResource(ref);
                resource.setChallengeResponse(getCredentials());
                Representation rep = resource.get();

                if (resource.getStatus().isSuccess()) {
                    DomRepresentation xmlRep = new DomRepresentation(rep);
                    // [ifndef android] instruction
                    Node node = xmlRep.getNode("//" + propertyName);

                    // [ifdef android] uncomment
                    // Node node = null;
                    // try {
                    // org.w3c.dom.NodeList nl = xmlRep.getDocument()
                    // .getElementsByTagName(propertyName);
                    // node = (nl.getLength() > 0) ? nl.item(0) : null;
                    // } catch (IOException e1) {
                    // }
                    // [enddef]

                    if (node != null) {
                        Property property = getMetadata().getProperty(entity,
                                propertyName);
                        try {
                            // [ifndef android] instruction
                            ReflectUtils.setProperty(entity, property, node
                                    .getTextContent());
                            // [ifdef android] instruction uncomment
                            // ReflectUtils.setProperty(entity, property,
                            // org.restlet.ext.xml.XmlRepresentation.getTextContent(node));
                        } catch (Exception e) {
                            getLogger().log(
                                    Level.WARNING,
                                    "Can't set the property " + propertyName
                                            + " of " + entity.getClass()
                                            + " for the service" + serviceRef,
                                    e);
                        }
                    } else {
                        getLogger().log(
                                Level.WARNING,
                                "Can't set the property " + propertyName
                                        + " of " + entity.getClass()
                                        + " for the service" + serviceRef);
                    }
                }
            } catch (ResourceException e) {
                getLogger().log(
                        Level.WARNING,
                        "Can't get the following resource " + ref
                                + " for the service" + serviceRef, e);
            }
        }
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
    private String getSubpath(Object entity) {
        return getMetadata().getSubpath(entity);
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
    private String getSubpath(Object entity, String propertyName) {
        return getMetadata().getSubpath(entity, propertyName);
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
    private String getSubpath(Object source, String sourceProperty,
            Object target) {
        return getMetadata().getSubpath(source, sourceProperty, target);
    }

    /**
     * Sets the credentials used to authenticate requests.
     * 
     * @param credentials
     *            The credentials used to authenticate requests.
     */
    public void setCredentials(ChallengeResponse credentials) {
        this.credentials = credentials;
    }

    /**
     * Sets the latest request sent to the service.
     * 
     * @param latestRequest
     *            The latest request sent to the service.
     */
    public void setLatestRequest(Request latestRequest) {
        this.latestRequest = latestRequest;
    }

    /**
     * Sets the response to the latest request.
     * 
     * @param latestResponse
     *            The response to the latest request.
     */
    public void setLatestResponse(Response latestResponse) {
        this.latestResponse = latestResponse;
    }

    /**
     * Converts an entity to an Atom content object.
     * 
     * @param entity
     *            The entity to wrap.
     * @return The Atom content object that corresponds to the given entity.
     */
    private Content toContent(final Object entity) throws Exception {
        Representation r = new SaxRepresentation(MediaType.APPLICATION_XML) {
            @Override
            public void write(XmlWriter writer) throws IOException {
                try {
                    writer
                            .forceNSDecl(
                                    "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata",
                                    "m");
                    writer
                            .forceNSDecl(
                                    "http://schemas.microsoft.com/ado/2007/08/dataservices",
                                    "ds");
                    writer
                            .startElement(
                                    "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata",
                                    "properties");

                    for (Field field : entity.getClass().getDeclaredFields()) {
                        String getter = "get"
                                + field.getName().substring(0, 1).toUpperCase()
                                + field.getName().substring(1);
                        Property prop = getMetadata().getProperty(entity,
                                field.getName());
                        if (prop != null) {
                            for (Method method : entity.getClass()
                                    .getDeclaredMethods()) {
                                if (method.getReturnType() != null
                                        && getter.equals(method.getName())
                                        && method.getParameterTypes().length == 0) {
                                    Object value = null;
                                    try {
                                        value = method.invoke(entity,
                                                (Object[]) null);
                                    } catch (Exception e) {

                                    }
                                    if (value != null) {
                                        writer
                                                .startElement(
                                                        "http://schemas.microsoft.com/ado/2007/08/dataservices",
                                                        prop.getName());
                                        writer.characters(Type.toEdm(value,
                                                prop.getType()));
                                        writer
                                                .endElement(
                                                        "http://schemas.microsoft.com/ado/2007/08/dataservices",
                                                        prop.getName());

                                    } else {
                                        writer
                                                .emptyElement(
                                                        "http://schemas.microsoft.com/ado/2007/08/dataservices",
                                                        prop.getName());
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    writer
                            .endElement(
                                    "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata",
                                    "properties");
                } catch (SAXException e) {
                    throw new IOException(e.getMessage());
                }
            }
        };
        Content content = new Content();
        content.setInlineContent(r);
        content.setToEncode(false);
        return content;
    }

    /**
     * Updates an entity.
     * 
     * @param entity
     *            The entity to put.
     * @throws Exception
     */
    public void updateEntity(Object entity) throws Exception {
        if (getMetadata() == null) {
            return;
        }
        Entry entry = new Entry();
        entry.setContent(toContent(entity));

        ClientResource resource = new ClientResource(new Reference(serviceRef
                .toString()
                + getSubpath(entity)));
        resource.setChallengeResponse(getCredentials());

        resource.put(entry);
        this.latestRequest = resource.getRequest();
        this.latestResponse = resource.getResponse();
        if (!resource.getStatus().isSuccess()) {
            throw new ResourceException(resource.getStatus(),
                    "Can't update this entity " + resource.getReference());
        }
    }
}
