/**
 * Copyright 2005-2013 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */
package org.restlet.example.ext.oauth.server;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import org.restlet.ext.oauth.Client;
import org.restlet.ext.oauth.ClientStore;
import org.restlet.ext.oauth.internal.memory.MemTokenGenerator;

/**
 * MongoDB version of ClientStore.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class MongoClientStore extends ClientStore<MemTokenGenerator> {

	private DBCollection clients;

	public MongoClientStore() {
		// FIXME: Use own TokenGenerator
		super(new MemTokenGenerator(new ScheduledThreadPoolExecutor(5)));
		clients = OAuth2Sample.getDefaultDB().getCollection("clients");
	}

	@Override
	public Client createClient(String clientId, String redirectUri) {
		return createClient(clientId, null, redirectUri);
	}

	@Override
	public Client createClient(String clientId, String clientSecret,
			String redirectUri) {
		DBObject client = new BasicDBObject().append("_id", clientId)
				.append(MongoClient.CLIENT_SECRET, clientSecret)
				.append(MongoClient.REDIRECT_URI, redirectUri);
		clients.insert(client);
		return new MongoClient(client);
	}

	@Override
	public void deleteClient(String id) {
		clients.findAndRemove(new BasicDBObject("_id", id));
	}

	@Override
	public Client findById(String id) {
		DBObject client = clients.findOne(new BasicDBObject("_id", id));
		if (client == null) {
			return null;
		}
		return new MongoClient(client);
	}

	@Override
	public Collection<? extends Client> findClientsForUser(String userId) {
		ArrayList<MongoClient> list = new ArrayList<MongoClient>();
		DBCollection authUsers = OAuth2Sample.getDefaultDB().getCollection(
				"authenticated_users");

		DBCursor cursor = authUsers.find(new BasicDBObject("_id.user_id",
				userId));
		while (cursor.hasNext()) {
			DBObject authUser = cursor.next();
			list.add(new MongoClient(clients.findOne(new BasicDBObject("_id",
					authUser.get("client_id")))));
		}
		return list;
	}

}
