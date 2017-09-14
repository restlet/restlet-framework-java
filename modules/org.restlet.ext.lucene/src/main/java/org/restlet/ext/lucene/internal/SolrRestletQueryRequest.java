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

import java.util.ArrayList;

import org.apache.solr.common.util.ContentStream;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequestBase;
import org.restlet.Request;

/**
 * Solr query request wrapping a Restlet request.
 * 
 * @author Remi Dewitte <remi@gide.net>
 */
public class SolrRestletQueryRequest extends SolrQueryRequestBase {

    /**
     * Constructor.
     * 
     * @param request
     *            The Restlet request to wrap.
     * @param core
     *            The Solr core.
     */
    public SolrRestletQueryRequest(Request request, SolrCore core) {
        super(core, new SolrRestletParams(request));
        getContext().put("path", request.getResourceRef().getPath());
        ArrayList<ContentStream> _streams = new ArrayList<ContentStream>(1);
        _streams.add(new SolrRepresentationContentStream(request.getEntity()));
        setContentStreams(_streams);
    }

}
