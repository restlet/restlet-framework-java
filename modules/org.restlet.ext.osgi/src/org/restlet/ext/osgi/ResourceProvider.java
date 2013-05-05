/**
 * Copyright 2005-2013 Restlet S.A.S.
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
import org.restlet.resource.Finder;
import org.restlet.routing.Template;

/**
 * @author Bryan Hunt
 * 
 */
public abstract class ResourceProvider extends RestletProvider implements
        IResourceProvider {
    private Finder finder;

    private String[] paths;
    
    private Integer matchingMode;
    
    protected void activate(ComponentContext context) {
        @SuppressWarnings("unchecked")
        Dictionary<String, Object> properties = context.getProperties();
        paths = (String[]) properties.get("paths");
        matchingMode = (Integer) properties.get("matchingMode");
        
        if (matchingMode == null)
        	matchingMode = Template.MODE_EQUALS;
    }

    protected abstract Finder createFinder(Context context);

    @Override
    protected Restlet getFilteredRestlet() {
        return finder;
    }

    @Override
    public Restlet getInboundRoot(Context context) {
        if (finder == null)
            finder = createFinder(context);

        Restlet inboundRoot = super.getInboundRoot(context);
        return inboundRoot != null ? inboundRoot : finder;
    }

    @Override
    public String[] getPaths() {
        return paths.clone();
    }
    
    @Override
    public int getMatchingMode() {
    	return matchingMode;
    }
}
