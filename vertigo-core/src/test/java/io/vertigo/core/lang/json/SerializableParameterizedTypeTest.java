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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Tests for {@link SerializableParameterizedType}.
 *
 * Verifies:
 * - Java serialization round-trip (required for BerkeleyDB KV store)
 * - Gson fromJson/toJson compatibility (must behave identically to Gson's native Type)
 * - equals/hashCode/toString contract
 *
 * @author npiedeloup
 */
final class SerializableParameterizedTypeTest {

	// ────────────────────────────────
	// Construction
	// ────────────────────────────────

	@Test
	void testOfConstructor() {
		final ParameterizedType type = SerializableParameterizedType.of(ArrayList.class, String.class);

		assertEquals(ArrayList.class, type.getRawType());
		assertArrayEqualsString(type.getActualTypeArguments(), String.class);
		assertEquals(null, type.getOwnerType());
	}

	@Test
	void testOfConstructorMultipleArgs() {
		final ParameterizedType type = SerializableParameterizedType.of(List.class, ArrayList.class, String.class);

		assertEquals(List.class, type.getRawType());
		assertEquals(2, type.getActualTypeArguments().length);
		assertEquals(ArrayList.class, type.getActualTypeArguments()[0]);
		assertEquals(String.class, type.getActualTypeArguments()[1]);
	}

	@Test
	void testOfConstructorNullRawTypeThrows() {
		assertThrows(IllegalArgumentException.class, () -> SerializableParameterizedType.of(null, String.class));
	}

	@Test
	void testFromConstructor() {
		// Gson TypeToken produces a non-serializable ParameterizedType
		final Type gsonType = TypeToken.getParameterized(ArrayList.class, String.class).getType();
		assertTrue(gsonType instanceof ParameterizedType);
		assertFalse(gsonType instanceof Serializable, "Gson's native Type should NOT be Serializable");

		// Wrap it
		final ParameterizedType wrapped = SerializableParameterizedType.from((ParameterizedType) gsonType);

		assertEquals(ArrayList.class, wrapped.getRawType());
		assertArrayEqualsString(wrapped.getActualTypeArguments(), String.class);
		assertTrue(wrapped instanceof Serializable, "Wrapped type must be Serializable");
	}

	// ────────────────────────────────
	// Java Serializable round-trip
	// ────────────────────────────────

	@Test
	void testJavaSerializationRoundTrip() {
		final SerializableParameterizedType original = SerializableParameterizedType.of(ArrayList.class, String.class);

		// Serialize → bytes → deserialize
		final byte[] bytes = serializeToBytes(original);
		final ParameterizedType deserialized = (ParameterizedType) deserializeFromBytes(bytes);

		assertEquals(original.getRawType(), deserialized.getRawType());
		assertArrayEqualsString(deserialized.getActualTypeArguments(), String.class);
	}

	@Test
	void testJavaSerializationRoundTripFromGsonType() {
		// Simulate the ViewContextMap use case: Gson TypeToken → wrap → serialize → deserialize
		final Type gsonType = TypeToken.getParameterized(ArrayList.class, Integer.class).getType();
		final SerializableParameterizedType wrapped = SerializableParameterizedType.from((ParameterizedType) gsonType);

		final byte[] bytes = serializeToBytes(wrapped);
		final ParameterizedType deserialized = (ParameterizedType) deserializeFromBytes(bytes);

		assertEquals(ArrayList.class, deserialized.getRawType());
		assertArrayEqualsString(deserialized.getActualTypeArguments(), Integer.class);
	}

	// ────────────────────────────────
	// Gson fromJson / toJson compatibility
	// ────────────────────────────────

	@Test
	void testGsonFromJsonWithStringList() {
		final Gson gson = new GsonBuilder().create();

		final Type type = SerializableParameterizedType.of(ArrayList.class, String.class);
		final String json = "[\"a\", \"b\", \"c\"]";

		@SuppressWarnings("unchecked")
		final List<String> list = gson.fromJson(json, type);
		assertEquals(Arrays.asList("a", "b", "c"), list);
	}

	@Test
	void testGsonFromJsonWithIntegerList() {
		final Gson gson = new GsonBuilder().create();

		final Type type = SerializableParameterizedType.of(ArrayList.class, Integer.class);
		final String json = "[1, 2, 3]";

		@SuppressWarnings("unchecked")
		final List<Integer> list = gson.fromJson(json, type);
		assertEquals(Arrays.asList(1, 2, 3), list);
	}

	@Test
	void testGsonFromJsonProducesSameResultAsGsonTypeToken() {
		final Gson gson = new GsonBuilder().create();

		final Type typeToken = TypeToken.getParameterized(ArrayList.class, String.class).getType();
		final Type wrapped = SerializableParameterizedType.of(ArrayList.class, String.class);
		final String json = "[\"hello\", \"world\"]";

		@SuppressWarnings("unchecked")
		final List<String> resultToken = gson.fromJson(json, typeToken);
		@SuppressWarnings("unchecked")
		final List<String> resultWrapped = gson.fromJson(json, wrapped);

		assertEquals(resultToken, resultWrapped,
				"SerializableParameterizedType must produce identical results to Gson TypeToken");
	}

	@Test
	void testGsonToJsonWithType() {
		final Gson gson = new GsonBuilder().create();

		final Type type = SerializableParameterizedType.of(ArrayList.class, String.class);
		final List<String> list = Arrays.asList("x", "y");

		final String json = gson.toJson(list, type);
		// Gson default (no pretty printing) produces compact JSON without spaces
		assertEquals("[\"x\",\"y\"]", json);
	}

	// ────────────────────────────────
	// Equality, hashCode, toString
	// ────────────────────────────────

	@Test
	void testEqualsIdentical() {
		final var a = SerializableParameterizedType.of(ArrayList.class, String.class);
		final var b = SerializableParameterizedType.of(ArrayList.class, String.class);

		assertEquals(a, b);
		assertEquals(a.hashCode(), b.hashCode());
	}

	@Test
	void testEqualsDifferentArgs() {
		final var a = SerializableParameterizedType.of(ArrayList.class, String.class);
		final var b = SerializableParameterizedType.of(ArrayList.class, Integer.class);

		assertFalse(a.equals(b));
	}

	@Test
	void testEqualsDifferentRawType() {
		final var a = SerializableParameterizedType.of(ArrayList.class, String.class);
		final var b = SerializableParameterizedType.of(List.class, String.class);

		assertFalse(a.equals(b));
	}

	@Test
	void testEqualsNull() {
		final var type = SerializableParameterizedType.of(ArrayList.class, String.class);
		assertFalse(type.equals(null));
		assertFalse(type.equals("not a type"));
	}

	@Test
	void testToString() {
		final var type = SerializableParameterizedType.of(ArrayList.class, String.class);
		assertEquals("java.util.ArrayList<java.lang.String>", type.toString());
	}

	@Test
	void testToStringMultipleArgs() {
		final var type = SerializableParameterizedType.of(List.class, ArrayList.class, String.class);
		assertEquals("java.util.List<java.util.ArrayList, java.lang.String>", type.toString());
	}

	@Test
	void testReturnTypeIsDefensiveCopy() {
		final var type = SerializableParameterizedType.of(ArrayList.class, String.class);
		final Type[] args = type.getActualTypeArguments();

		assertSame(String.class, args[0]);
		// Mutating returned array should not affect internal state
		args[0] = Integer.class;
		assertSame(String.class, type.getActualTypeArguments()[0],
				"getActualTypeArguments must return a copy");
	}

	// ────────────────────────────────
	// Helpers
	// ────────────────────────────────

	private byte[] serializeToBytes(final Object obj) {
		try (final var baos = new ByteArrayOutputStream();
				final var oos = new ObjectOutputStream(baos)) {
			oos.writeObject(obj);
			return baos.toByteArray();
		} catch (final Exception e) {
			throw new AssertionError("Serialization failed", e);
		}
	}

	private Object deserializeFromBytes(final byte[] bytes) {
		try (final var bais = new ByteArrayInputStream(bytes);
				final var ois = new ObjectInputStream(bais)) {
			return ois.readObject();
		} catch (final Exception e) {
			throw new AssertionError("Deserialization failed", e);
		}
	}

	private void assertArrayEqualsString(final Type[] types, final Class<?>... expectedClasses) {
		assertEquals(expectedClasses.length, types.length);
		for (int i = 0; i < expectedClasses.length; i++) {
			assertEquals(expectedClasses[i], types[i]);
		}
	}
}
