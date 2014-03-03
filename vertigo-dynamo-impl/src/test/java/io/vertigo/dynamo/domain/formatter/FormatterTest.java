package io.vertigo.dynamo.domain.formatter;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.domain.metamodel.FormatterException;
import io.vertigo.dynamo.domain.metamodel.KDataType;
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
 * @version $Id: FormatterTest.java,v 1.3 2013/10/22 10:46:48 pchretien Exp $
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
		formatterBoolean = new FormatterBoolean("FMT_BOOLEAN");
		formatterBoolean.initParameters("OUI;NON");
		/*
						formatterDate = new FormatterDate();
				  formatterDate.initParameters("OUI;NON");
		*/
		formatterNumber = new FormatterNumber("FMT_NUMMBER");
		formatterNumber.initParameters("#,###,##0.00");

		formatterString = new FormatterString("FMT_STRING");
		formatterString.initParameters("UPPER");
	}

	/**
	 * Test du formatter de nombre.
	 * @throws FormatterException e
	 */
	@Test
	public void testFormatterNumber() throws FormatterException {
		//BigDecimal
		final BigDecimal pi = new BigDecimal("3.14");
		Assert.assertEquals(pi, formatterNumber.stringToValue("3.14", KDataType.BigDecimal));
		Assert.assertEquals(pi, formatterNumber.stringToValue("3,14", KDataType.BigDecimal));
		Assert.assertEquals("3,14", formatterNumber.valueToString(pi, KDataType.BigDecimal));
		Assert.assertEquals(new BigDecimal("0.14"), formatterNumber.stringToValue("0.14", KDataType.BigDecimal));
		//Integer
		Assert.assertEquals(1492, formatterNumber.stringToValue("1492", KDataType.Integer));
		Assert.assertEquals(1492, formatterNumber.stringToValue("1 492", KDataType.Integer));
		Assert.assertEquals(1492, formatterNumber.stringToValue("1492  ", KDataType.Integer));
		Assert.assertEquals(1492, formatterNumber.stringToValue("01492  ", KDataType.Integer));
		//Long
		Assert.assertEquals(1492L, formatterNumber.stringToValue("1492", KDataType.Long));
		Assert.assertEquals(1492L, formatterNumber.stringToValue("1 492", KDataType.Long));
		Assert.assertEquals(1492L, formatterNumber.stringToValue("1492  ", KDataType.Long));
		Assert.assertEquals(1492L, formatterNumber.stringToValue("01492  ", KDataType.Long));
	}

	/**
	 * Test du formatter de nombre.
	 * @throws FormatterException e
	 */
	@Test
	public void testFormatterNumberMLWithDecimal() throws FormatterException {
		final FormatterNumber formatterNumberLocalized = new FormatterNumberLocalized("FMT_TEST");
		//séparateur décimal , et accepte .
		//séparateur milliers '\u00A0' => espace insécable + espace (implicite)
		formatterNumberLocalized.initParameters("#,##0.00|,.|\u00A0 ");

		//BigDecimal
		final BigDecimal pi = new BigDecimal("3.14");
		Assert.assertEquals(pi, formatterNumberLocalized.stringToValue("3.14", KDataType.BigDecimal));
		Assert.assertEquals(pi, formatterNumberLocalized.stringToValue("3,14", KDataType.BigDecimal));
		Assert.assertEquals(new BigDecimal("0.14"), formatterNumberLocalized.stringToValue("0.14", KDataType.BigDecimal));

		Assert.assertEquals("3,14", formatterNumberLocalized.valueToString(pi, KDataType.BigDecimal));
		Assert.assertEquals("1" + (char) 160 + "495,00", formatterNumberLocalized.valueToString(1495, KDataType.BigDecimal));
		Assert.assertEquals("1\u00A0495,00", formatterNumberLocalized.valueToString(1495, KDataType.BigDecimal));
		Assert.assertEquals("1\u00A0495,52", formatterNumberLocalized.valueToString(1495.52, KDataType.BigDecimal));

		//Integer
		Assert.assertEquals(1492, formatterNumber.stringToValue("1492", KDataType.Integer));
		Assert.assertEquals(1492, formatterNumberLocalized.stringToValue("1 492", KDataType.Integer));
		Assert.assertEquals(1492, formatterNumberLocalized.stringToValue("1492  ", KDataType.Integer));
		Assert.assertEquals(1492, formatterNumberLocalized.stringToValue("01492  ", KDataType.Integer));
		Assert.assertEquals("1\u00A0492,00", formatterNumberLocalized.valueToString(1492, KDataType.Integer));

		//Long
		Assert.assertEquals(1492L, formatterNumberLocalized.stringToValue("1492", KDataType.Long));
		Assert.assertEquals(1492L, formatterNumberLocalized.stringToValue("1 492", KDataType.Long));
		Assert.assertEquals(1492L, formatterNumberLocalized.stringToValue("1492  ", KDataType.Long));
		Assert.assertEquals(1492L, formatterNumberLocalized.stringToValue("01492  ", KDataType.Long));
		Assert.assertEquals("1\u00A0492,00", formatterNumberLocalized.valueToString(1492L, KDataType.Long));
	}

	/**
	 * Test du formatter de nombre.
	 * @throws FormatterException e
	 */
	@Test
	public void testFormatterNumberMLNoDecimal() throws FormatterException {
		final FormatterNumber formatterNumberLocalized = new FormatterNumberLocalized("FMT_TEST");
		//séparateur décimal . et accepte ,
		//séparateur milliers non précisé => par défaut sep \u00A0
		formatterNumberLocalized.initParameters("#,##0.##|.,|");

		final FormatterNumber formatterNumberLocalizedSpace = new FormatterNumberLocalized("FMT_TEST");
		//séparateur milliers ' '
		formatterNumberLocalizedSpace.initParameters("#,##0.##|.,| \u00A0");

		//BigDecimal
		final BigDecimal pi = new BigDecimal("3.14");
		Assert.assertEquals(pi, formatterNumberLocalized.stringToValue("3.14", KDataType.BigDecimal));
		Assert.assertEquals(pi, formatterNumberLocalized.stringToValue("3,14", KDataType.BigDecimal));
		Assert.assertEquals(new BigDecimal("0.14"), formatterNumberLocalized.stringToValue("0.14", KDataType.BigDecimal));

		Assert.assertEquals("3.14", formatterNumberLocalized.valueToString(pi, KDataType.BigDecimal));
		Assert.assertEquals("1\u00A0495", formatterNumberLocalized.valueToString(1495, KDataType.BigDecimal));
		Assert.assertEquals("1 495.52", formatterNumberLocalizedSpace.valueToString(1495.52, KDataType.BigDecimal));

		//Integer
		Assert.assertEquals(1492, formatterNumber.stringToValue("1492", KDataType.Integer));
		Assert.assertEquals(1492, formatterNumberLocalized.stringToValue("1\u00A0492", KDataType.Integer));
		Assert.assertEquals(1492, formatterNumberLocalized.stringToValue("1492  ", KDataType.Integer));
		Assert.assertEquals(1492, formatterNumberLocalized.stringToValue("01492  ", KDataType.Integer));
		Assert.assertEquals("1\u00A0492", formatterNumberLocalized.valueToString(1492, KDataType.Integer));

		//Long
		Assert.assertEquals(1492L, formatterNumberLocalized.stringToValue("1492", KDataType.Long));
		Assert.assertEquals(1492L, formatterNumberLocalizedSpace.stringToValue("1\u00A0492", KDataType.Long));
		Assert.assertEquals(1492L, formatterNumberLocalized.stringToValue("1492  ", KDataType.Long));
		Assert.assertEquals(1492L, formatterNumberLocalized.stringToValue("01492  ", KDataType.Long));
		Assert.assertEquals("1 492", formatterNumberLocalizedSpace.valueToString(1492L, KDataType.Long));

	}

	/**
	 * Test du formatter de nombre.
	 * @throws FormatterException e
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testFormatterNumberMLConflit() throws FormatterException {
		final FormatterNumber formatterNumberLocalized = new FormatterNumberLocalized("FMT_TEST");
		formatterNumberLocalized.initParameters("#,##0.##|.,|.");
		formatterNumberLocalized.stringToValue("3.14", KDataType.BigDecimal);
		//Détection du conflit entre séparateur décimal et de millier
	}

	/**
	 * Test du formatter de booléen.
	* @throws FormatterException e
	*/
	@Test
	public void testFormatterBoolean() {
		Assert.assertEquals(Boolean.TRUE, formatterBoolean.stringToValue("OUI", KDataType.Boolean));
		Assert.assertEquals(Boolean.TRUE, formatterBoolean.stringToValue("OUI ", KDataType.Boolean));
		Assert.assertEquals(Boolean.FALSE, formatterBoolean.stringToValue("NON", KDataType.Boolean));
		Assert.assertEquals(Boolean.FALSE, formatterBoolean.stringToValue("NON ", KDataType.Boolean));
		Assert.assertEquals(null, formatterBoolean.stringToValue(null, KDataType.Boolean));
		Assert.assertEquals(null, formatterBoolean.stringToValue("", KDataType.Boolean));
		Assert.assertEquals(null, formatterBoolean.stringToValue(" ", KDataType.Boolean));

		Assert.assertEquals(Boolean.TRUE, formatterBoolean.stringToValue(" OUI", KDataType.Boolean));
		Assert.assertEquals(Boolean.TRUE, formatterBoolean.stringToValue("OUI ", KDataType.Boolean));

		Assert.assertEquals("OUI", formatterBoolean.valueToString(Boolean.TRUE, KDataType.Boolean));
		Assert.assertEquals("NON", formatterBoolean.valueToString(Boolean.FALSE, KDataType.Boolean));
		Assert.assertEquals(null, formatterBoolean.valueToString(null, KDataType.Boolean));
	}

	@Test(expected = FormatterException.class)
	public void testFormatterBoolean1() {
		formatterBoolean.stringToValue("abc ", KDataType.Boolean);
	}

	@Test(expected = Exception.class)
	public void testFormatterBoolean2() {
		formatterBoolean.valueToString("", KDataType.Boolean);
	}

	@Test(expected = Exception.class)
	public void testFormatterBoolean3() {
		formatterBoolean.valueToString(" ", KDataType.Boolean);
	}
}
