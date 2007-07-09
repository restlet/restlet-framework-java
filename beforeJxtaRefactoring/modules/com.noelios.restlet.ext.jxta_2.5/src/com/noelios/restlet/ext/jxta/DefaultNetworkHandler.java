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

                msg.append("NetworkEvent: ").append(pg.getPeerGroupName())
                        .append(" ");

                EventObject cause = ne.getCause();

                if (cause != null) {
                    msg.append(cause.getClass().getName()).append(" ");

                    if (cause instanceof RendezvousEvent) {
                        RendezvousEvent re = (RendezvousEvent) cause;
                        String p = re.getPeer();
                        String pid = re.getPeerID().toString();
                        int t = re.getType();

                        pg = ne.getPeerGroup();

                        msg.append(pg.getPeerGroupName()).append(" ").append(p)
                                .append(" ").append(pid).append(" ").append(t);
                    } else if (cause instanceof GroupEvent) {
                        GroupEvent ge = (GroupEvent) cause;
                        int t = ge.getType();

                        pg = ge.getPeerGroup();

                        msg.append(pg.getPeerGroupName()).append(" ").append(t);
                    }
                }

                System.out.println(msg);
            }
        });
    }
}