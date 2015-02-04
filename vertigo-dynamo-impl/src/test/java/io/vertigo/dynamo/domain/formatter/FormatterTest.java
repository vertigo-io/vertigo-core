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
package io.vertigo.dynamo.domain.formatter;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamox.domain.formatter.FormatterBoolean;
import io.vertigo.dynamox.domain.formatter.FormatterNumber;
import io.vertigo.dynamox.domain.formatter.FormatterNumberLocalized;
import io.vertigo.dynamox.domain.formatter.FormatterString;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public class FormatterTest extends AbstractTestCaseJU4 {
	private FormatterBoolean formatterBoolean;
	/*	private FormatterDate formatterDate;*/
	private FormatterNumber formatterNumber;
	private FormatterString formatterString;

	//	/** {@inheritDoc} */
	//	@Override
	//	protected String getPropertiesFileName() {
	//		return "/dynamo/default-test.properties";
	//	}

	/**{@inheritDoc}*/
	@Override
	public void doSetUp() {
		formatterBoolean = new FormatterBoolean("OUI;NON");
		/*
		formatterDate = new FormatterDate();
		formatterDate.initParameters("OUI;NON");
		*/
		formatterNumber = new FormatterNumber("#,###,##0.00");

		formatterString = new FormatterString("UPPER");
	}

	/**
	 * Test du formatter de nombre.
	 * @throws FormatterException e
	 */
	@Test
	public void testFormatterNumber() throws FormatterException {
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
	}

	@Test
	public void testUpper() throws FormatterException {
		Assert.assertEquals("AA", formatterString.valueToString("aa", DataType.String));
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
		Assert.assertEquals(1492, formatterNumber.stringToValue("1492", DataType.Integer));
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
		//séparateur milliers non précisé => par défaut sep \u00A0
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
		Assert.assertEquals(1492, formatterNumber.stringToValue("1492", DataType.Integer));
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

	/**
	 * Test du formatter de booléen.
	* @throws FormatterException e
	*/
	@Test
	public void testFormatterBoolean() {
		Assert.assertEquals(Boolean.TRUE, formatterBoolean.stringToValue("OUI", DataType.Boolean));
		Assert.assertEquals(Boolean.TRUE, formatterBoolean.stringToValue("OUI ", DataType.Boolean));
		Assert.assertEquals(Boolean.FALSE, formatterBoolean.stringToValue("NON", DataType.Boolean));
		Assert.assertEquals(Boolean.FALSE, formatterBoolean.stringToValue("NON ", DataType.Boolean));
		Assert.assertEquals(null, formatterBoolean.stringToValue(null, DataType.Boolean));
		Assert.assertEquals(null, formatterBoolean.stringToValue("", DataType.Boolean));
		Assert.assertEquals(null, formatterBoolean.stringToValue(" ", DataType.Boolean));

		Assert.assertEquals(Boolean.TRUE, formatterBoolean.stringToValue(" OUI", DataType.Boolean));
		Assert.assertEquals(Boolean.TRUE, formatterBoolean.stringToValue("OUI ", DataType.Boolean));

		Assert.assertEquals("OUI", formatterBoolean.valueToString(Boolean.TRUE, DataType.Boolean));
		Assert.assertEquals("NON", formatterBoolean.valueToString(Boolean.FALSE, DataType.Boolean));
		Assert.assertEquals(null, formatterBoolean.valueToString(null, DataType.Boolean));
	}

	@Test(expected = FormatterException.class)
	public void testFormatterBoolean1() {
		formatterBoolean.stringToValue("abc ", DataType.Boolean);
	}

	@Test(expected = Exception.class)
	public void testFormatterBoolean2() {
		formatterBoolean.valueToString("", DataType.Boolean);
	}

	@Test(expected = Exception.class)
	public void testFormatterBoolean3() {
		formatterBoolean.valueToString(" ", DataType.Boolean);
	}
}
