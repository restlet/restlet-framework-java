/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.lucene;

import java.util.LinkedHashMap;

import org.apache.solr.common.params.MultiMapSolrParams;
import org.restlet.data.Form;
import org.restlet.data.Request;

/**
 * Reads a Restlet query parameters and adds them to the Solr params map.
 * 
 * @author Rémi Dewitte <remi@gide.net>
 */
public class RestletSolrParams extends MultiMapSolrParams {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * 
     * @param request
     *            The wrapped Restlet request.
     */
    public RestletSolrParams(Request request) {
        super(new LinkedHashMap<String, String[]>());
        formToMultiMap(request.getResourceRef().getQueryAsForm());
        // formToMultiMap(request.getEntityAsForm());
    }

    /**
     * Reads a Restlet form and adds its parameters to the Solr params map.
     * 
     * @param form
     *            The Restlet form to read.
     */
    protected void formToMultiMap(Form form) {
        for (String name : form.getNames()) {
            map.put(name, form.getValuesArray(name));
        }
    }
}