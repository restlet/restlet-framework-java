/*
 * Copyright 2005-2008 Noelios Consulting.
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
package org.restlet.ext.jaxrs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.ApplicationConfig;

import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * @author Stephan Koops
 * @deprecated This class does not, what a Component typically do.
 */
@Deprecated
public class JaxRsComponent extends Component {

    /**
     * 
     */
    private static final int DEFAULT_PORT = 8080;

    /**
     * reads the port from the command line arguments (the first value parseable
     * as {@link Integer}). Returns a default port, if no one was found.
     * 
     * @param arguments
     *                command line arguments to extract the port.
     * @return the port to use.
     */
    private static int getPort(List<String> arguments) {
        Integer port = null;
        Iterator<String> argIter = arguments.iterator();
        while (argIter.hasNext()) {
            String arg = argIter.next();
            try {
                port = new Integer(arg);
                argIter.remove();
                break;
            } catch (NumberFormatException e) {
                // try next arg
            }
        }
        if (port == null) {
            port = DEFAULT_PORT;
            String noPortGivenMessage = "No port given. Use port "
                    + port
                    + ". You can give a port by set it as command line argument.";
            System.out.println(noPortGivenMessage);
        }
        return port;
    }

    /**
     * @param args
     *                List of classes to load. May contain a port.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.out
                    .println("Run some ApplicationConfigs by give the class names (including package name)\nas command line arguments.");
            System.out.println("You can also give a port or an AccessControl.");
            return;
        }
        JaxRsComponent component = new JaxRsComponent();
        List<String> arguments = new ArrayList<String>(Arrays.asList(args));
        int port = getPort(arguments);
        Server server = component.getServers().add(Protocol.HTTP, port);

        int numberAttached = 0;
        for (String arg : arguments) {
            try {
                component.attach(arg);
                System.out.println("Attached class " + arg);
                numberAttached++;
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
                System.out.println("ignore " + arg);
            } catch (ClassNotFoundException e) {
                System.out.println("Class " + e.getMessage()
                        + " does not exist");
                System.out.println("ignore " + arg);
            }
        }

        if (numberAttached == 0) {
            System.out.println("No class attached. Will finish");
            return;
        }

        for (String rootUris : component.jaxRsApplication.getRootUris()) {
            System.out.println("http://localhost:" + server.getPort()
                    + rootUris);
        }
        
        component.getDefaultHost().attach(component.jaxRsApplication);

        component.start();

        System.out.println("Press key to stop server");
        System.in.read();
        System.out.println("Stopping server");
        component.stop();
        System.out.println("Server stopped");
    }

    private JaxRsApplication jaxRsApplication;

    /**
     * Default constructor.
     */
    public JaxRsComponent() {
        super();
        this.jaxRsApplication = new JaxRsApplication(this.getContext());
    }

    /**
     * Attaches an application created from a WADL description document to the
     * component.
     * 
     * @param appConfig
     *                The {@link ApplicationConfig} to attach.
     * @see JaxRsRouter#attach(ApplicationConfig)
     */
    public void attach(ApplicationConfig appConfig) {
        this.jaxRsApplication.attach(appConfig);
    }

    /**
     * Attaches an application created from a WADL description document
     * available at a given URI reference.
     * 
     * @param clazz
     *                The {@link ApplicationConfig} class to attach.
     * @throws IllegalArgumentException
     *                 if the class could not be used.
     */
    public void attach(Class<?> clazz) throws IllegalArgumentException {
        Object instance;
        try {
            instance = clazz.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Could not instantiate class "
                    + clazz.getClass().getName(), e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Could not instantiate class "
                    + clazz.getClass().getName(), e);
        }
        if (instance instanceof ApplicationConfig)
            this.attach((ApplicationConfig) instance);
        else if (instance instanceof AccessControl)
            this.setAccessControl((AccessControl) instance);
        else
            throw new IllegalArgumentException("The class " + clazz.getName()
                    + " is not valid for a JaxRsComponent");
    }

    /**
     * Attaches an {@link ApplicationConfig} to this component.
     * 
     * @param className
     *                The class name of the {@link ApplicationConfig} subclass.
     * @throws ClassNotFoundException
     *                 if the class woh the icen name could not be found.
     * @throws IllegalArgumentException
     *                 if the class could not be used.
     */
    @SuppressWarnings("unchecked")
    public void attach(String className) throws ClassNotFoundException,
            IllegalArgumentException {
        attach(Class.forName(className));
    }

    /**
     * @param accessControl
     * @see JaxRsRouter#setAccessControl(AccessControl)
     */
    public void setAccessControl(AccessControl accessControl) {
        this.jaxRsApplication.setAccessControl(accessControl);
    }
}