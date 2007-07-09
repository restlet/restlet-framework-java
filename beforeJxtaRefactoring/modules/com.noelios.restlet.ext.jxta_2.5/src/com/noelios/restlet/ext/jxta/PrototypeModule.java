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

package com.noelios.restlet.ext.jxta;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
public class PrototypeModule extends AbstractModule {

    // private static final String CLIENT = "client";
    private static final String SERVER = "server";

    private static final String JXTA_PROPERTIES = "/com/noelios/restlet/ext/jxta/prototype/resources/jxta.properties";

    private Class main;

    private List<String> options;

    private Properties properties = new Properties();

    public PrototypeModule(Class main, List<String> options) {
        this.main = main;
        this.options = options;

        try {
            properties.load(getClass().getResourceAsStream(JXTA_PROPERTIES));
        } catch (IOException ioe) {
            // ignore
        }
    }

    @SuppressWarnings("unchecked")
    public void configure() {
        boolean isServer = options.contains("-" + SERVER);
        String base = System.getProperty("peer.home", System
                .getProperty("user.dir"))
                + File.separator + ".restlet.jxta.";

        bind(main);
        // bind(Peer.class).to(isServer ? ServerPeer.class : ClientPeer.class);
        bindConstant().annotatedWith(Names.named("peer.name")).to(
                getProperty("peer.name"));
        bindConstant().annotatedWith(Names.named("peer.id")).to(
                getProperty("peer.id"));
        bindConstant().annotatedWith(Names.named("peer.home")).to(
                base + (isServer ? "server" : "client"));
        bindConstant().annotatedWith(Names.named("peer.profile")).to(
                getProperty(isServer ? "profile.server" : "profile.client"));
    }

    private String getProperty(String key) {
        String p = properties.getProperty(key);

        return p != null ? p.trim() : p;
    }
}
