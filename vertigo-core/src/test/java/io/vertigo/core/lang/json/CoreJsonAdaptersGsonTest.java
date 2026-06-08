/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.core.lang.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

/**
 * Tests that Vertigo custom Gson adapters for java.time types work correctly
 * and produce identical output across Gson versions.
 *
 * Newer Gson versions introduced native type adapters for Instant, LocalDate, etc.
 * that serialize them as objects with fields (e.g. epochSecond, nano).
 * Vertigo uses string-based ISO8601 formats instead.
 *
 * This test class verifies:
 * - Custom adapters are NOT overridden by Gson built-in adapters
 * - Serialization output is STABLE between Gson versions
 * - Round-trip (serialize → deserialize) preserves values
 * - If newer Gson native adapters exist, custom adapters still take precedence
 *
 * @author npiedeloup
 */
final class CoreJsonAdaptersGsonTest {

	private static final Gson V_CORE_GSON = CoreJsonAdapters.V_CORE_GSON;

	/**
	 * Detect whether this runtime has newer Gson built-in java.time support.
	 * In newer Gson, plain Gson serializes Instant as an object.
	 * In older Gson, it throws an InaccessibleObjectException (module system blocks reflection on java.time).
	 */
	private static final boolean IS_GSON_WITH_BUILTIN_JAVA_TIME = detectGsonJavaTimeSupport();

	private static boolean detectGsonJavaTimeSupport() {
		try {
			final Gson plain = new Gson();
			final String json = plain.toJson(Instant.EPOCH);
			return json.startsWith("{"); // newer Gson produces objects
		} catch (final Exception e) {
			return false; // older Gson throws InaccessibleObjectException (no built-in adapter + module restriction)
		}
	}

	// ────────────────────────────────
	// Instant
	// ────────────────────────────────

	@Test
	void testInstantSerialization() {
		final Instant instant = Instant.parse("2025-06-05T12:00:00Z");
		final String json = V_CORE_GSON.toJson(instant);

		// Must be a string (custom adapter), NOT an object (Gson built-in)
		assertTrue(json.trim().startsWith("\""),
				"Instant must serialize as string. Custom adapter may be overridden. Got: " + json);
		// .SSS pattern forces .000 milliseconds
		assertEquals("\"2025-06-05T12:00:00.000Z\"", json.trim());
	}

	@Test
	void testInstantDeserialization() {
		final Instant deserialized = V_CORE_GSON.fromJson("\"2025-06-05T12:00:00Z\"", Instant.class);
		assertEquals(Instant.parse("2025-06-05T12:00:00Z"), deserialized,
				"Instant deserialization from ISO8601 string must work");
	}

	@Test
	void testInstantDeserializationWithMilliseconds() {
		final Instant deserialized = V_CORE_GSON.fromJson("\"2025-06-05T12:00:00.000Z\"", Instant.class);
		assertEquals(Instant.parse("2025-06-05T12:00:00Z"), deserialized,
				"Instant deserialization from ISO8601 with .000 milliseconds must work");
	}

	@Test
	void testInstantRoundTrip() {
		final Instant original = Instant.parse("2025-06-05T12:30:45.123Z");
		final String json = V_CORE_GSON.toJson(original);
		final Instant deserialized = V_CORE_GSON.fromJson(json, Instant.class);
		assertEquals(original, deserialized,
				"Instant round-trip must be preserved (millisecond precision only due to .SSS pattern)");
	}

	@Test
	void testInstantNanosecondsTruncatedToMilliseconds() {
		final Instant original = Instant.parse("2025-06-05T12:30:45.123456789Z");
		final String json = V_CORE_GSON.toJson(original);
		final Instant deserialized = V_CORE_GSON.fromJson(json, Instant.class);
		assertEquals(Instant.parse("2025-06-05T12:30:45.123Z"), deserialized,
				"Nanosecond precision should be truncated to milliseconds by .SSS pattern");
	}

	// ────────────────────────────────
	// LocalDate
	// ────────────────────────────────

	@Test
	void testLocalDateSerialization() {
		final LocalDate localDate = LocalDate.of(2025, 6, 5);
		final String json = V_CORE_GSON.toJson(localDate);

		assertTrue(json.trim().startsWith("\""),
				"LocalDate must serialize as string. Got: " + json);
		assertEquals("\"2025-06-05\"", json.trim());
	}

	@Test
	void testLocalDateDeserialization() {
		final LocalDate deserialized = V_CORE_GSON.fromJson("\"2025-06-05\"", LocalDate.class);
		assertEquals(LocalDate.of(2025, 6, 5), deserialized,
				"LocalDate deserialization from ISO string must work");
	}

	@Test
	void testLocalDateRoundTrip() {
		final LocalDate original = LocalDate.of(2025, 12, 25);
		final String json = V_CORE_GSON.toJson(original);
		final LocalDate deserialized = V_CORE_GSON.fromJson(json, LocalDate.class);
		assertEquals(original, deserialized, "LocalDate round-trip must be preserved");
	}

	// ────────────────────────────────
	// ZonedDateTime
	// ────────────────────────────────

