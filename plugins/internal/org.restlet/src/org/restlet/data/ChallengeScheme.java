/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.data;

/**
 * Challenge scheme used to authenticate remote clients.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ChallengeScheme extends Metadata
{
	/** Custom scheme based on IP address or cookies or query params, etc. */
	public static final ChallengeScheme CUSTOM = new ChallengeScheme("CUSTOM", "Custom",
			"Custom authentication");

	/** Basic HTTP scheme. */
	public static final ChallengeScheme HTTP_BASIC = new ChallengeScheme("HTTP_BASIC",
			"Basic", "Basic HTTP authentication");

	/** Digest HTTP scheme. */
	public static final ChallengeScheme HTTP_DIGEST = new ChallengeScheme("HTTP_DIGEST",
			"Digest", "Digest HTTP authentication");

	/** Microsoft NTML HTTP scheme. */
	public static final ChallengeScheme HTTP_NTLM = new ChallengeScheme("HTTP_NTLM",
			"NTLM", "Microsoft's NTLM HTTP authentication");

	/** Plain SMTP scheme. */
	public static final ChallengeScheme SMTP_PLAIN = new ChallengeScheme("SMTP_PLAIN",
			"PLAIN", "Plain SMTP authentication");

	/** The technical name. */
	private String technicalName;

	/**
	 * Constructor.
	 * @param name The unique name.
	 * @param technicalName The technical name.
	 */
	public ChallengeScheme(String name, String technicalName)
	{
		this(name, technicalName, null);
	}

	/**
	 * Constructor.
	 * @param name The unique name.
	 * @param technicalName The technical name.
	 * @param description The description.
	 */
	public ChallengeScheme(String name, String technicalName, String description)
	{
		super(name, description);
		this.technicalName = technicalName;
	}

	/**
	 * Returns the technical name (ex: BASIC).
	 * @return The technical name (ex: BASIC).
	 */
	public String getTechnicalName()
	{
		return this.technicalName;
	}

	/**
	 * Indicates if the scheme is equal to a given one.
	 * @param scheme The scheme to compare to.
	 * @return True if the scheme is equal to a given one.
	 */
	public boolean equals(ChallengeScheme scheme)
	{
		return scheme.getName().equalsIgnoreCase(getName());
	}
}
