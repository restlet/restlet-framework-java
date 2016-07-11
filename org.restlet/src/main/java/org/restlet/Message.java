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

package org.restlet;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.data.CacheDirective;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.RecipientInfo;
import org.restlet.data.Warning;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

/**
 * Generic message exchanged between components.
 * 
 * @author Jerome Louvel
 */
public abstract class Message {
    /** The modifiable attributes map. */
    private volatile ConcurrentMap<String, Object> attributes;

    /** The caching directives. */
    private volatile List<CacheDirective> cacheDirectives;

    /** The date and time at which the message was originated. */
    private volatile Date date;

    /** The payload of the message. */
    private volatile Representation entity;

    // [ifndef gwt] member
    /** The optional cached text. */
    private volatile String entityText;

    /** Callback invoked when an error occurs when sending the message. */
    private volatile Uniform onError;

    /** Callback invoked after sending the message. */
    private volatile Uniform onSent;

    /** The intermediary recipients info. */
    private volatile List<RecipientInfo> recipientsInfo;

    /** The additional warnings information. */
    private volatile List<Warning> warnings;

    /**
     * Constructor.
     */
    public Message() {
        this((Representation) null);
    }

    /**
     * Constructor.
     * 
     * @param entity
     *            The payload of the message.
     */
    public Message(Representation entity) {
        this.attributes = null;
        this.cacheDirectives = null;
        this.date = null;
        this.entity = entity;
        // [ifndef gwt] instruction
        this.entityText = null;
        this.onSent = null;
        this.recipientsInfo = null;
        this.warnings = null;
    }

    // [ifndef gwt] method
    /**
     * If the entity is transient or its size unknown in advance but available,
     * then the entity is wrapped with a
     * {@link org.restlet.representation.BufferingRepresentation}.<br>
     * <br>
     * Be careful as this method could create potentially very large byte
     * buffers in memory that could impact your application performance.
     * 
     * @see org.restlet.representation.BufferingRepresentation
     * @see ClientResource#setRequestEntityBuffering(boolean)
     * @see ClientResource#setResponseEntityBuffering(boolean)
     */
    public void bufferEntity() {
        if ((getEntity() != null)
                && (getEntity().isTransient() || (getEntity().getSize() == Representation.UNKNOWN_SIZE))
                && getEntity().isAvailable()) {
            setEntity(new org.restlet.representation.BufferingRepresentation(
                    getEntity()));
        }
    }

    /**
     * Asks the underlying connector to immediately flush the network buffers.
     * 
     * @throws IOException
     */
    public void flushBuffers() throws IOException {
    }

    /**
     * Returns the modifiable map of attributes that can be used by developers
     * to save information relative to the message. Creates a new instance if no
     * one has been set. This is an easier alternative to the creation of a
     * wrapper instance around the whole message.<br>
     * <br>
     * 
     * In addition, this map is a shared space between the developer and the
     * connectors. In this case, it is used to exchange information that is not
     * uniform across all protocols and couldn't therefore be directly included
     * in the API. For this purpose, all attribute names starting with
     * "org.restlet" are reserved. Currently the following attributes are used:
     * <table>
     * <tr>
     * <th>Attribute name</th>
     * <th>Class name</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>org.restlet.http.headers</td>
     * <td>org.restlet.util.Series&lt;org.restlet.engine.header.Header&gt;</td>
     * <td>Server HTTP connectors must provide all request headers and client
     * HTTP connectors must provide all response headers, exactly as they were
     * received. In addition, developers can also use this attribute to specify
     * <b>non-standard</b> headers that should be added to the request or to the
     * response.</td>
     * </tr>
     * <tr>
     * <td>org.restlet.https.clientCertificates</td>
     * <td>List<java.security.cert.Certificate></td>
     * <td>For requests received via a secure connector, indicates the ordered
     * list of client certificates, if they are available and accessible.</td>
     * </tr>
     * </table>
     * <br>
     * Most of the standard HTTP headers are directly supported via the Restlet
     * API. Thus, adding such HTTP headers is forbidden because it could
     * conflict with the connector's internal behavior, limit portability or
     * prevent future optimizations. The other standard HTTP headers (that are
     * not supported) can be added as attributes via the
     * "org.restlet.http.headers" key.<br>
     * 
     * @return The modifiable attributes map.
     */
    public ConcurrentMap<String, Object> getAttributes() {
        // Lazy initialization with double-check.
        ConcurrentMap<String, Object> r = this.attributes;
        if (r == null) {
            synchronized (this) {
                r = this.attributes;
                if (r == null) {
                    this.attributes = r = new ConcurrentHashMap<String, Object>();
                }
            }
        }

        return this.attributes;
    }

