/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.data;

import org.restlet.util.Series;

/**
 * Metadata preference definition.
 * 
 * @author Jerome Louvel
 */
public final class Preference<T extends Metadata> {
    /** The metadata associated with this preference. */
    private volatile T metadata;

    /** The modifiable list of parameters. */
    private volatile Series<Parameter> parameters;

    /** The quality/preference level. */
    private volatile float quality;

    /**
     * Constructor.
     */
    public Preference() {
        this(null, 1F, null);
    }

    /**
     * Constructor.
     * 
     * @param metadata
     *            The associated metadata.
     */
    public Preference(T metadata) {
        this(metadata, 1F, null);
    }

    /**
     * Constructor.
     * 
     * @param metadata
     *            The associated metadata.
     * @param quality
     *            The quality/preference level.
     */
    public Preference(T metadata, float quality) {
        this(metadata, quality, null);
    }

    /**
     * Constructor.
     * 
     * @param metadata
     *            The associated metadata.
     * @param quality
     *            The quality/preference level.
     * @param parameters
     *            The list of parameters.
     */
    public Preference(T metadata, float quality, Series<Parameter> parameters) {
        this.metadata = metadata;
        this.quality = quality;
        this.parameters = parameters;
    }

    /**
     * Returns the metadata associated with this preference.
     * 
     * @return The metadata associated with this preference.
     */
    public T getMetadata() {
        return this.metadata;
    }

    /**
     * Returns the modifiable list of parameters. Creates a new instance if no
     * one has been set.
     * 
     * @return The modifiable list of parameters.
     */
    public Series<Parameter> getParameters() {
        // Lazy initialization with double-check.
        Series<Parameter> p = this.parameters;
        if (p == null) {
            synchronized (this) {
                p = this.parameters;
                if (p == null) {
                    this.parameters = p = new Form();
                }
            }
        }
        return p;
    }

    /**
     * Returns the quality/preference level.
     * 
     * @return The quality/preference level.
     */
    public float getQuality() {
        return this.quality;
    }

    /**
     * Sets the metadata associated with this preference.
     * 
     * @param metadata
     *            The metadata associated with this preference.
     */
    public void setMetadata(T metadata) {
        this.metadata = metadata;
    }

    /**
     * Sets the modifiable list of parameters.
     * 
     * @param parameters
     *            The modifiable list of parameters.
     */
    public void setParameters(Series<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Sets the quality/preference level.
     * 
     * @param quality
     *            The quality/preference level.
     */
    public void setQuality(float quality) {
        this.quality = quality;
    }

    @Override
    public String toString() {
        return (getMetadata() == null) ? ""
                : (getMetadata().getName() + ":" + getQuality());
    }
}
