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

package org.restlet.ext.dataservices;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.atom.Content;
import org.restlet.ext.atom.Entry;
import org.restlet.ext.dataservices.internal.edm.AssociationEnd;
import org.restlet.ext.dataservices.internal.edm.EntityType;
import org.restlet.ext.dataservices.internal.edm.Metadata;
import org.restlet.ext.dataservices.internal.edm.Property;
import org.restlet.ext.dataservices.internal.edm.Type;
import org.restlet.ext.dataservices.internal.reflect.ReflectUtils;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Represents the runtime context of an ADO.NET data service. ADO.NET Data
 * Services are stateless, but the Session is not. State on the client is
 * maintained between interactions in order to support features such as update
 * management.<br>
 * <br>
 * This Java class is more or less equivalent to the ADO.NET DataServiceContext
 * class.
 * 
 * @author Jerome Louvel
 * @see <a
 *      href="http://msdn.microsoft.com/en-us/library/system.data.services.client.dataservicecontext.aspx">DataServiceContext
 *      Class on MSDN</a>
 */
public class Session {

    /** The credentials used to authenticate requests. */
    private ChallengeResponse credentials;

    /** The latest request sent to the service. */
    private Request latestRequest;

    /** The response to the latest request. */
    private Response latestResponse;

    /** The internal logger. */
    private Logger logger;

    /** The metadata of the ADO.NET service. */
    private Metadata metadata;

    /** The reference of the ADO.NET service. */
    private Reference serviceRef;

    /**
     * Constructor.
     * 
     * @param serviceRef
     *            The reference to the ADO.NET service.
     */
    public Session(Reference serviceRef) {
        this.serviceRef = serviceRef;
    }

    /**
     * Constructor.
     * 
     * @param serviceUri
     *            The URI of the ADO.NET service.
     */
    public Session(String serviceUri) {
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

        addEntity(getMetadata().getSubpath(source, sourceProperty), target);
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
                + getMetadata().getSubpath(entity)));
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
        deleteEntity(getMetadata().getSubpath(source, sourceProperty, target));
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
            logger = Logger.getLogger(this.getClass().getCanonicalName());
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
                Representation rep = resource.get();

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
     * Returns the reference to the ADO.NET service.
     * 
     * @return The reference to the ADO.NET service.
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
                        getMetadata().getSubpath(entity, propertyName),
                        propertyClass).iterator();

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
                    + getMetadata().getSubpath(entity, propertyName);
            try {
                ClientResource resource = new ClientResource(ref);
                resource.setChallengeResponse(getCredentials());
                Representation rep = resource.get();

                if (resource.getStatus().isSuccess()) {
                    DomRepresentation xmlRep = new DomRepresentation(rep);
                    Node node = xmlRep.getNode("//" + propertyName);
                    if (node != null) {
                        Property property = getMetadata().getProperty(entity,
                                propertyName);
                        try {
                            ReflectUtils.setProperty(entity, property, node
                                    .getTextContent());
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
    private Content toContent(Object entity) throws Exception {
        // Créer un objet Content correspondant à l'objet
        DomRepresentation dr = new DomRepresentation(MediaType.APPLICATION_XML) {

            @Override
            protected Transformer createTransformer() throws IOException {
                Transformer transformer = super.createTransformer();
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,
                        "yes");
                return transformer;
            }

        };
        Document document = dr.getDocument();
        Element properties = document
                .createElementNS(
                        "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata",
                        "properties");

        for (Field field : entity.getClass().getDeclaredFields()) {
            String getter = "get"
                    + field.getName().substring(0, 1).toUpperCase()
                    + field.getName().substring(1);

            for (Method method : entity.getClass().getDeclaredMethods()) {
                if (method.getReturnType() != null
                        && getter.equals(method.getName())
                        && method.getParameterTypes().length == 0) {
                    Element property = document
                            .createElementNS(
                                    "http://schemas.microsoft.com/ado/2007/08/dataservices",
                                    field.getName());
                    Object value = method.invoke(entity, (Object[]) null);
                    Text text = document.createTextNode((value != null) ? value
                            .toString() : "");
                    property.appendChild(text);
                    properties.appendChild(property);
                    break;
                }
            }
        }

        document.appendChild(properties);
        document.normalizeDocument();

        Content content = new Content();
        content.setInlineContent(dr);
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
                + getMetadata().getSubpath(entity)));
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
