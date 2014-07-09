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
package io.vertigo.commons.parser;

import io.vertigo.commons.parser.NotFoundException;
import io.vertigo.commons.parser.Parser;
import io.vertigo.commons.parser.Rule;

import org.junit.Assert;
import org.junit.Test;

public class CalculatorTest {
	private static final Rule<Integer> MAIN = new CalculatorRule();

	@Test
	public void test() throws NotFoundException {
		final Parser<Integer> parser = MAIN.createParser();
		//--
		parser.parse("2*3", 0);
		Assert.assertEquals(6, parser.get().intValue());
		//--
		parser.parse("2 + 3", 0);
		Assert.assertEquals(5, parser.get().intValue());
		//--
		parser.parse("121 /11", 0);
		Assert.assertEquals(11, parser.get().intValue());
	}

	@Test(expected = NotFoundException.class)
	public void testFail() throws NotFoundException {
		final Parser<Integer> parser = MAIN.createParser();
		//--
		parser.parse("2 $ 3", 0); //l'opérateur  $ n'existe pas 
		Assert.fail();
	}
}
