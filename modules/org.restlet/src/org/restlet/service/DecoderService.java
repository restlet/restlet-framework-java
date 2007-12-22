/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.service;

/**
 * Service automatically decoding or decompressing request entities.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class DecoderService {
    /** Indicates if the service has been enabled. */
    private volatile boolean enabled;

    /**
     * Constructor.
     * 
     * @param enabled
     *                True if the service has been enabled.
     */
    public DecoderService(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Indicates if the service should be enabled.
     * 
     * @return True if the service should be enabled.
     */
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Indicates if the service should be enabled.
     * 
     * @param enabled
     *                True if the service should be enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
