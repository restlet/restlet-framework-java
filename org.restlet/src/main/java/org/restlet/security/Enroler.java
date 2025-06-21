/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.security;

import org.restlet.data.ClientInfo;

import java.security.Principal;

/**
 * Updates an authenticated client user with assigned roles. Typically, it is
 * invoked by an {@link Authenticator} after successful authentication to add
 * {@link Role} instances based on available {@link User}.
 * 
 * @see Authenticator#getEnroler()
 * @see Authenticator#setEnroler(Enroler)
 * @see ClientInfo#getUser()
 * @see ClientInfo#getRoles()
 * @author Jerome Louvel
 */
public interface Enroler {

	/**
	 * Attempts to update an authenticated client, with a {@link User} properly
	 * defined, by adding the {@link Role} that are assigned to this user. Note that
	 * principals could also be added to the {@link ClientInfo} if necessary. The
	 * addition could also potentially be based on the presence of
	 * {@link Principal}.
	 * 
	 * @param clientInfo The clientInfo to update.
	 */
	void enrole(ClientInfo clientInfo);

}
