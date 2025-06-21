/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Verifier that stores its local secrets in a map indexed by the identifier.
 * Note that this verifier isn't very secure by itself.
 * 
 * @author Jerome Louvel
 */
public class MapVerifier extends LocalVerifier {

	/** The map of local secrets. */
	private final ConcurrentMap<String, char[]> localSecrets;

	/**
	 * Constructor.
	 */
	public MapVerifier() {
		this(new ConcurrentHashMap<String, char[]>());
	}

	/**
	 * Constructor.
	 * 
	 * @param localSecrets The map of local secrets.
	 */
	public MapVerifier(ConcurrentMap<String, char[]> localSecrets) {
		this.localSecrets = localSecrets;
	}

	@Override
	public char[] getLocalSecret(String identifier) {
		return (identifier == null) ? null : getLocalSecrets().get(identifier);
	}

	/**
	 * Returns the map of local secrets.
	 * 
	 * @return The map of local secrets.
	 */
	public ConcurrentMap<String, char[]> getLocalSecrets() {
		return localSecrets;
	}

	/**
	 * Sets the modifiable map of local secrets. This method clears the current map
	 * and puts all entries in the parameter map.
	 * 
	 * @param localSecrets A map of local secrets.
	 */
	public void setLocalSecrets(Map<String, char[]> localSecrets) {
		synchronized (getLocalSecrets()) {
			if (localSecrets != getLocalSecrets()) {
				getLocalSecrets().clear();

				if (localSecrets != null) {
					getLocalSecrets().putAll(localSecrets);
				}
			}
		}
	}

}
