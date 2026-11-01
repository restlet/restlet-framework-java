/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.data;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test {@link org.restlet.data.ChallengeScheme}.
 */
public class ChallengeSchemeTestCase {

	@Test
	public void valueOf_isCaseInsensitive() {
		Assertions.assertSame(ChallengeScheme.HTTP_BASIC, ChallengeScheme.valueOf("http_basic"));
	}

	/** Issue #1618: the Bearer and MAC schemes used to be mapped to HTTP_OAUTH. */
	@Test
	public void valueOf_oauthBearerAndMac_returnTheirOwnConstants() {
		Assertions.assertSame(ChallengeScheme.HTTP_OAUTH_BEARER, ChallengeScheme.valueOf("HTTP_Bearer"));
		Assertions.assertSame(ChallengeScheme.HTTP_OAUTH_BEARER, ChallengeScheme.valueOf("http_bearer"));
		Assertions.assertSame(ChallengeScheme.HTTP_OAUTH_MAC, ChallengeScheme.valueOf("HTTP_MAC"));
		Assertions.assertSame(ChallengeScheme.HTTP_OAUTH, ChallengeScheme.valueOf("HTTP_OAuth"));
	}

	/**
	 * Every constant must be found again by its name, so that a missing or
	 * mistaken entry in the lookup table is detected. HTTP_AWS_IAM and
	 * HTTP_AWS_QUERY are deliberately resolved to HTTP_AWS_S3.
	 */
	@Test
	public void valueOf_everyConstant_isFoundByItsName() throws IllegalAccessException {
		int checked = 0;
		for (Field field : ChallengeScheme.class.getFields()) {
			if (field.getType() != ChallengeScheme.class || !Modifier.isStatic(field.getModifiers())) {
				continue;
			}
			ChallengeScheme constant = (ChallengeScheme) field.get(null);
			if (constant == ChallengeScheme.HTTP_AWS_IAM || constant == ChallengeScheme.HTTP_AWS_QUERY) {
				continue;
			}
			Assertions.assertSame(constant, ChallengeScheme.valueOf(constant.getName()), field.getName());
			checked++;
		}
		Assertions.assertEquals(12, checked);
	}
}
