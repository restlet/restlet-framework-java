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

package org.restlet.ext.jaxrs.internal.resteasy;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;

import org.jboss.resteasy.spi.HttpResponse;
import org.restlet.Response;
import org.restlet.data.CookieSetting;

/**
 * RESTEasy HTTP response wrapper for Restlet requests.
 * 
 * @author Jerome Louvel
 */
public class RestletHttpResponse implements HttpResponse {

    /**
     * Converts the Restlet {@link CookieSetting} to a JAX-RS {@link NewCookie}.
     * 
     * @param cookieSetting
     *            The Restlet cookie setting.
     * @return The JAX-RS NewCookie
     * @throws IllegalArgumentException
     */
    public static NewCookie toNewCookie(CookieSetting cookieSetting)
            throws IllegalArgumentException {
        if (cookieSetting == null) {
            return null;
        }
        return new NewCookie(cookieSetting.getName(), cookieSetting.getValue(),
                cookieSetting.getPath(), cookieSetting.getDomain(),
                cookieSetting.getVersion(), cookieSetting.getComment(),
                cookieSetting.getMaxAge(), cookieSetting.isSecure());
    }

    /**
     * Converts the Restlet JAX-RS NewCookie to a CookieSettings.
     * 
     * @param newCookie
     * @return the converted CookieSetting
     * @throws IllegalArgumentException
     */
    public static CookieSetting toRestletCookieSetting(NewCookie newCookie)
            throws IllegalArgumentException {
        if (newCookie == null) {
            return null;
        }
        return new CookieSetting(newCookie.getVersion(), newCookie.getName(),
                newCookie.getValue(), newCookie.getPath(),
                newCookie.getDomain(), newCookie.getComment(),
                newCookie.getMaxAge(), newCookie.isSecure());
    }

    private final Response response;

    public RestletHttpResponse(Response response) {
        this.response = response;
    }

    @Override
    public void addNewCookie(NewCookie cookie) {
        getResponse().getCookieSettings().add(toRestletCookieSetting(cookie));
    }

    @Override
    public MultivaluedMap<String, Object> getOutputHeaders() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Returns the wrapped Restlet response.
     * 
     * @return The wrapped Restlet response.
     */
    public Response getResponse() {
        return this.response;
    }

    @Override
    public int getStatus() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isCommitted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void reset() {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendError(int arg0) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void sendError(int arg0, String arg1) throws IOException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setOutputStream(OutputStream arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStatus(int arg0) {
        // TODO Auto-generated method stub

    }

}