    /**
     * Returns the cache directives.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Cache-Control" header.
     * 
     * @return The cache directives.
     */
    public List<CacheDirective> getCacheDirectives() {
        // Lazy initialization with double-check.
        List<CacheDirective> r = this.cacheDirectives;
        if (r == null) {
            synchronized (this) {
                r = this.cacheDirectives;
                if (r == null) {
                    this.cacheDirectives = r = new CopyOnWriteArrayList<CacheDirective>();
                }
            }
        }
        return r;
    }

    /**
     * Returns the date and time at which the message was originated.
     * 
     * @return The date and time at which the message was originated.
     */
    public Date getDate() {
        return date;
    }

    /**
     * Returns the entity representation.
     * 
     * @return The entity representation.
     */
    public Representation getEntity() {
        return this.entity;
    }

    // [ifndef gwt] method
    /**
     * Returns the entity as text. This method can be called several times and
     * will always return the same text. Note that if the entity is large this
     * method can result in important memory consumption.
     * 
     * @return The entity as text.
     */
    public String getEntityAsText() {
        if (this.entityText == null) {
            try {
                this.entityText = (getEntity() == null) ? null : getEntity()
                        .getText();
            } catch (java.io.IOException e) {
                Context.getCurrentLogger().log(java.util.logging.Level.FINE,
                        "Unable to get the entity text.", e);
            }
        }

        return this.entityText;
    }

