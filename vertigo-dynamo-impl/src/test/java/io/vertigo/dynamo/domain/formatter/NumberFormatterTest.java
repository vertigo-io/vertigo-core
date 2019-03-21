/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Formatter;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamox.domain.formatter.FormatterNumber;
import io.vertigo.dynamox.domain.formatter.FormatterNumberLocalized;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public class NumberFormatterTest extends AbstractTestCaseJU4 {

	/**
	 * Test du formatter de nombre.
	 * @throws FormatterException e
	 */
	@Test
	public void testFormatterNumber() throws FormatterException {
		final Formatter formatterNumber = new FormatterNumber("#,###,##0.00");

		//BigDecimal
		final BigDecimal pi = new BigDecimal("3.14");
		Assert.assertEquals(pi, formatterNumber.stringToValue("3.14", DataType.BigDecimal));
		Assert.assertEquals(pi, formatterNumber.stringToValue("3,14", DataType.BigDecimal));
		Assert.assertEquals("3,14", formatterNumber.valueToString(pi, DataType.BigDecimal));
		Assert.assertEquals(new BigDecimal("0.14"), formatterNumber.stringToValue("0.14", DataType.BigDecimal));
		//Integer
		Assert.assertEquals(1492, formatterNumber.stringToValue("1492", DataType.Integer));
		Assert.assertEquals(1492, formatterNumber.stringToValue("1 492", DataType.Integer));
		Assert.assertEquals(1492, formatterNumber.stringToValue("1492  ", DataType.Integer));
		Assert.assertEquals(1492, formatterNumber.stringToValue("01492  ", DataType.Integer));
		//Long
		Assert.assertEquals(1492L, formatterNumber.stringToValue("1492", DataType.Long));
		Assert.assertEquals(1492L, formatterNumber.stringToValue("1 492", DataType.Long));
		Assert.assertEquals(1492L, formatterNumber.stringToValue("1492  ", DataType.Long));
		Assert.assertEquals(1492L, formatterNumber.stringToValue("01492  ", DataType.Long));
		//Double
		Assert.assertEquals(3.14D, formatterNumber.stringToValue("3.14", DataType.Double));
		Assert.assertEquals(3.14D, formatterNumber.stringToValue("3,14", DataType.Double));
		Assert.assertEquals(.14D, formatterNumber.stringToValue("0.14", DataType.Double));
		Assert.assertEquals("3,14", formatterNumber.valueToString(3.14D, DataType.Double));
	}

	/**
	 * Test du formatter de nombre.
	 * @throws FormatterException e
	 */
	@Test
	public void testFormatterNumberMLWithDecimal() throws FormatterException {
		//séparateur décimal , et accepte .
		//séparateur milliers '\u00A0' => espace insécable + espace (implicite)
		final FormatterNumber formatterNumberLocalized = new FormatterNumberLocalized("#,##0.00|,.|\u00A0 ");

		//BigDecimal
		final BigDecimal pi = new BigDecimal("3.14");
		Assert.assertEquals(pi, formatterNumberLocalized.stringToValue("3.14", DataType.BigDecimal));
		Assert.assertEquals(pi, formatterNumberLocalized.stringToValue("3,14", DataType.BigDecimal));
		Assert.assertEquals(new BigDecimal("0.14"), formatterNumberLocalized.stringToValue("0.14", DataType.BigDecimal));

		Assert.assertEquals("3,14", formatterNumberLocalized.valueToString(pi, DataType.BigDecimal));
		Assert.assertEquals("1" + (char) 160 + "495,00", formatterNumberLocalized.valueToString(1495, DataType.BigDecimal));
		Assert.assertEquals("1\u00A0495,00", formatterNumberLocalized.valueToString(1495, DataType.BigDecimal));
		Assert.assertEquals("1\u00A0495,52", formatterNumberLocalized.valueToString(1495.52, DataType.BigDecimal));

		//Integer
		Assert.assertEquals(1492, formatterNumberLocalized.stringToValue("1492", DataType.Integer));
		Assert.assertEquals(1492, formatterNumberLocalized.stringToValue("1 492", DataType.Integer));
		Assert.assertEquals(1492, formatterNumberLocalized.stringToValue("1492  ", DataType.Integer));
		Assert.assertEquals(1492, formatterNumberLocalized.stringToValue("01492  ", DataType.Integer));
		Assert.assertEquals("1\u00A0492,00", formatterNumberLocalized.valueToString(1492, DataType.Integer));

		//Long
		Assert.assertEquals(1492L, formatterNumberLocalized.stringToValue("1492", DataType.Long));
		Assert.assertEquals(1492L, formatterNumberLocalized.stringToValue("1 492", DataType.Long));
		Assert.assertEquals(1492L, formatterNumberLocalized.stringToValue("1492  ", DataType.Long));
		Assert.assertEquals(1492L, formatterNumberLocalized.stringToValue("01492  ", DataType.Long));
		Assert.assertEquals("1\u00A0492,00", formatterNumberLocalized.valueToString(1492L, DataType.Long));
	}

	/**
	 * Test du formatter de nombre.
	 * @throws FormatterException e
	 */
	@Test
	public void testFormatterNumberMLNoDecimal() throws FormatterException {
		//séparateur décimal . et accepte ,
		//séparateur milliers NO précisé => par défaut sep \u00A0
		final FormatterNumber formatterNumberLocalized = new FormatterNumberLocalized("#,##0.##|.,|");

		//séparateur milliers ' '
		final FormatterNumber formatterNumberLocalizedSpace = new FormatterNumberLocalized("#,##0.##|.,| \u00A0");

		//BigDecimal
		final BigDecimal pi = new BigDecimal("3.14");
		Assert.assertEquals(pi, formatterNumberLocalized.stringToValue("3.14", DataType.BigDecimal));
		Assert.assertEquals(pi, formatterNumberLocalized.stringToValue("3,14", DataType.BigDecimal));
		Assert.assertEquals(new BigDecimal("0.14"), formatterNumberLocalized.stringToValue("0.14", DataType.BigDecimal));

		Assert.assertEquals("3.14", formatterNumberLocalized.valueToString(pi, DataType.BigDecimal));
		Assert.assertEquals("1\u00A0495", formatterNumberLocalized.valueToString(1495, DataType.BigDecimal));
		Assert.assertEquals("1 495.52", formatterNumberLocalizedSpace.valueToString(1495.52, DataType.BigDecimal));

		//Integer
		Assert.assertEquals(1492, formatterNumberLocalized.stringToValue("1492", DataType.Integer));
		Assert.assertEquals(1492, formatterNumberLocalized.stringToValue("1\u00A0492", DataType.Integer));
		Assert.assertEquals(1492, formatterNumberLocalized.stringToValue("1492  ", DataType.Integer));
		Assert.assertEquals(1492, formatterNumberLocalized.stringToValue("01492  ", DataType.Integer));
		Assert.assertEquals("1\u00A0492", formatterNumberLocalized.valueToString(1492, DataType.Integer));

		//Long
		Assert.assertEquals(1492L, formatterNumberLocalized.stringToValue("1492", DataType.Long));
		Assert.assertEquals(1492L, formatterNumberLocalizedSpace.stringToValue("1\u00A0492", DataType.Long));
		Assert.assertEquals(1492L, formatterNumberLocalized.stringToValue("1492  ", DataType.Long));
		Assert.assertEquals(1492L, formatterNumberLocalized.stringToValue("01492  ", DataType.Long));
		Assert.assertEquals("1 492", formatterNumberLocalizedSpace.valueToString(1492L, DataType.Long));

	}

	/**
	 * Test du formatter de nombre.
	 * @throws FormatterException e
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testFormatterNumberMLConflit() throws FormatterException {
		final FormatterNumber formatterNumberLocalized = new FormatterNumberLocalized("#,##0.##|.,|.");
		formatterNumberLocalized.stringToValue("3.14", DataType.BigDecimal);
		//Détection du conflit entre séparateur décimal et de millier
	}
}
