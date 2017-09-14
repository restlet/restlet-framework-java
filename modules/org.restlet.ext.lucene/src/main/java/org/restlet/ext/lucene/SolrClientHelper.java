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

package org.restlet.ext.lucene;

import java.io.File;
import java.net.URI;
import java.util.logging.Level;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.SolrQueryResponse;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.connector.ClientHelper;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.lucene.internal.SolrRepresentation;
import org.restlet.ext.lucene.internal.SolrRestletQueryRequest;

/**
 * Solr client connector.
 * 
 * There are two ways of initializing the helped core container. <br>
 * First one : <br>
 * 
 * <pre>
 * Client solrClient = component.getClients().add(SolrClientHelper.SOLR_PROTOCOL);
 * solrClient.getContext().getAttributes().put("CoreContainer", new CoreContainer(...));
 * </pre>
 * 
 * <br>
 * Second one : <br>
 * 
 * <pre>
 * Client solrClient = component.getClients().add(SolrClientHelper.SOLR_PROTOCOL);
 * solrClient.getContext().getParameters().add(&quot;directory&quot;, &quot;...&quot;);
 * solrClient.getContext().getParameters().add(&quot;configFile&quot;, &quot;...&quot;);
 * </pre>
 * 
 * <br>
 * The helper handles "solr://" requests. There is one additional parameter :
 * "DefaultCore" which gives default core for "solr:///..." requests.
 * 
 * @author Remi Dewitte <remi@gide.net>
 */
public class SolrClientHelper extends ClientHelper {

    public final static Protocol SOLR_PROTOCOL = new Protocol("solr", "Solr",
            "Solr indexer helper", Protocol.UNKNOWN_PORT);

    /** The core Solr container. */
    protected CoreContainer coreContainer;

    /**
     * Constructor.
     * 
     * @param client
     *            The client connector.
     */
    public SolrClientHelper(Client client) {
        super(client);
        getProtocols().add(SOLR_PROTOCOL);
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        Reference resRef = request.getResourceRef();
        String path = resRef.getPath();

        if (path != null) {
            path = resRef.getPath(true);
        }

        String coreName = request.getResourceRef().getHostDomain();

        if (StringUtils.isNullOrEmpty(coreName)) {
            coreName = getContext().getParameters().getFirstValue("DefaultCore");
        }

        SolrCore core = coreContainer.getCore(coreName);

        if (core == null) {
            response.setStatus(Status.SERVER_ERROR_INTERNAL, "No such core: " + coreName);
            return;
        }

        // Extract the handler from the path or params
        SolrRequestHandler handler = core.getRequestHandler(path);

        if (handler == null) {
            if ("/select".equals(path) || "/select/".equalsIgnoreCase(path)) {
                String qt = request.getResourceRef().getQueryAsForm().getFirstValue(CommonParams.QT);
                handler = core.getRequestHandler(qt);
                if (handler == null) {
                    response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST,
                            "unknown handler: " + qt);
                    return;
                }
            }
            // Perhaps the path is to manage the cores
            if (handler == null
                    && coreContainer != null
                    && path != null
                    && path.equals(coreContainer.getAdminPath())) {
                handler = coreContainer.getMultiCoreHandler();
            }
        }

        if (handler == null) {
            core.close();
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "unknown handler: " + path);
            return;
        }

        try {
            SolrQueryRequest solrReq = new SolrRestletQueryRequest(request, core);
            SolrQueryResponse solrResp = new SolrQueryResponse();
            core.execute(handler, solrReq, solrResp);

            if (solrResp.getException() != null) {
                response.setStatus(Status.SERVER_ERROR_INTERNAL, solrResp.getException());
            } else {
                response.setEntity(new SolrRepresentation(
                        MediaType.APPLICATION_XML, solrReq, solrResp));
                response.setStatus(Status.SUCCESS_OK);
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING,
                    "Unable to evaluate " + resRef.toString(), e);
            response.setStatus(Status.SERVER_ERROR_INTERNAL, e);
        } finally {
            core.close();
        }
    }

    @Override
    public void start() {
        try {
            coreContainer = (CoreContainer) getHelped().getContext()
                    .getAttributes().get("CoreContainer");

            if (coreContainer == null) {
                String directory = getHelped().getContext().getParameters()
                        .getFirstValue("directory");
                String configFile = getHelped().getContext().getParameters()
                        .getFirstValue("configFile");

                if (directory != null && configFile != null) {
                    File config = new File(configFile);
                    if (!config.exists()) {
                        config = new File(new URI(configFile));
                    }
                    coreContainer = CoreContainer.createAndLoad(directory,
                            config);
                }
            }

            if (coreContainer == null) {
                throw new RuntimeException(
                        "Could not initialize core container");
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize core container", e);
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }

}
