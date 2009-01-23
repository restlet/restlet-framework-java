/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.jxta;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EventObject;

import net.jxta.exception.ConfiguratorException;
import net.jxta.ext.configuration.AbstractConfigurator;
import net.jxta.ext.configuration.Configurator;
import net.jxta.ext.configuration.Profile;
import net.jxta.ext.network.GroupEvent;
import net.jxta.ext.network.Network;
import net.jxta.ext.network.NetworkEvent;
import net.jxta.ext.network.NetworkException;
import net.jxta.impl.protocol.PlatformConfig;
import net.jxta.peergroup.PeerGroup;
import net.jxta.rendezvous.RendezvousEvent;

/**
 * Handle the access to the JXTA network.
 * 
 * @author James Todd (james dot w dot todd at gmail dot com)
 */
public class NetworkHandler {

    private class NetworkListener implements
            net.jxta.ext.network.NetworkListener {

        public void notify(NetworkEvent ne) {
            final StringBuffer msg = new StringBuffer();
            PeerGroup pg = ne.getPeerGroup();

            msg.append("NetworkEvent: ").append(pg.getPeerGroupName()).append(
                    " ");

            final EventObject cause = ne.getCause();

            if (cause != null) {
                msg.append(cause.getClass().getName()).append(" ");

                if (cause instanceof RendezvousEvent) {
                    final RendezvousEvent re = (RendezvousEvent) cause;
                    final String p = re.getPeer();
                    final String pid = re.getPeerID().toString();
                    final int t = re.getType();

                    pg = ne.getPeerGroup();

                    msg.append(pg.getPeerGroupName()).append(" ").append(p)
                            .append(" ").append(pid).append(" ").append(t);
                } else if (cause instanceof GroupEvent) {
                    final GroupEvent ge = (GroupEvent) cause;
                    final int t = ge.getType();

                    pg = ge.getPeerGroup();

                    msg.append(pg.getPeerGroupName()).append(" ").append(t);
                }
            }

            System.out.println(msg);
        }
    }

    public static final String JXTA_HOME = "JXTA_HOME";

    public static final String PROFILE = "PROFILE";

    // todo: configuration hack
    private static final URI HOME = new File(System.getProperty(JXTA_HOME,
            System.getProperty("user.dir") + File.separator + ".jxta")).toURI();

    private static final String PROFILE_RESOURCE = System.getProperty(PROFILE,
            "/com/noelios/restlet/ext/jxta/adhoc.xml");

    private static final String CONFIG_NAME = "restlet";

    private static final String CONFIG_USER = "usr";

    private static final String CONFIG_PASSWORD = "pwd";

    private volatile Network network = null;

    private volatile NetworkListener listener = null;

    public NetworkHandler() {
        this.listener = new NetworkListener();
    }

    public Network getNetwork() {
        return this.network;
    }

    public void start() throws NetworkException {
        if (this.network != null) {
            return;
        }

        try {
            this.network = new Network(new AbstractConfigurator(HOME, Profile
                    .get(getClass().getResource(PROFILE_RESOURCE).toURI())) {
                @Override
                public PlatformConfig createPlatformConfig(Configurator c)
                        throws ConfiguratorException {
                    c.setName(CONFIG_NAME);
                    c.setSecurity(CONFIG_USER, CONFIG_PASSWORD);

                    return c.getPlatformConfig();
                }
            }, this.listener);

            this.network.start();
        } catch (URISyntaxException use) {
            throw new NetworkException("invalid uri: "
                    + getClass().getResource(PROFILE_RESOURCE), use);
        }

        // todo: fix, profile.adhoc !-> PlatformConfig.RdvConfig.mode
        // while (! network.isConnected()) {
        // try {
        // Thread.sleep(500);
        // } catch (InterruptedException ie) {
        // // ignore
        // }
        // }
    }

    public void stop() {
        if (this.network == null) {
            return;
        }

        this.network.stop();

        this.network = null;
    }
}