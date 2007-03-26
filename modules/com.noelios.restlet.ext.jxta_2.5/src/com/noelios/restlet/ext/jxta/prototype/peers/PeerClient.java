package com.noelios.restlet.ext.jxta.prototype.peers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.noelios.restlet.ext.jxta.util.PipeUtility;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.socket.JxtaMulticastSocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.URI;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
public class PeerClient extends PeerBase {

    private MulticastSocket socket;

    @Inject
    public PeerClient(@Named("peer.name")String name,
                      @Named("peer.id")String id,
                      @Named("peer.home")String home,
                      @Named("peer.profile")String profile) {
        super(name, URI.create(id), home, profile);
    }

    public void start() {
        super.start();

        try {
            PipeAdvertisement pipe = PipeUtility.createPipeAdvertisement(getName(),
                    PipeService.PropagateType, getNetworkHandler().getNetwork().getNetPeerGroup(),
                    PipeID.create(getId()));

            socket = new JxtaMulticastSocket(getNetworkHandler().getNetwork().getNetPeerGroup(), pipe);

            socket.setSoTimeout(0);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        for (int i = 0; i < 10; i++) {
            System.out.println("sleeping ...");
            try {
                Thread.sleep(3000);
            } catch (Exception e) {
            }
            
            System.out.println("send ping");

            String ping = "ping";
            byte[] bytes = ping.getBytes();
            DatagramPacket message = new DatagramPacket(bytes, bytes.length);

            try {
                socket.send(message);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        // todo: read ping
    }

    public void stop() {
        socket.disconnect();
        socket.close();

        super.stop();
    }
}
