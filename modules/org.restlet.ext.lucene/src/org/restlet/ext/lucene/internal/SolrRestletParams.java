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

package org.restlet.ext.lucene.internal;

import java.util.Iterator;

import org.apache.solr.common.params.SolrParams;
import org.restlet.Request;
import org.restlet.data.Form;

/**
 * Wrap Restlet query parameters as Solr params.
 * 
 * @author Remi Dewitte <remi@gide.net>
 */
public class SolrRestletParams extends SolrParams {

    private static final long serialVersionUID = 1L;

    /** The wrapped Restlet request. */
    private final Request request;

    /**
     * Constructor.
     * 
     * @param request
     *            The wrapped Restlet request.
     */
    public SolrRestletParams(Request request) {
        this.request = request;
    }

    /**
     * Reads parameter from the form returned {@link #getForm()}.
     * 
     */
    @Override
    public String get(String param) {
        return getForm().getFirstValue(param);
    }

    /**
     * Returns the request query form.
     * 
     * @return The request query form.
     */
    protected Form getForm() {
        return request.getResourceRef().getQueryAsForm();
    }

    /**
     * Reads parameter names from the form returned {@link #getForm()}.
     * 
     */
    @Override
    public Iterator<String> getParameterNamesIterator() {
        return getForm().getNames().iterator();
    }

    /**
     * Reads parameter values from the form returned {@link #getForm()}.
     * 
     */
    @Override
    public String[] getParams(String param) {
        return getForm().getValuesArray(param);
    }

}
