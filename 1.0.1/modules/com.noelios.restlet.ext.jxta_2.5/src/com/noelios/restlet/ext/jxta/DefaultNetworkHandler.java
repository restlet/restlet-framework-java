package com.noelios.restlet.ext.jxta;

import com.noelios.restlet.ext.jxta.util.NetworkHandler;
import net.jxta.ext.network.GroupEvent;
import net.jxta.ext.network.NetworkEvent;
import net.jxta.ext.network.NetworkListener;
import net.jxta.peergroup.PeerGroup;
import net.jxta.rendezvous.RendezvousEvent;

import java.util.EventObject;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */
class DefaultNetworkHandler extends NetworkHandler {
    public DefaultNetworkHandler() {
        super(new NetworkListener() {
            public void notify(NetworkEvent ne) {
                StringBuffer msg = new StringBuffer();
                PeerGroup pg = ne.getPeerGroup();

                msg.append("NetworkEvent: ").
                        append(pg.getPeerGroupName()).
                        append(" ");

                EventObject cause = ne.getCause();

                if (cause != null) {
                    msg.append(cause.getClass().getName()).
                            append(" ");

                    if (cause instanceof RendezvousEvent) {
                        RendezvousEvent re = (RendezvousEvent) cause;
                        String p = re.getPeer();
                        String pid = re.getPeerID().toString();
                        int t = re.getType();

                        pg = ne.getPeerGroup();

                        msg.append(pg.getPeerGroupName()).
                                append(" ").
                                append(p).
                                append(" ").
                                append(pid).
                                append(" ").
                                append(t);
                    } else if (cause instanceof GroupEvent) {
                        GroupEvent ge = (GroupEvent) cause;
                        int t = ge.getType();

                        pg = ge.getPeerGroup();

                        msg.append(pg.getPeerGroupName()).
                                append(" ").
                                append(t);
                    }
                }

                System.out.println(msg);
            }
        });
    }
}