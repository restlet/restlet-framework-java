/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet
 */

package org.restlet.ext.osgi;

import java.util.Dictionary;

import org.osgi.service.component.ComponentContext;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.resource.Directory;

/**
 * This class provides an implementation of {@link DirectoryProvider}. You
 * register this class as an OSGi declarative service. The service declaration
 * should look like:
 * <p>
 * 
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <scr:component xmlns:scr="http://www.osgi.org/xmlns/scr/v1.1.0" immediate="true" name="org.example.app.directory">
 *   <implementation class="org.restlet.ext.osgi.BaseDirectoryProvider"/>
 *   <property name="path" type="String" value="/myDir"/>
 *   <property name="rootUri" type="String" value="file:/Path/to/dir"/>
 *   <service>
 *     <provide interface="org.restlet.ext.osgi.DirectoryProvider"/>
 *   </service>
 * </scr:component>
 * }
 * </pre>
 * 
 * </p>
 * <p>
 * The service properties are:
 * <ul>
 * <li>path - the path to the resource relative to the application - required -
 * must not be null</li>
 * <li>rootUri - the URI to the directory - required - must not be null</li>
 * <li>indexName - the index name - optional - defaults to "index"</li>
 * <li>deeplyAccessible - Indicates if the sub-directories are deeply accessible
 * - optional - defaults to true</li>
 * <li>modifiable - Indicates if modifications to local resources are allowed -
 * optional - defaults to false</li>
 * <li>negotiatingContent - Indicates if the best content is automatically
 * negotiated - optional - defaults to true</li>
 * </ul>
 * </p>
 * <p>
 * The referenced services are:
 * <ul>
 * <li>FilterProvider - optional - policy="static" cardinality="1..1"</li>
 * </ul>
 * </p>
 * <p>
 * The provided services are:
 * <ul>
 * <li>DirectoryProvider</li>
 * </ul>
 * </p>
 * 
 * @author Bryan Hunt
 * 
 */
public class BaseDirectoryProvider extends BaseRestletProvider implements
        DirectoryProvider {
    private boolean deeplyAccessible = true;

    private Directory directory;

    private String indexName = "index";

    private boolean modifiable = false;

    private boolean negotiatingContent = true;

    private String path;

    private String rootUri;

    /**
     * Called by OSGi DS to activate the service
     * 
     * @param context
     *            the OSGi service context
     */
    public void activate(ComponentContext context) {
        @SuppressWarnings("unchecked")
        Dictionary<String, Object> properties = context.getProperties();

        path = (String) properties.get("path");
        rootUri = (String) properties.get("rootUri");

        String indexName = (String) properties.get("indexName");

        if (indexName != null)
            this.indexName = indexName;

        Boolean deeplyAccessible = (Boolean) properties.get("deeplyAccessible");

        if (deeplyAccessible != null)
            this.deeplyAccessible = deeplyAccessible;

        Boolean modifiable = (Boolean) properties.get("modifiable");

        if (modifiable != null)
            this.modifiable = modifiable;

        Boolean negotiatingContent = (Boolean) properties
                .get("negotiatingContent");

        if (negotiatingContent != null)
            this.negotiatingContent = negotiatingContent;
    }

    /**
     * Creates the Restlet Directory instance using the rootUri, indexName,
     * deeplyAccessible, modifiable, and negotiatingContent service properties
     * 
     * @param context
     *            the Restlet application context
     * @return the configured Restlet Directory
     */
    protected Directory createDirectory(Context context) {
        Directory directory = new Directory(context, rootUri);
        directory.setIndexName(indexName);
        directory.setDeeplyAccessible(deeplyAccessible);
        directory.setModifiable(modifiable);
        directory.setNegotiatingContent(negotiatingContent);
        return directory;
    }

    @Override
    protected Restlet getFilteredRestlet() {
        return directory;
    }

    @Override
    public Restlet getInboundRoot(Context context) {
        if (directory == null)
            directory = createDirectory(context);

        Restlet inboundRoot = super.getInboundRoot(context);
        return inboundRoot != null ? inboundRoot : directory;
    }

    @Override
    public String getPath() {
        return path;
    }
}
