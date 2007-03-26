package com.noelios.restlet.ext.jxta.prototype.configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.noelios.restlet.ext.jxta.prototype.Peer;
import com.noelios.restlet.ext.jxta.prototype.peers.PeerClient;
import com.noelios.restlet.ext.jxta.prototype.peers.PeerServer;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
public class PrototypeModule extends AbstractModule {

//    private static final String CLIENT = "client";
    private static final String SERVER = "server";
    private static final String JXTA_PROPERTIES =
            "/com/noelios/restlet/ext/jxta/prototype/configuration/jxta.properties";

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
        String base = System.getProperty("peer.home", System.getProperty("user.dir")) +
                File.separator + ".restlet.jxta.";

        bind(main);
        bind(Peer.class).to(isServer ? PeerServer.class : PeerClient.class);
        bindConstant().annotatedWith(Names.named("peer.name")).to(getProperty("peer.name"));
        bindConstant().annotatedWith(Names.named("peer.id")).to(getProperty("peer.id"));
        bindConstant().annotatedWith(Names.named("peer.home")).to(base + (isServer ? "server" : "client"));
        bindConstant().annotatedWith(Names.named("peer.profile")).
                to(getProperty(isServer ? "profile.server" : "profile.client"));
    }

    private String getProperty(String key) {
        String p = properties.getProperty(key);

        return p != null ? p.trim() : p;
    }
}
