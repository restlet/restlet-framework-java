/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.restlet.engine.header.CacheDirectiveReader;
import org.restlet.engine.header.CacheDirectiveWriter;

/**
 * Tests that a {@link CacheDirective} built by a factory equals the same directive read from a
 * header (#1730), and that what is written does not depend on how it was built.
 */
class CacheDirectiveEqualityTestCase {

	private static CacheDirective parse(String header) throws IOException {
		return new CacheDirectiveReader(header).readValue();
	}

	@Test
	void builtNumericDirectives_equalTheParsedOnes() throws IOException {
		assertEquals(parse("max-age=60"), CacheDirective.maxAge(60));
		assertEquals(parse("max-stale=120"), CacheDirective.maxStale(120));
		assertEquals(parse("min-fresh=30"), CacheDirective.minFresh(30));
		assertEquals(parse("s-maxage=600"), CacheDirective.sharedMaxAge(600));
		assertEquals(parse("stale-while-revalidate=30"), CacheDirective.staleWhileRevalidate(30));
		assertEquals(parse("stale-if-error=600"), CacheDirective.staleIfError(600));
	}

	@Test
	void builtNumericDirectives_haveTheHashCodeOfTheParsedOnes() throws IOException {
		assertEquals(parse("max-age=60").hashCode(), CacheDirective.maxAge(60).hashCode());
		assertEquals(
				parse("stale-if-error=600").hashCode(),
				CacheDirective.staleIfError(600).hashCode());
	}

	@Test
	void builtDirectivesWithoutValue_equalTheParsedOnes() throws IOException {
		assertEquals(parse("no-store"), CacheDirective.noStore());
		assertEquals(parse("must-revalidate"), CacheDirective.mustRevalidate());
		assertEquals(parse("max-stale"), CacheDirective.maxStale());
	}

	@Test
	void aSetMixingBuiltAndParsedDirectives_holdsOneOfEach() throws IOException {
		Set<CacheDirective> set = new HashSet<>();
		set.add(CacheDirective.maxAge(60));
		set.add(parse("max-age=60"));
		set.add(CacheDirective.noStore());

		assertEquals(2, set.size());
		assertTrue(set.contains(parse("max-age=60")));
		assertTrue(List.of(CacheDirective.maxAge(60)).contains(parse("max-age=60")));
	}

	@Test
	void directivesWithAnotherNameOrValue_areNotEqual() throws IOException {
		assertNotEquals(parse("max-age=60"), CacheDirective.maxAge(61));
		assertNotEquals(parse("max-age=60"), CacheDirective.sharedMaxAge(60));
		assertNotEquals(parse("max-age=60"), CacheDirective.noStore());
		assertNotEquals(CacheDirective.noStore(), null);
		assertFalse(CacheDirective.noStore().equals("no-store"));
	}

	@Test
	void writer_writesTheSameHeaderWhateverTheWayTheDirectiveWasBuilt() throws IOException {
		assertEquals(
				"max-age=60, s-maxage=600, stale-while-revalidate=30, stale-if-error=5",
				CacheDirectiveWriter.write(
						List.of(
								CacheDirective.maxAge(60),
								CacheDirective.sharedMaxAge(600),
								CacheDirective.staleWhileRevalidate(30),
								CacheDirective.staleIfError(5))));
		assertEquals(
				"max-age=60, s-maxage=600, stale-while-revalidate=30, stale-if-error=5",
				CacheDirectiveWriter.write(
						List.of(
								parse("max-age=60"),
								parse("s-maxage=600"),
								parse("stale-while-revalidate=30"),
								parse("stale-if-error=5"))));
		assertEquals(
				"no-cache=\"a, b\", private=\"c\", max-stale=1, min-fresh=2, no-store",
				CacheDirectiveWriter.write(
						List.of(
								CacheDirective.noCache(List.of("a", "b")),
								CacheDirective.privateInfo("c"),
								CacheDirective.maxStale(1),
								CacheDirective.minFresh(2),
								CacheDirective.noStore())));
	}
}