	@Test
	void testZonedDateTimeSerialization() {
		final ZonedDateTime zdt = ZonedDateTime.of(2025, 6, 5, 14, 30, 0, 0, ZoneId.of("Europe/Paris"));
		final String json = V_CORE_GSON.toJson(zdt);

		assertTrue(json.trim().startsWith("\""),
				"ZonedDateTime must serialize as string. Got: " + json);
	}

	@Test
	void testZonedDateTimeRoundTrip() {
		final ZonedDateTime original = ZonedDateTime.of(2025, 6, 5, 14, 30, 0, 0, ZoneId.of("Europe/Paris"));
		final String json = V_CORE_GSON.toJson(original);
		final ZonedDateTime deserialized = V_CORE_GSON.fromJson(json, ZonedDateTime.class);
		assertEquals(original.toInstant().toEpochMilli(), deserialized.toInstant().toEpochMilli(),
				"ZonedDateTime round-trip must preserve the same instant in time");
	}

	// ────────────────────────────────
	// java.util.Date
	// ────────────────────────────────

	@Test
	void testDateSerialization() {
		final Date date = new Date(1717550400000L); // 2024-06-05T12:00:00Z
		final String json = V_CORE_GSON.toJson(date);

		assertTrue(json.trim().startsWith("\""),
				"Date must serialize as ISO8601 string. Got: " + json);
	}

	@Test
	void testDateRoundTrip() {
		final Date original = new Date(1717550400000L);
		final String json = V_CORE_GSON.toJson(original);
		final Date deserialized = V_CORE_GSON.fromJson(json, Date.class);
		assertEquals(original.getTime(), deserialized.getTime(), "Date round-trip must be preserved");
	}

	// ────────────────────────────────
	// Cross-version comparison: what happens WITHOUT custom adapters?
	// ────────────────────────────────

	@Test
	void testPlainGsonInstantBehavior() {
		final Gson plain = new GsonBuilder().setPrettyPrinting().create();
		final Instant instant = Instant.parse("2025-06-05T12:00:00Z");

		if (IS_GSON_WITH_BUILTIN_JAVA_TIME) {
			// Newer Gson: Instant becomes an object → custom adapter is ESSENTIAL
			final String json = plain.toJson(instant);
			assertTrue(json.trim().startsWith("{"),
					"Newer Gson built-in produces object (custom adapter still needed). Got: " + json);
		} else {
			// Older Gson: no built-in adapter, module system blocks reflection → exception
			// This proves custom adapters are ABSOLUTELY REQUIRED (not just "nice to have")
			assertThrows(JsonIOException.class, () -> plain.toJson(instant),
					"Older Gson cannot serialize Instant without custom adapter (proves adapters are essential)");
		}
	}

	@Test
	void testPlainGsonLocalDateBehavior() {
		final Gson plain = new GsonBuilder().setPrettyPrinting().create();
		final LocalDate localDate = LocalDate.of(2025, 6, 5);

		if (IS_GSON_WITH_BUILTIN_JAVA_TIME) {
			// Gson 2.14.0+ : LocalDate becomes an object → custom adapter is ESSENTIAL
			final String json = plain.toJson(localDate);
			assertTrue(json.trim().startsWith("{"),
					"Gson 2.14.0+ built-in produces object (custom adapter still needed). Got: " + json);
		} else {
			// Gson 2.13.1 : no built-in adapter, module system blocks reflection → exception
			assertThrows(JsonIOException.class, () -> plain.toJson(localDate),
					"Gson 2.13.1 cannot serialize LocalDate without custom adapter (proves adapters are essential)");
		}
	}

	@Test
	void testPlainGsonZonedDateTimeBehavior() {
		final Gson plain = new GsonBuilder().setPrettyPrinting().create();
		final ZonedDateTime zdt = ZonedDateTime.of(2025, 6, 5, 14, 30, 0, 0, ZoneId.of("Europe/Paris"));

		if (IS_GSON_WITH_BUILTIN_JAVA_TIME) {
			// Gson 2.14.0+ : produces some output but NOT our ISO_INSTANT format
			final String json = plain.toJson(zdt);
			assertNotEquals(V_CORE_GSON.toJson(zdt), json.trim(),
					"Plain Gson 2.14.0+ must NOT match custom adapter output (adapter still needed)");
		} else {
			// Gson 2.13.1 : no built-in adapter, module system blocks reflection → exception
			assertThrows(JsonIOException.class, () -> plain.toJson(zdt),
					"Gson 2.13.1 cannot serialize ZonedDateTime without custom adapter (proves adapters are essential)");
		}
	}

	/**
	 * Print Gson version for diagnostic output.
	 */
	@Test
	void printGsonVersion() {
		final Gson gson = new Gson();
		// Gson doesn't expose version publicly; infer from behavior
		System.out.println("[Diagnostic] Gson java.time support: " + IS_GSON_WITH_BUILTIN_JAVA_TIME
				+ " (true = 2.14.0+, false = 2.13.1 or earlier)");
	}

}
