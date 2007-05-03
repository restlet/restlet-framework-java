package com.noelios.restlet.ext.jxta.prototype;

import com.noelios.restlet.ext.jxta.*;
import com.noelios.restlet.ext.jxta.util.NetworkHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
public class Main {

    private static final String CLIENT = "client";
    private static final String SERVER = "server";
    private static final String JXTA_PROPERTIES =
        "/com/noelios/restlet/ext/jxta/prototype/resources/jxta.properties";
    private static final String CONNECTION_ID_KEY = "connection.id";
    private static final String CONNECTION_NAME_KEY = "connection.name";
    private static final String PEER_HOME = "peer.home";
    private static final String PROFILE_CLIENT = "profile.client";
    private static final String PROFILE_SERVER = "profile.server";

    public static void main(String[] args) {

        List<String> options = Arrays.asList(args);
        boolean isClient = options.contains("-" + CLIENT);
        Properties config = new Properties();

        try {
            config.load(Main.class.getResourceAsStream(JXTA_PROPERTIES));
        } catch (IOException ioe) {
            // ignore
        }

        Main m = new Main();
        String base = System.getProperty(config.getProperty(PEER_HOME)) + File.separator + ".restlet.jxta";
        String home = base + (isClient ? ".client" : ".server");
        // todo: adhoc profile config not reflected in derived PlatformConfig
        String profile = isClient ? config.getProperty(PROFILE_CLIENT) : config.getProperty(PROFILE_SERVER);;
        Peer peer = new DefaultPeer(home, profile);
        URI id = URI.create(config.getProperty(CONNECTION_ID_KEY));
        String name = config.getProperty(CONNECTION_NAME_KEY);
        NetworkHandler networkHandler = peer.getNetworkHandler();

        class EchoAsynchronousConnection extends DefaultAsynchronousConnection {
            public EchoAsynchronousConnection(URI id, String name, NetworkHandler networkHandler) {
                super(id, name, networkHandler, null);

                setConnectionListener(new ConnectionListener() {
                    public void receiveFrom(byte[] data, InetAddress from) {
                        System.out.println("inbound: " + new String(data));
                        // todo: discard messages from self
                        System.out.println("from: " + from);
                        ByteArrayOutputStream ba = new ByteArrayOutputStream();

                        try {
                            ba.write("echo: ".getBytes());
                            ba.write(data);

                            EchoAsynchronousConnection.this.sendTo(ba.toByteArray(), from);
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                });
            }
        }

        // todo: connection factory provisions connections based on scheme
        AsynchronousConnection connection = isClient ?
                new DefaultAsynchronousConnection(id, name, networkHandler, new ConnectionListener() {
                    public void receiveFrom(byte[] data, InetAddress from) {
                        System.out.println("inbound: " + new String(data));
                        System.out.println("from: " + from);
                    }
                }) :
                new EchoAsynchronousConnection(id, name, networkHandler);

        peer.addConnections(connection);
        peer.start();

        if (isClient) {
            while (true) {
                try {
                    String msg = "ping";
                    System.out.println("outbound: " + msg);
                    connection.send(msg.getBytes());
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

                try {
                    Thread.sleep(20000);
                } catch (InterruptedException ie) {
                    // ignore
                }
            }
        }

//        peer.stop();
    }
}