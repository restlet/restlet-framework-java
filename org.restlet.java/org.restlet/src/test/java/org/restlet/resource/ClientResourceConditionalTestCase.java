/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Conditions;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.engine.Engine;
import org.restlet.representation.StringRepresentation;

/** Tests the per-call conditional writes of {@link ClientResource} (#100). */
class ClientResourceConditionalTestCase {

	private static final Date LAST_MODIFIED = new Date(1_700_000_000_000L);

	/** A one-resource server that evaluates the preconditions and records what it received. */
	private static final class Origin extends Restlet {
		volatile boolean exists = true;
		volatile String version = "v1";
		volatile boolean strongTag = true;
		final List<Method> methods = new ArrayList<>();
		final List<Conditions> received = new ArrayList<>();

		@Override
		public void handle(Request request, Response response) {
			methods.add(request.getMethod());
			Conditions c = request.getConditions();
			Conditions copy = new Conditions();
			copy.getMatch().addAll(c.getMatch());
			copy.getNoneMatch().addAll(c.getNoneMatch());
			copy.setUnmodifiedSince(c.getUnmodifiedSince());
			received.add(copy);

			Tag tag = new Tag(version, !strongTag);

			if (request.getMethod() == Method.GET) {
				StringRepresentation entity = new StringRepresentation("state " + version);
				entity.setTag(tag);
				entity.setModificationDate(LAST_MODIFIED);
				response.setEntity(entity);
				response.setDate(new Date(LAST_MODIFIED.getTime() + 60_000));
				return;
			}

			Status status = c.getStatus(request.getMethod(), exists, tag, LAST_MODIFIED);

			if (status == null) {
				status = exists ? Status.SUCCESS_NO_CONTENT : Status.SUCCESS_CREATED;

				if (request.getMethod() == Method.DELETE) {
					exists = false;
				} else {
					exists = true;
					version += "'";
				}
			}

			response.setStatus(status);
		}
	}

	private Origin origin;
	private ClientResource cr;

	@BeforeEach
	void setUpEach() {
		Engine.clearThreadLocalVariables();
		Engine.register(false);
		Engine.getInstance().registerDefaultConverters();
		origin = new Origin();
		cr = new ClientResource("http://localhost/r");
		cr.setNext(origin);
	}

	@AfterEach
	void tearDownEach() {
		Engine.clearThreadLocalVariables();
	}

	private Conditions readAndBuildConditions() {
		cr.get();
		return Conditions.unchangedSince(cr.getResponse());
	}

	private static StringRepresentation body() {
		return new StringRepresentation("new state");
	}

	@Test
	void put_withCurrentStrongTag_succeedsAndSendsIfMatch() {
		Conditions conditions = readAndBuildConditions();

		cr.put(body(), conditions);

		assertEquals(Status.SUCCESS_NO_CONTENT, cr.getStatus());
		assertEquals("v1", origin.received.get(1).getMatch().get(0).getName());
		assertEquals("v1'", origin.version);
	}

