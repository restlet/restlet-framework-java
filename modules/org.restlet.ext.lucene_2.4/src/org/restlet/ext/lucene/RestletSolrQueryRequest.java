/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
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

package org.restlet.ext.lucene;

import java.util.ArrayList;

import org.apache.solr.common.util.ContentStream;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequestBase;
import org.restlet.data.Request;

/**
 * Solr query request wrapping a Restlet request.
 * 
 * @author Rémi Dewitte <remi@gide.net>
 */
public class RestletSolrQueryRequest extends SolrQueryRequestBase {

    /**
     * Constructor.
     * 
     * @param request
     *            The Restlet request to wrap.
     * @param core
     *            The Solr core.
     */
    public RestletSolrQueryRequest(Request request, SolrCore core) {
        super(core, new RestletSolrParams(request));
        getContext().put("path", request.getResourceRef().getPath());
        ArrayList<ContentStream> _streams = new ArrayList<ContentStream>(1);
        _streams.add(new SolrRepresentationContentStream(request.getEntity()));
        setContentStreams(_streams);
    }

}