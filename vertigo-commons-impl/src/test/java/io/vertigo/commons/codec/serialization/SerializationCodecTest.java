package io.vertigo.commons.codec.serialization;

import io.vertigo.commons.codec.AbstractCodecTest;
import io.vertigo.commons.codec.Codec;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.kernel.exception.VRuntimeException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

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
		for (final Serializable value : createObjectList()) {
			final byte[] serializedValue = codec.encode(value);
			Assert.assertEquals(value, codec.decode(serializedValue));
		}
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testDecode() throws Exception {
		for (final Serializable value : createObjectList()) {
			final byte[] serializedValue = codec.encode(value);
			Assert.assertEquals(value, codec.decode(serializedValue));
		}
	}

	/** {@inheritDoc} */
	@Override
	@Test(expected = VRuntimeException.class)
	public void testFailDecode() throws Exception {
		// object ne correspondant pas à une classe;
		final byte[] s = "qdfsdf".getBytes();
		codec.decode(s);
	}

	// ===========================================================================
	// =========================== Données de tests
	// ==============================
	// ===========================================================================
	private List<Serializable> createObjectList() {
		final List<Serializable> valueList = new ArrayList<>();
		valueList.add(Integer.valueOf(54)); // Test d'un entier
		valueList.add(""); // Test d'une chaine vide
		valueList.add("   "); // Test d'une chaine remplie avec des espaces
		valueList.add("abcdefghijklmnopqrstuvwxyz ABCDEFGHIJKLMNOPQRSTUVWXYZ  1234567890"); // Test
																							// d'une
																							// chaine
																							// de
																							// caractères
																							// simples
		valueList.add("éèêë üû ïî àäâ öô"); // Test des accents
		valueList.add(">< & # @ %"); // Test des caractères HTML XML
		valueList.add(" % ' "); // Test des caractères SQL
		valueList.add("abcdef aéàè ' () {}  \" ' 12345 \\ / /254sd %§!*-+/"); // Test
																				// d'une
																				// chaine
																				// de
																				// caractères
																				// avec
																				// caractères
																				// spéciaux
																				// mélangés

		valueList.add(createPerson()); // Test d'un objet
		return valueList;
	}

	private TestPerson createPerson() {
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
