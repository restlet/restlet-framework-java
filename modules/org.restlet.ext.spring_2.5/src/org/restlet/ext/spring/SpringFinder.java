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
package org.restlet.ext.spring;

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Finder;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Resource;

/**
 * Finder that is specialized for easier usage by Spring wiring services. The
 * idea is to create a singleton Spring bean based on that SpringFinder and
 * configure it using Spring's "lookup-method" element to return instances of a
 * "prototype" bean for {@link #createResource()}. Finally, attach the
 * SpringFinder to your Router. When the createResource() method is invoked, a
 * new instance of your prototype bean will be created and returned. A sample
 * xml for "lookup-method":
 * 
 * <pre>
 *      &lt;bean id=&quot;myFinder&quot; class=&quot;org.restlet.ext.spring.SpringFinder&quot;&gt; 
 *              &lt;lookup-method name=&quot;createResource&quot; bean=&quot;myResource&quot;/&gt; 
 *      &lt;/bean&gt;
 *       
 *      &lt;bean id=&quot;myResource&quot; class=&quot;com.mycompany.rest.resource.MyResource&quot; scope=&quot;prototype&quot;&gt; 
 *              &lt;property name=&quot;aProperty&quot; value=&quot;anotherOne&quot;/&gt; 
 *              &lt;property name=&quot;oneMore&quot; value=&quot;true&quot;/&gt;
 *      &lt;/bean&gt;
 * </pre>
 * 
 * Note that the <a href="http://cglib.sourceforge.net/">Code Generation
 * Library</a> (cglib) will be required in order to use the Spring's lookup
 * method mechanism.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @see <a href="http://www.springframework.org/">Spring home page< /a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class SpringFinder extends Finder {

    /**
     * Constructor.
     */
    public SpringFinder() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     */
    public SpringFinder(Context context) {
        super(context);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param targetClass
     *            The target resource class.
     */
    public SpringFinder(Context context, Class<? extends Resource> targetClass) {
        super(context, targetClass);
    }

    /**
     * Creates a new instance of the resource class designated by the
     * "targetClass" property. For easier Spring configuration, the default
     * target resource's constructor is invoked. The created instance is
     * initialized by the calling {@link #createResource(Request, Response)}
     * method, by invoking the {@link Resource#init(Context, Request, Response)}
     * method on the resource.
     * 
     * @return The created resource or null.
     */
    public Resource createResource() {
        Resource result = null;

        if (getTargetClass() != null) {
            try {
                // Invoke the default constructor
                result = (Resource) getTargetClass().newInstance();
            } catch (final Exception e) {
                getLogger()
                        .log(
                                Level.WARNING,
                                "Exception while instantiating the target resource.",
                                e);
            }
        }

        return result;
    }

    @Override
    public Resource createTarget(Request request, Response response) {
        final Resource result = createResource();

        if (result != null) {
            result.init(getContext(), request, response);
        }

        return result;
    }

}
