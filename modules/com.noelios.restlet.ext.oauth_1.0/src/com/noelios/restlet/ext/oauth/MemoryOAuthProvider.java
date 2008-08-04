/*
 * Copyright 2005-2008 Noelios Technologies.
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

package com.noelios.restlet.ext.oauth;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthMessage;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Provider with a in-memory backing store.
 * 
 * @author Adam Rosien
 */
public class MemoryOAuthProvider extends OAuthProvider {

    // TODO: Mutexes.
    // TODO: LRU collections.
    private final Map<String, OAuthConsumer> consumers = new HashMap<String, OAuthConsumer>();

    /** The current logger. */
    private final Logger logger;

    private final Set<OAuthAccessor> tokens = new HashSet<OAuthAccessor>();

    /**
     * Constructor.
     * 
     * @param logger
     *            The current logger.
     */
    public MemoryOAuthProvider(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void addConsumer(String key, OAuthConsumer consumer) {
        this.logger.fine("Adding consumer " + consumer);
        this.consumers.put(key, consumer);
    }

    @Override
    public void generateAccessToken(OAuthAccessor accessor) {
        // generate oauth_token and oauth_secret
        final String consumer_key = (String) accessor.consumer
                .getProperty("name");

        // generate token and secret based on consumer_key
        // for now use md5 of name + current time as token
        final String token_data = consumer_key + System.nanoTime();
        final String token = DigestUtils.md5Hex(token_data);

        // first remove the accessor from cache
        this.tokens.remove(accessor);

        accessor.requestToken = null;
        accessor.accessToken = token;

        this.logger.fine("Adding access token " + accessor);

        // update token in local cache
        this.tokens.add(accessor);
    }

    @Override
    public void generateRequestToken(OAuthAccessor accessor) {
        // generate oauth_token and oauth_secret
        final String consumer_key = (String) accessor.consumer
                .getProperty("name");
        // generate token and secret based on consumer_key

        // for now use md5 of name + current time as token
        final String token_data = consumer_key + System.nanoTime();
        final String token = DigestUtils.md5Hex(token_data);
        // for now use md5 of name + current time + token as secret
        final String secret_data = consumer_key + System.nanoTime() + token;
        final String secret = DigestUtils.md5Hex(secret_data);

        accessor.requestToken = token;
        accessor.tokenSecret = secret;
        accessor.accessToken = null;

        this.logger.fine("Adding request token " + accessor);

        // add to the local cache
        this.tokens.add(accessor);
    }

    @Override
    public OAuthAccessor getAccessor(OAuthMessage requestMessage) {
        String consumer_token = null;
        try {
            consumer_token = requestMessage.getToken();
        } catch (final IOException e) {
            return null;
        }

        if (consumer_token == null) {
            return null;
        }

        OAuthAccessor accessor = null;

        // TODO: This is slow.
        for (final OAuthAccessor a : this.tokens) {
            if (a.requestToken != null) {
                if (a.requestToken.equals(consumer_token)) {
                    accessor = a;
                    break;
                }
            } else if (a.accessToken != null) {
                if (a.accessToken.equals(consumer_token)) {
                    accessor = a;
                    break;
                }
            }
        }

        return accessor;
    }

    @Override
    public OAuthConsumer getConsumer(OAuthMessage requestMessage) {
        try {
            final String consumer_key = requestMessage.getConsumerKey();
            return this.consumers.get(consumer_key);
        } catch (final IOException e) {
            return null;
        }
    }

    @Override
    public void markAsAuthorized(OAuthAccessor accessor, String userId) {
        // first remove the accessor from cache
        this.tokens.remove(accessor);

        accessor.setProperty("user", userId);
        accessor.setProperty("authorized", Boolean.TRUE);

        this.logger.fine("Authorizing request token " + accessor
                + " for userId " + userId);

        // update token in local cache
        this.tokens.add(accessor);
    }

}
