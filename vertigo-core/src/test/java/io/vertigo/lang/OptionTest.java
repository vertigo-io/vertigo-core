/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import java.util.NoSuchElementException;

import org.junit.Assert;
import org.junit.Test;

/**
*
* @author pchretien
*/
public final class OptionTest {
	@Test
	public void testNone() {
		final Option<String> none = Option.none();
		Assert.assertTrue(none.isEmpty());
		Assert.assertFalse(none.isDefined());
		Assert.assertEquals("movie", none.getOrElse("movie"));
	}

	@Test(expected = NoSuchElementException.class)
	public void testNoneFail() {
		Option.none().get();
	}

	@Test(expected = NullPointerException.class)
	public void testSomeFail() {
		Option.some(null);
	}

	@Test
	public void testSome() {
		final Option<String> some = Option.some("music");
		Assert.assertFalse(some.isEmpty());
		Assert.assertTrue(some.isDefined());
		Assert.assertEquals("music", some.get());
		Assert.assertEquals("music", some.getOrElse("movie"));
	}
}
