/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
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
 * Note that the <a href="http://cglib.sourceforge.net/">Code Generation Library</a>
 * (cglib) will be required in order to use the Spring's lookup method
 * mechanism.
 * 
 * @see <a href="http://www.springframework.org/">Spring home page</a>
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
     *                The context.
     */
    public SpringFinder(Context context) {
        super(context);
    }

    /**
     * Constructor.
     * 
     * @param context
     *                The context.
     * @param targetClass
     *                The target resource class.
     */
    public SpringFinder(Context context, Class<? extends Resource> targetClass) {
        super(context, targetClass);
    }

    @Override
    public Resource createTarget(Request request, Response response) {
        Resource result = createResource();

        if (result != null) {
            result.init(getContext(), request, response);
        }

        return result;
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
            } catch (Exception e) {
                getLogger()
                        .log(
                                Level.WARNING,
                                "Exception while instantiating the target resource.",
                                e);
            }
        }

        return result;
    }

}
