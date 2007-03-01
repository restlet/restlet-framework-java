/*
 * Copyright 2007 Noelios Consulting.
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

package com.noelios.restlet.ext.jxta.util;

import net.jxta.document.AdvertisementFactory;
import net.jxta.peergroup.PeerGroup;
import net.jxta.pipe.PipeID;
import net.jxta.pipe.PipeService;
import net.jxta.protocol.PipeAdvertisement;
import net.jxta.id.IDFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author james todd [james dot w dot todd at gmail dot com]
 */

public class PipeUtil {

    private static final String MESSAGE_DIGEST = "MD5";

    public static URI getURI(PipeAdvertisement pipe)
            throws URISyntaxException {
        String pid = pipe.getPipeID().toString();
        String ptype = pipe.getType();
        int i = pid.lastIndexOf(":") + 1;
        String scheme = PipeService.UnicastType;

        if (ptype.equals(PipeService.UnicastType)) {
            scheme = Constants.Protocol.P2PP.getProtocol();
        } else if (ptype.equals(PipeService.UnicastSecureType)) {
            scheme = Constants.Protocol.P2PSP.getProtocol();
        } else if (ptype.equals(PipeService.PropagateType)) {
            scheme = Constants.Protocol.P2MP.getProtocol();
        }

        return new URI(scheme + "://" + pid.substring(i));
    }

    public static PipeAdvertisement createPipeAdvertisement(String name, String type, PeerGroup group,
                                                            PipeID pipeId) {
        PipeAdvertisement pa = (PipeAdvertisement)AdvertisementFactory.
                newAdvertisement(PipeAdvertisement.getAdvertisementType());

        pa.setPipeID(createPipeID(group, pipeId));

        String pn = name;

        if (type.equals(PipeService.UnicastType)) {
            pn = Constants.Pipe.P2PP_NAME.getPipe() + "/" + name;
        } else if (type.equals(PipeService.PropagateType)) {
            pn = Constants.Pipe.P2MP_NAME.getPipe() + "/" + name;
        }

        pa.setName(pn);
        pa.setType(type);

        return pa;
    }

    public static PipeID createPipeID(PeerGroup group, PipeID pipeId) {
        byte[] sb = null;

        if (pipeId != null) {
            String s = pipeId.toString() + ":" + Constants.Http.HTTP_V1_0.getHttp();
            
            try {
                MessageDigest a = MessageDigest.getInstance(MESSAGE_DIGEST);

                a.reset();
                a.update(s.getBytes());

                sb = a.digest();
            } catch (NoSuchAlgorithmException nsae) {
                // todo: do better
                nsae.printStackTrace();
            }
        }

        return sb != null ?
                IDFactory.newPipeID(group.getPeerGroupID(), sb) : IDFactory.newPipeID(group.getPeerGroupID());
    }
}