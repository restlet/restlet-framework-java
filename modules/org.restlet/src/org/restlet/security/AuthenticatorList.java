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

package org.restlet.security;

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.WrapperList;

/**
 * Special authenticator composed of a list of child authenticators. A mode can
 * also be defined to configure the authenticator strategy, i.e. what is
 * required from child authenticators to consider a request fully authenticated.
 * 
 * @author Jerome Louvel
 */
public class AuthenticatorList extends WrapperList<Authenticator> implements
        Authenticator {

    public static final int MODE_ALL_AVAILABLE_REQUIRED = 1;

    public static final int MODE_ALL_REQUIRED = 2;

    public static final int MODE_FIRST_REQUIRED = 3;

    public static final int MODE_ONE_AVAILABLE_REQUIRED = 4;

    public static final int MODE_SECOND_REQUIRED = 5;

    public static final int MODE_THIRD_REQUIRED = 6;

    public static final int MODE_THREE_AVAILABLE_REQUIRED = 7;

    public static final int MODE_TWO_AVAILABLE_REQUIRED = 8;

    private int mode;

    /**
     * Constructor using the {@link #MODE_FIRST_REQUIRED} mode by default.
     */
    public AuthenticatorList() {
        this.mode = MODE_ONE_AVAILABLE_REQUIRED;
    }

    /**
     * 
     */
    public int authenticate(Request request) {
        switch (getMode()) {
        case MODE_ALL_AVAILABLE_REQUIRED:
            // for (Authenticator authenticator : this) {
            //                
            // }
            break;

        case MODE_ONE_AVAILABLE_REQUIRED:

            break;

        case MODE_TWO_AVAILABLE_REQUIRED:

            break;

        case MODE_THREE_AVAILABLE_REQUIRED:

            break;

        case MODE_ALL_REQUIRED:

            break;

        case MODE_FIRST_REQUIRED:

            break;

        case MODE_SECOND_REQUIRED:

            break;

        case MODE_THIRD_REQUIRED:

            break;

        default:

            break;
        }

        return 0;
    }

    public void challenge(Response response, boolean stale) {
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int require) {
        this.mode = require;
    }
}
