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

package org.restlet.data;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.engine.util.SystemUtils;

/**
 * Authentication challenge sent by an origin server to a client. Upon reception
 * of this request, the client should send a new request with the proper {@link ChallengeResponse} set.<br>
 * <br>
 * Note that when used with HTTP connectors, this class maps to the
 * "WWW-Authenticate" header.
 * 
 * @author Jerome Louvel
 */
public final class ChallengeRequest extends ChallengeMessage {

    /** The available options for quality of protection. */
    private volatile List<String> qualityOptions;

    /** The URI references that define the protection domains. */
    private volatile List<Reference> domainRefs;

    /** Indicates if the previous request from the client was stale. */
    private volatile boolean stale;

    /**
     * Constructor.
     * 
     * @param scheme
     *            The challenge scheme.
     */
    public ChallengeRequest(ChallengeScheme scheme) {
        this(scheme, null);
    }

    /**
     * Constructor.
     * 
     * @param scheme
     *            The challenge scheme.
     * @param realm
     *            The authentication realm.
     */
    public ChallengeRequest(ChallengeScheme scheme, String realm) {
        super(scheme, realm);
        this.domainRefs = null;
        this.qualityOptions = null;
        this.stale = false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ChallengeRequest)) {
            return false;
        }

        final ChallengeRequest that = (ChallengeRequest) obj;

        return getParameters().equals(that.getParameters())
                && Objects.equals(getRealm(), that.getRealm())
                && Objects.equals(getScheme(), that.getScheme());
    }

    /**
     * Returns the base URI references that collectively define the protected
     * domains for the digest authentication. By default it return a list with a
     * single "/" URI reference.
     * 
     * @return The base URI references.
     */
    public List<Reference> getDomainRefs() {
        // Lazy initialization with double-check.
        List<Reference> r = this.domainRefs;
        if (r == null) {
            synchronized (this) {
                r = this.domainRefs;
                if (r == null) {
                    this.domainRefs = r = new CopyOnWriteArrayList<Reference>();
                    this.domainRefs.add(new Reference("/"));
                }
            }
        }
        return r;
    }

    /**
     * Returns the available options for quality of protection. The default
     * value is {@link #QUALITY_AUTHENTICATION}.
     * 
     * @return The available options for quality of protection.
     */
    public List<String> getQualityOptions() {
        // Lazy initialization with double-check.
        List<String> r = this.qualityOptions;
        if (r == null) {
            synchronized (this) {
                r = this.qualityOptions;
                if (r == null) {
                    this.qualityOptions = r = new CopyOnWriteArrayList<String>();
                    this.qualityOptions.add(QUALITY_AUTHENTICATION);
                }
            }
        }
        return r;
    }
    
    @Override
    public int hashCode() {
        return SystemUtils.hashCode(super.hashCode(), qualityOptions, domainRefs, stale);
    }

    /**
     * Indicates if the previous request from the client was stale.
     * 
     * @return True if the previous request from the client was stale.
     */
    public boolean isStale() {
        return stale;
    }

    /**
     * Sets the URI references that define the protection domains for the digest
     * authentication.
     * 
     * @param domainRefs
     *            The base URI references.
     */
    public void setDomainRefs(List<Reference> domainRefs) {
        this.domainRefs = domainRefs;
    }

    /**
     * Sets the URI references that define the protection domains for the digest
     * authentication. Note that the parameters are copied into a new {@link CopyOnWriteArrayList} instance.
     * 
     * @param domainUris
     *            The base URI references.
     * @see #setDomainRefs(List)
     */
    public void setDomainUris(Collection<String> domainUris) {
        List<Reference> domainRefs = null;

        if (domainUris != null) {
            domainRefs = new CopyOnWriteArrayList<Reference>();

            for (String domainUri : domainUris) {
                domainRefs.add(new Reference(domainUri));
            }
        }

        setDomainRefs(domainRefs);
    }

    /**
     * Sets the available options for quality of protection. The default value
     * is {@link #QUALITY_AUTHENTICATION}.
     * 
     * @param qualityOptions
     *            The available options for quality of protection.
     */
    public void setQualityOptions(List<String> qualityOptions) {
        this.qualityOptions = qualityOptions;
    }

    /**
     * Indicates if the previous request from the client was stale.
     * 
     * @param stale
     *            True if the previous request from the client was stale.
     */
    public void setStale(boolean stale) {
        this.stale = stale;
    }

}
