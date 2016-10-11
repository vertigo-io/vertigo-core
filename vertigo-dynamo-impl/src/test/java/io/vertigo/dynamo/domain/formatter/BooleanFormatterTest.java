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
package io.vertigo.dynamo.domain.formatter;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamox.domain.formatter.FormatterBoolean;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public final class BooleanFormatterTest {
	private final Formatter formatterBoolean = new FormatterBoolean("YES;NO");

	/**
	 * Test du formatter de booléen.
	 * @throws FormatterException
	*/
	@Test
	public void testFormatter() throws FormatterException {
		Assert.assertEquals(Boolean.TRUE, formatterBoolean.stringToValue("YES", DataType.Boolean));
		Assert.assertEquals(Boolean.TRUE, formatterBoolean.stringToValue("YES ", DataType.Boolean));
		Assert.assertEquals(Boolean.FALSE, formatterBoolean.stringToValue("NO", DataType.Boolean));
		Assert.assertEquals(Boolean.FALSE, formatterBoolean.stringToValue("NO ", DataType.Boolean));
		Assert.assertEquals(null, formatterBoolean.stringToValue(null, DataType.Boolean));
		Assert.assertEquals(null, formatterBoolean.stringToValue("", DataType.Boolean));
		Assert.assertEquals(null, formatterBoolean.stringToValue(" ", DataType.Boolean));

		Assert.assertEquals(Boolean.TRUE, formatterBoolean.stringToValue(" YES", DataType.Boolean));
		Assert.assertEquals(Boolean.TRUE, formatterBoolean.stringToValue("YES ", DataType.Boolean));

		Assert.assertEquals("YES", formatterBoolean.valueToString(Boolean.TRUE, DataType.Boolean));
		Assert.assertEquals("NO", formatterBoolean.valueToString(Boolean.FALSE, DataType.Boolean));
		Assert.assertEquals(null, formatterBoolean.valueToString(null, DataType.Boolean));
	}

	@Test(expected = FormatterException.class)
	public void testFormatter1() throws FormatterException {
		formatterBoolean.stringToValue("abc ", DataType.Boolean);
	}

	@Test(expected = Exception.class)
	public void testFormatter2() {
		formatterBoolean.valueToString("", DataType.Boolean);
	}

	@Test(expected = Exception.class)
	public void testFormatter3() {
		formatterBoolean.valueToString(" ", DataType.Boolean);
	}
}
