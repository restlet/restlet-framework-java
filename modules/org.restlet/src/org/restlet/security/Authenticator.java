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

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Filter authenticating the subjects sending requests.
 * 
 * @author Jerome Louvel
 */
public abstract class Authenticator extends Filter {
    /**
     * The authenticator is not required to succeed. In all cases, the attached
     * Restlet is invoked.
     */
    public static final int MODE_OPTIONAL = 1;

    /**
     * The authenticator is required to succeed. The attached Restlet is only
     * invoked if it succeeds.
     */
    public static final int MODE_REQUIRED = 2;

    /**
     * The authenticator is not required to succeed. The attached Restlet is
     * only invoked if it fails.
     */
    public static final int MODE_SUFFICIENT = 3;

    /**
     * The authentication mode.
     */
    private volatile int mode;

    /**
     * Default constructor setting the mode to {@link #MODE_REQUIRED}.
     */
    public Authenticator(Context context) {
        this(context, MODE_REQUIRED);
    }

    /**
     * Constructor.
     * 
     * @param mode
     *            The authentication mode.
     */
    public Authenticator(Context context, int mode) {
        super(context);
        this.mode = mode;
    }

    /**
     * Attempts to authenticate the subject sending the request.
     * 
     * @param request
     *            The request sent.
     * @param response
     *            The response to update.
     * @return True if the authentication succeeded.
     */
    protected abstract boolean authenticate(Request request, Response response);

    /**
     * Handles the authentication by first invoking the
     * {@link #authenticate(Request, Response)} method. Then, depending on the
     * result and the mode set, it either skips or invoke the (optionally)
     * attached Restlet.
     */
    @Override
    protected int beforeHandle(Request request, Response response) {
        int result = CONTINUE;

        boolean success = authenticate(request, response);

        switch (getMode()) {
        case MODE_OPTIONAL:
            // We try to continue in all cases
            break;

        case MODE_REQUIRED:
            // We only continue if the authentication succeeded
            if (!success)
                result = SKIP;
            break;

        case MODE_SUFFICIENT:
            // We don't need to continue if the authentication succeeded
            if (success)
                result = SKIP;
            break;

        default:
            result = SKIP;
            break;
        }

        return result;
    }

    /**
     * Returns the authentication mode.
     * 
     * @return The authentication mode.
     */
    public int getMode() {
        return mode;
    }

    /**
     * Sets the authentication mode.
     * 
     * @param mode
     *            The authentication mode.
     */
    public void setMode(int mode) {
        this.mode = mode;
    }

}
