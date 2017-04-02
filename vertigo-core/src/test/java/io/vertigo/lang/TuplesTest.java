/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.lang;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tuples.
 *
 * @author pchretien
 */
@RunWith(JUnitPlatform.class)
public final class TuplesTest {
	@Test
	public void testTuple2() {
		final Tuples.Tuple2<String, String> homer = Tuples.of("homer", "simpson");
		final Tuples.Tuple2<String, String> homer2 = Tuples.of("homer", "simpson");
		final Tuples.Tuple2<String, String> marge = Tuples.of("marge", "simpson");

		assertEquals(homer, homer);
		assertTrue(homer.equals(homer));
		assertEquals(homer.hashCode(), homer2.hashCode());
		assertEquals(homer, homer2);
		assertNotEquals(homer, marge);
		assertNotNull(marge);

		assertEquals("homer", homer.getVal1());
		assertEquals("simpson", homer.getVal2());

		assertEquals("marge", marge.getVal1());
		assertEquals("simpson", marge.getVal2());
	}

	@Test
	public void testTuple3() {
		final Tuples.Tuple3<String, String, String> homer = Tuples.of("homer", "simpson", "M");
		final Tuples.Tuple3<String, String, String> homer2 = Tuples.of("homer", "simpson", "M");
		final Tuples.Tuple3<String, String, String> marge = Tuples.of("marge", "simpson", "F");

		assertEquals(homer, homer);
		assertTrue(homer.equals(homer));
		assertEquals(homer.hashCode(), homer2.hashCode());
		assertEquals(homer, homer2);
		assertNotEquals(homer, marge);
		assertNotNull(marge);

		assertEquals(homer, homer2);
		assertEquals(homer, homer);
		assertNotEquals(homer, marge);

		assertEquals("homer", homer.getVal1());
		assertEquals("simpson", homer.getVal2());
		assertEquals("M", homer.getVal3());

		assertEquals("marge", marge.getVal1());
		assertEquals("simpson", marge.getVal2());
		assertEquals("F", marge.getVal3());
	}
}
