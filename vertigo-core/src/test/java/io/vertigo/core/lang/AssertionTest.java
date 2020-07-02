/**
O * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.lang;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test de l'utilitaire des assertions.
 *
 * @author pchretien
 */
public final class AssertionTest {
	@Test
	public void testCheckNotNull() {
		Assertion.check()
				.isNotNull("notNull1")
				.isNotNull("notNull2");
	}

	@Test
	public void testCheckNotNull2() {
		Assertion.check()
				.isNotNull("notNull1", "msg1")
				.isNotNull("notNull2", "msg2");
	}

	@Test
	public void testCheckNotNull3() {
		Assertion.check()
				.isNotNull("notNull1", "message1: {0} ", "param")
				.isNotNull("notNull2", "message2: {0} ", "param");
	}

	@Test
	public void testCheckNotNullFail() {
		Assertions.assertThrows(NullPointerException.class,
				() -> Assertion.check().isNotNull(null));
	}

	@Test
	public void testCheckNotNull3FailWithMessage() {
		Assertions.assertThrows(NullPointerException.class,
				() -> Assertion.check().isNotNull(null, "message: {0} ", "param"));
	}

	@Test
	public void testCheckArgument() {
		Assertion.check().isTrue(true, "message");
	}

	@Test
	public void testCheckArgument2() {
		Assertion.check().isTrue(true, "message {0}", "param");
	}

	@Test
	public void testCheckArgumentFail() {
		Assertions.assertThrows(IllegalStateException.class,
				() -> Assertion.check().isTrue(false, "message"));
	}

	@Test
	public void testCheckArgument2Fail() {
		Assertions.assertThrows(IllegalStateException.class,
				() -> Assertion.check().isTrue(false, "message {0}", "param"));
	}

	//-----
	@Test
	public void testCheckState() {
		Assertion.check().isTrue(true, "message");
	}

	@Test
	public void testCheckState2() {
		Assertion.check().isTrue(true, "message {0}", "param1");
	}

	@Test
	public void testCheckStateFail() {
		Assertions.assertThrows(IllegalStateException.class,
				() -> Assertion.check().isTrue(false, "message"));
	}

	@Test
	public void testCheckState2Fail() {
		Assertions.assertThrows(IllegalStateException.class,
				() -> Assertion.check().isTrue(false, "message {0}", "param1"));
	}

	@Test
	public void testCheckNotEmpty() {
		Assertion.check().isNotBlank("test", "message");
	}

	@Test
	public void testCheckNotEmptyFail() {
		Assertions.assertThrows(IllegalArgumentException.class,
				() -> Assertion.check().isNotBlank("  ", "message"));
	}

	@Test
	public void testCheckNotEmpty2Fail() {
		Assertions.assertThrows(NullPointerException.class,
				() -> Assertion.check().isNotBlank(null, "message {0}", "param"));
	}

	@Test
	public void testWhen() {
		final Optional<String> option1 = Optional.empty();
		final Optional<String> option2 = Optional.of("test");
		Assertion.when(option1.isPresent())
				.isTrue(() -> option1.get() != null, "fail")
				.isTrue(() -> option1.get() == null, "fail");
		Assertion.when(option2.isPresent())
				.isTrue(() -> option2.get() != null, "not null")
				.isTrue(() -> option2.get().startsWith("test"), "invalid prefix")
				.isFalse(() -> option2.get().length() > 4, "invalid size");
	}

	@Test
	public void testWhenFail() {
		Assertions.assertThrows(IllegalStateException.class,
				() -> Assertion.when(true)
						.isTrue(() -> 1 != 2, "success")
						.isTrue(() -> 1 == 2, "fail"));
	}

}
