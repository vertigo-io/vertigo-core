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
package io.vertigo.commons.parser;

import org.junit.Assert;
import org.junit.Test;

public class CalculatorTest {
	private static final Rule<Integer> MAIN = new CalculatorRule();

	private static int eval(final String s) throws NotFoundException {
		return MAIN.parse(s, 0).getResult().intValue();
	}

	@Test
	public void test() throws NotFoundException {
		Assert.assertEquals(6, eval("2*3"));
		//--
		Assert.assertEquals(5, eval("2 + 3"));
		//--
		Assert.assertEquals(11, eval("121 /11"));
	}

	@Test(expected = NotFoundException.class)
	public void testFail() throws NotFoundException {
		eval("2 $ 3");
		//l'opérateur  $ n'existe pas
	}
}
