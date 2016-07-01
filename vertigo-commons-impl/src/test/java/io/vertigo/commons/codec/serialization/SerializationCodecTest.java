/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.commons.codec.serialization;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.commons.codec.AbstractCodecTest;
import io.vertigo.commons.codec.Codec;
import io.vertigo.commons.codec.CodecManager;

/**
 * Test du codec de sérialisation.
 *
 * @author pchretien
 */
public final class SerializationCodecTest extends AbstractCodecTest<Serializable, byte[]> {

	/** {@inheritDoc} */
	@Override
	public Codec<Serializable, byte[]> obtainCodec(final CodecManager codecManager) {
		return codecManager.getSerializationCodec();
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testNull() {
		Assert.assertNull(codec.encode(null));
		Assert.assertNull(codec.decode(null));
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testEncode() {
		for (final Serializable value : createObjects()) {
			final byte[] serializedValue = codec.encode(value);
			Assert.assertEquals(value, codec.decode(serializedValue));
		}
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testDecode() throws Exception {
		for (final Serializable value : createObjects()) {
			final byte[] serializedValue = codec.encode(value);
			Assert.assertEquals(value, codec.decode(serializedValue));
		}
	}

	/** {@inheritDoc} */
	@Override
	@Test(expected = RuntimeException.class)
	public void testFailDecode() throws Exception {
		// object ne correspondant pas à une classe;
		final byte[] s = "qdfsdf".getBytes();
		codec.decode(s);
	}

	// ===========================================================================
	// =========================== Données de tests
	// ==============================
	// ===========================================================================
	private static List<Serializable> createObjects() {
		final List<Serializable> values = new ArrayList<>();
		values.add(Integer.valueOf(54)); // Test d'un entier
		values.add(""); // Test d'une chaine vide
		values.add("   "); // Test d'une chaine remplie avec des espaces
		values.add("abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ  1234567890"); // Test
		// d'une
		// chaine
		// de
		// caractères
		// simples
		values.add("éèêë üû ïî àäâ öô"); // Test des accents
		values.add(">< & # @ %"); // Test des caractères HTML XML
		values.add(" % ' "); // Test des caractères SQL
		values.add("abcdef aéàè ' () {}  \" ' 12345 \\ / /254sd %§!*-+/"); // Test
		// d'une
		// chaine
		// de
		// caractères
		// avec
		// caractères
		// spéciaux
		// mélangés

		values.add(createPerson()); // Test d'un objet

		return values;
	}

	private static TestPerson createPerson() {
		final TestPerson mother = new TestPerson("jeanne", "lagrange", 58, null, null);
		final TestPerson father = new TestPerson("charles", "dupond", 86, null, null);
		return new TestPerson("edmond", "dupond", 72, mother, father);
	}

	private static class TestPerson implements Serializable {
		private static final long serialVersionUID = 1L;

		private final String firstName;
		private final String lastName;
		private final Integer weight;

		private final TestPerson mother;
		private final TestPerson father;

		TestPerson(final String firstName, final String lastName, final Integer weight, final TestPerson mother, final TestPerson father) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.weight = weight;
			this.mother = mother;
			this.father = father;
		}

		/** {@inheritDoc} */
		@Override
		public int hashCode() {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc} */
		@Override
		public boolean equals(final Object o) {
			if (o instanceof TestPerson) {
				final TestPerson person = (TestPerson) o;
				return lastName.equals(person.lastName) && firstName.equals(person.firstName) && weight.equals(person.weight) && mother == null && person.mother == null || mother.equals(person.mother) && father == null && person.father == null || father.equals(person.father);
			}
			return false;
		}
	}

}
