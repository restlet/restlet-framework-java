/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.security;

/**
 * Verifier that can locally retrieve the secrets. This verifier assumes that
 * the secret associated to an identifier can be retrieved, which isn't always
 * possible or even desirable.
 * 
 * @author Jerome Louvel
 */
public abstract class LocalVerifier extends SecretVerifier {

	/**
	 * Returns the local secret associated to a given identifier.
	 * 
	 * @param identifier The identifier to lookup.
	 * @return The secret associated to the identifier or null.
	 */
	public abstract char[] getLocalSecret(String identifier);

	@Override
	public int verify(String identifier, char[] secret) {
		return compare(secret, getLocalSecret(identifier)) ? RESULT_VALID : RESULT_INVALID;
	}

}
