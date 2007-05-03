package com.noelios.restlet.ext.jxta.client;

import com.noelios.restlet.ext.jxta.util.NetworkHandler;
import com.noelios.restlet.ext.jxta.util.PipeUtility;
import com.noelios.restlet.ext.jxta.prototype.Constants;
import net.jxta.ext.network.NetworkListener;
import net.jxta.ext.network.NetworkEvent;
import net.jxta.ext.network.GroupEvent;
import net.jxta.ext.network.NetworkException;
import net.jxta.socket.JxtaMulticastSocket;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.util.PipeUtilities;

import java.io.IOException;
import java.net.MulticastSocket;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public class Main {

    // todo: test hack
    public static void main(String[] args) {
        // todo: start network
        NetworkHandler network = new NetworkHandler(new NetworkListener() {
            public void notify(NetworkEvent ne) {
                // todo: do better
                System.out.println("NetworkEvent");

                if (ne.getCause() instanceof GroupEvent) {
                    System.out.println("GroupEvent");
                }
            }
        });

        try {
            network.start();
        } catch (NetworkException ne) {
            ne.printStackTrace();
        }

        // todo: start multicast
        MulticastSocket socket = null;
        try {
            PipeAdvertisement pipe = PipeUtility.createPipeAdvertisement(Constants.PROTOTYPE_MULTICAST_PIPE_NAME,
                    PipeService.PropagateType, network.getNetwork().getNetPeerGroup(),
                    PipeID.create(Constants.PROTOTYPE_MULTICAST_PIPE_ID));

            socket = new JxtaMulticastSocket(network.getNetwork().getNetPeerGroup(), pipe);

            socket.setSoTimeout(0);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // todo: ping

        // todo: exit
        socket.disconnect();
        socket.close();
        network.stop();

        System.out.println("exiting");
    }
}