	@Test
	void put_withStaleTag_failsWith412AndLeavesTheResourceUnchanged() {
		Conditions conditions = readAndBuildConditions();
		origin.version = "v2";

		ResourceException e =
				assertThrows(ResourceException.class, () -> cr.put(body(), conditions));

		assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, e.getStatus());
		assertEquals("v2", origin.version);
	}

	@Test
	void put_objectEntityWithStaleTag_failsWith412() {
		Conditions conditions = readAndBuildConditions();
		origin.version = "v2";

		ResourceException e =
				assertThrows(ResourceException.class, () -> cr.put((Object) "text", conditions));

		assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, e.getStatus());
	}

	@Test
	void put_withWeakTagAndOldEnoughDate_usesIfUnmodifiedSince() {
		origin.strongTag = false;
		Conditions conditions = readAndBuildConditions();

		cr.put(body(), conditions);

		assertTrue(origin.received.get(1).getMatch().isEmpty());
		assertEquals(LAST_MODIFIED, origin.received.get(1).getUnmodifiedSince());
		assertEquals(Status.SUCCESS_NO_CONTENT, cr.getStatus());
	}

	@Test
	void put_ifAbsentOnMissingResource_creates() {
		origin.exists = false;

		cr.put(body(), Conditions.ifAbsent());

		assertEquals(Status.SUCCESS_CREATED, cr.getStatus());
		assertTrue(origin.exists);
	}

	@Test
	void put_ifAbsentOnExistingResource_failsWith412AndLeavesItUnchanged() {
		ResourceException e =
				assertThrows(ResourceException.class, () -> cr.put(body(), Conditions.ifAbsent()));

		assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, e.getStatus());
		assertEquals("v1", origin.version);
	}

	@Test
	void patch_withCurrentTag_succeeds() {
		Conditions conditions = readAndBuildConditions();

		cr.patch(body(), conditions);

		assertEquals(Method.PATCH, origin.methods.get(1));
		assertEquals("v1", origin.received.get(1).getMatch().get(0).getName());
		assertEquals(Status.SUCCESS_NO_CONTENT, cr.getStatus());
	}

	@Test
	void patch_objectEntityWithStaleTag_failsWith412() {
		Conditions conditions = readAndBuildConditions();
		origin.version = "v2";

		ResourceException e =
				assertThrows(ResourceException.class, () -> cr.patch((Object) "text", conditions));

		assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, e.getStatus());
	}

	@Test
	void delete_withCurrentTag_deletes() {
		Conditions conditions = readAndBuildConditions();

		cr.delete(conditions);

		assertFalse(origin.exists);
		assertEquals("v1", origin.received.get(1).getMatch().get(0).getName());
	}

	@Test
	void delete_withStaleTag_failsWith412AndKeepsTheResource() {
		Conditions conditions = readAndBuildConditions();
		origin.version = "v2";

		ResourceException e = assertThrows(ResourceException.class, () -> cr.delete(conditions));

		assertEquals(Status.CLIENT_ERROR_PRECONDITION_FAILED, e.getStatus());
		assertTrue(origin.exists);
	}

	@Test
	void conditionalWrite_isNotStickyOnTheResource() {
		Conditions conditions = readAndBuildConditions();

		cr.put(body(), conditions);
		cr.get();
		cr.put(body());

		// received: GET, conditional PUT, GET, plain PUT
		assertTrue(origin.received.get(2).getMatch().isEmpty());
		assertTrue(origin.received.get(3).getMatch().isEmpty());
		assertNull(origin.received.get(3).getUnmodifiedSince());
		assertFalse(cr.getRequest().getConditions().hasSome());
	}

	@Test
	void conditionalWrite_doesNotMutateConditionsSetOnThePrototypeRequest() {
		Conditions conditions = readAndBuildConditions();
		cr.getRequest().getConditions().getNoneMatch().add(new Tag("other", false));

		cr.put(body(), conditions);

		assertEquals(1, cr.getRequest().getConditions().getNoneMatch().size());
		assertTrue(cr.getRequest().getConditions().getMatch().isEmpty());
		// the per-call conditions replace the prototype ones for this call only
		assertTrue(origin.received.get(1).getNoneMatch().isEmpty());
	}

	@Test
	void conditionalWrite_isNotRetriedOnRecoverableError() {
		cr.setRetryOnError(true);
		cr.setRetryAttempts(3);
		cr.setRetryDelay(0);
		List<Method> calls = new ArrayList<>();
		cr.setNext(
				new Restlet() {
					@Override
					public void handle(Request request, Response response) {
						calls.add(request.getMethod());
						response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
					}
				});

		assertThrows(ResourceException.class, () -> cr.put(body(), Conditions.ifAbsent()));

		assertEquals(1, calls.size());
	}

	@Test
	void unconditionalPut_isStillRetriedOnRecoverableError() {
		cr.setRetryOnError(true);
		cr.setRetryAttempts(2);
		cr.setRetryDelay(0);
		List<Method> calls = new ArrayList<>();
		cr.setNext(
				new Restlet() {
					@Override
					public void handle(Request request, Response response) {
						calls.add(request.getMethod());
						response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
					}
				});

		assertThrows(ResourceException.class, () -> cr.put(body()));

		assertTrue(calls.size() > 1);
	}
}