    /**
     * Returns the series of lower-level HTTP headers. Please not that this
     * method should rarely be used as most HTTP headers are already surfaced by
     * the Restlet API. The result series can be used to deal with HTTP
     * extension headers.
     * 
     * @return The HTTP headers.
     */
    @SuppressWarnings("unchecked")
    public Series<Header> getHeaders() {
        Series<Header> headers = (Series<Header>) getAttributes().get(
                HeaderConstants.ATTRIBUTE_HEADERS);
        if (headers == null) {
            // [ifndef gwt] instruction
            headers = new Series<Header>(Header.class);
            // [ifdef gwt] instruction uncomment
            // headers = new org.restlet.engine.util.HeaderSeries();
            getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, headers);
        }
        return headers;
    }

    /**
     * Returns the callback invoked when an error occurs when sending the
     * message.
     * 
     * @return The callback invoked when an error occurs when sending the
     *         message.
     */
    public Uniform getOnError() {
        return onError;
    }

    /**
     * Returns the callback invoked after sending the message.
     * 
     * @return The callback invoked after sending the message.
     */
    public Uniform getOnSent() {
        return onSent;
    }

    /**
     * Returns the intermediary recipient information.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the "Via"
     * headers.
     * 
     * @return The intermediary recipient information.
     */
    public List<RecipientInfo> getRecipientsInfo() {
        // Lazy initialization with double-check.
        List<RecipientInfo> r = this.recipientsInfo;
        if (r == null) {
            synchronized (this) {
                r = this.recipientsInfo;
                if (r == null) {
                    this.recipientsInfo = r = new CopyOnWriteArrayList<RecipientInfo>();
                }
            }
        }
        return r;
    }

    /**
     * Returns the additional warnings information.<br>
     * <br>
     * Note that when used with HTTP connectors, this property maps to the
     * "Warning" headers.
     * 
     * @return The additional warnings information.
     */
    public List<Warning> getWarnings() {
        // Lazy initialization with double-check.
        List<Warning> r = this.warnings;
        if (r == null) {
            synchronized (this) {
                r = this.warnings;
                if (r == null) {
                    this.warnings = r = new CopyOnWriteArrayList<Warning>();
                }
            }
        }
        return r;
    }

    /**
     * Indicates if the message was or will be exchanged confidentially, for
     * example via a SSL-secured connection.
     * 
     * @return True if the message is confidential.
     */
    public abstract boolean isConfidential();

    /**
     * Indicates if a content is available and can be sent or received. Several
     * conditions must be met: the content must exists and have some available
     * data.
     * 
     * @return True if a content is available and can be sent.
     */
    public boolean isEntityAvailable() {
        // The declaration of the "result" variable is a workaround for the GWT
        // platform. Please keep it!
        boolean result = (getEntity() != null) && getEntity().isAvailable();
        return result;
    }

    /**
     * Releases the message's entity if present.
     * 
     * @see org.restlet.representation.Representation#release()
     */
    public void release() {
        if (getEntity() != null) {
            getEntity().release();
        }
    }

    /**
     * Sets the modifiable map of attributes. This method clears the current map
     * and puts all entries in the parameter map.
     * 
     * @param attributes
     *            A map of attributes
     */
    public void setAttributes(Map<String, Object> attributes) {
        synchronized (getAttributes()) {
            if (attributes != getAttributes()) {
                getAttributes().clear();

                if (attributes != null) {
                    getAttributes().putAll(attributes);
                }
            }
        }
    }

    /**
     * Sets the cache directives. Note that when used with HTTP connectors, this
     * property maps to the "Cache-Control" header. This method clears the
     * current list and adds all entries in the parameter list.
     * 
     * @param cacheDirectives
     *            The cache directives.
     */
    public void setCacheDirectives(List<CacheDirective> cacheDirectives) {
        synchronized (getCacheDirectives()) {
            if (cacheDirectives != getCacheDirectives()) {
                getCacheDirectives().clear();

                if (cacheDirectives != null) {
                    getCacheDirectives().addAll(cacheDirectives);
                }
            }
        }
    }

    /**
     * Sets the date and time at which the message was originated.
     * 
     * @param date
     *            The date and time at which the message was originated.
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Sets the entity representation.
     * 
     * @param entity
     *            The entity representation.
     */
    public void setEntity(Representation entity) {
        this.entity = entity;
    }

    /**
     * Sets a textual entity.
     * 
     * @param value
     *            The represented string.
     * @param mediaType
     *            The representation's media type.
     */
    public void setEntity(String value, MediaType mediaType) {
        setEntity(new StringRepresentation(value, mediaType));
    }

    /**
     * Sets the callback invoked when an error occurs when sending the message.
     * 
     * @param onError
     *            The callback invoked when an error occurs when sending the
     *            message.
     */
    public void setOnError(Uniform onError) {
        this.onError = onError;
    }

    /**
     * Sets the callback invoked after sending the message.
     * 
     * @param onSentCallback
     *            The callback invoked after sending the message.
     */
    public void setOnSent(Uniform onSentCallback) {
        this.onSent = onSentCallback;
    }

    /**
     * Sets the modifiable list of intermediary recipients. Note that when used
     * with HTTP connectors, this property maps to the "Via" headers. This
     * method clears the current list and adds all entries in the parameter
     * list.
     * 
     * @param recipientsInfo
     *            A list of intermediary recipients.
     */
    public void setRecipientsInfo(List<RecipientInfo> recipientsInfo) {
        synchronized (getRecipientsInfo()) {
            if (recipientsInfo != getRecipientsInfo()) {
                getRecipientsInfo().clear();

                if (recipientsInfo != null) {
                    getRecipientsInfo().addAll(recipientsInfo);
                }
            }
        }
    }

    /**
     * Sets the additional warnings information. Note that when used with HTTP
     * connectors, this property maps to the "Warning" headers. This method
     * clears the current list and adds all entries in the parameter list.
     * 
     * @param warnings
     *            The warnings.
     */
    public void setWarnings(List<Warning> warnings) {
        synchronized (getWarnings()) {
            if (warnings != getWarnings()) {
                getWarnings().clear();

                if (warnings != null) {
                    getWarnings().addAll(warnings);
                }
            }
        }
    }

}
