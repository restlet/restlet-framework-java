/**
 * Copyright 2005-2012 Restlet S.A.S.
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
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.osgi;

import java.util.Dictionary;

import org.osgi.service.component.ComponentContext;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.resource.Directory;

/**
 * @author Bryan Hunt
 * 
 */
public class DirectoryProvider extends RestletProvider implements
        IDirectoryProvider {
    private boolean deeplyAccessible = true;

    private Directory directory;

    private String indexName = "index";

    private boolean modifiable = false;

    private boolean negotiatingContent = true;

    private String path;

    private String rootUri;

    protected void activate(ComponentContext context) {
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
