/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.oauth.internal;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.restlet.engine.util.Base64;
import org.restlet.ext.oauth.GrantType;
import org.restlet.ext.oauth.ResponseType;
import org.restlet.ext.oauth.internal.Client.ClientType;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public abstract class AbstractClientManager implements ClientManager {

    public static final Object[] DEFAULT_SUPPORTED_FLOWS_CONFIDENTIAL = new Object[] {
            ResponseType.code,
            GrantType.authorization_code,
            GrantType.client_credentials,
            GrantType.refresh_token };

    public static final Object[] DEFAULT_SUPPORTED_FLOWS_PUBLIC = new Object[] { ResponseType.token };

    public static final int RESEED_CLIENTS = 100;

    private volatile int count = 0;

    private Map<ClientType, Object[]> defaultSupportedFlow;

    private boolean issueClientSecretToPublicClients = false;

    private SecureRandom random;

    public AbstractClientManager() {
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
        defaultSupportedFlow = new EnumMap<ClientType, Object[]>(ClientType.class);
        defaultSupportedFlow.put(ClientType.PUBLIC, DEFAULT_SUPPORTED_FLOWS_PUBLIC);
        defaultSupportedFlow.put(ClientType.CONFIDENTIAL, DEFAULT_SUPPORTED_FLOWS_CONFIDENTIAL);
    }

    public Client createClient(ClientType clientType, String[] redirectURIs, Map<String, Object> properties) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        }

        Object flows = properties.get(Client.PROPERTY_SUPPORTED_FLOWS);
        if (flows == null) {
            flows = defaultSupportedFlow.get(clientType);
            properties.put(Client.PROPERTY_SUPPORTED_FLOWS, flows);
        }

        // The authorization server MUST require the following clients to register their redirection endpoint:
        // o Public clients.
        // o Confidential clients utilizing the implicit grant type. (3.1.2.2. Registration Requirements)
        if (clientType == ClientType.PUBLIC
                || (clientType == ClientType.CONFIDENTIAL
                && Arrays.asList((Object[]) flows).contains(ResponseType.token))) {
            if (redirectURIs == null || redirectURIs.length == 0) {
                throw new IllegalArgumentException("RedirectionURI(s) required.");
            }
        }

        String clientId = UUID.randomUUID().toString();
        char[] clientSecret = null;
        if (clientType == ClientType.CONFIDENTIAL
                || (clientType == ClientType.PUBLIC && isIssueClientSecretToPublicClients())) {
            // Issue a client secret to the confidential client.
            if (count++ > RESEED_CLIENTS) {
                count = 0;
                random.setSeed(random.generateSeed(20));
            }
            byte[] secret = new byte[20];
            random.nextBytes(secret);
            clientSecret = Base64.encode(secret, false).toCharArray();
        }

        return createClient(clientId, clientSecret, clientType, redirectURIs, properties);
    }

    protected abstract Client createClient(String clientId,
            char[] clientSecret,
            ClientType clientType,
            String[] redirectURIs,
            Map<String, Object> properties);

    /**
     * @return the issueClientSecretToPublicClients
     */
    public boolean isIssueClientSecretToPublicClients() {
        return issueClientSecretToPublicClients;
    }

    public void setDefaultSupportedFlow(ClientType clientType, Object[] flows) {
        if (flows == null) {
            throw new IllegalArgumentException("Flows cannot be null.");
        }
        for (Object o : flows) {
            if (!(o instanceof GrantType) || !(o instanceof ResponseType)) {
                throw new IllegalArgumentException("Unsupported flow type.");
            }
        }
        defaultSupportedFlow.put(clientType, flows);
    }

    /**
     * @param issueClientSecretToPublicClients
     *            the issueClientSecretToPublicClients to set
     */
    public void setIssueClientSecretToPublicClients(boolean issueClientSecretToPublicClients) {
        this.issueClientSecretToPublicClients = issueClientSecretToPublicClients;
    }
}
