/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

package org.restlet.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.security.Realm;

/**
 * Component service that manages the available security realms.
 * 
 * @author Jerome Louvel
 */
public class RealmService extends Service {

    /** The modifiable list of security realms. */
    private List<Realm> realms;

    /**
     * Constructor.
     */
    public RealmService() {
        this.realms = new CopyOnWriteArrayList<Realm>();
    }

    /**
     * Constructor.
     * 
     * @param enabled
     *            True if the service has been enabled.
     */
    public RealmService(boolean enabled) {
        super(enabled);
        this.realms = new CopyOnWriteArrayList<Realm>();
    }

    /**
     * Returns the modifiable list of security realms.
     * 
     * @return The modifiable list of security realms.
     */
    public List<Realm> getRealms() {
        return realms;
    }

    /**
     * Sets the list of realms.
     * 
     * @param realms
     *            The list of realms.
     */
    public synchronized void setRealms(List<Realm> realms) {
        this.realms.clear();

        if (realms != null) {
            this.realms.addAll(realms);
        }
    }

}
