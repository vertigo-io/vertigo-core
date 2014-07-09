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
package io.vertigo.dynamo.domain.constraint;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamox.domain.constraint.ConstraintBigDecimalLength;
import io.vertigo.dynamox.domain.constraint.ConstraintDoubleLength;
import io.vertigo.dynamox.domain.constraint.ConstraintIntegerLength;
import io.vertigo.dynamox.domain.constraint.ConstraintLongLength;
import io.vertigo.dynamox.domain.constraint.ConstraintRegex;
import io.vertigo.dynamox.domain.constraint.ConstraintStringLength;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test des contraintes.
 *
 * @author pchretien
 */
public class ConstraintTest extends AbstractTestCaseJU4 {
	//private ConstraintNotNull constraintNotNull;

	private ConstraintBigDecimalLength constraintBigDecimalLength;
	private ConstraintDoubleLength constraintDoubleLength;
	private ConstraintIntegerLength constraintIntegerLength;
	private ConstraintLongLength constraintLongLength;

	private ConstraintStringLength constraintStringLength;

	private ConstraintRegex constraintRegex;

	/** {@inheritDoc} */
	@Override
	public void doSetUp() {
		//		constraintNotNull = new ConstraintNotNull();
		//		constraintNotNull.initParameters(null);

		constraintBigDecimalLength = new ConstraintBigDecimalLength("CK_BIG");
		constraintBigDecimalLength.initParameters("3"); //10^3

		constraintDoubleLength = new ConstraintDoubleLength("CK_DOUBLE");
		constraintDoubleLength.initParameters("3"); //10^3

		constraintIntegerLength = new ConstraintIntegerLength("CK_INTEGER");
		constraintIntegerLength.initParameters("3"); //10^3

		constraintLongLength = new ConstraintLongLength("CK_LONG");
		constraintLongLength.initParameters("3"); //10^3

		constraintStringLength = new ConstraintStringLength("CK_STRING");
		constraintStringLength.initParameters("3"); //10^3

		constraintRegex = new ConstraintRegex("CK_REGEX");
		// \w signifie WORD [A-Za-z0-9_] et on ajoute le tiret -
		constraintRegex.initParameters("[\\w-]*");
	}

	private void testBDTrue(final BigDecimal value) {
		Assert.assertTrue(constraintBigDecimalLength.checkConstraint(value));
	}

	private void testBDFalse(final BigDecimal value) {
		Assert.assertFalse(constraintBigDecimalLength.checkConstraint(value));
	}

	private void testDoubleTrue(final Double value) {
		Assert.assertTrue(constraintDoubleLength.checkConstraint(value));
	}

	private void testDoubleFalse(final Double value) {
		Assert.assertFalse(constraintDoubleLength.checkConstraint(value));
	}

	private void testIntegerTrue(final Integer value) {
		Assert.assertTrue(constraintIntegerLength.checkConstraint(value));
	}

	private void testIntegerFalse(final Integer value) {
		Assert.assertFalse(constraintIntegerLength.checkConstraint(value));
	}

	private void testLongTrue(final Long value) {
		Assert.assertTrue(constraintLongLength.checkConstraint(value));
	}

	private void testLongFalse(final Long value) {
		Assert.assertFalse(constraintLongLength.checkConstraint(value));
	}

	private void testStringTrue(final String value) {
		Assert.assertTrue(constraintStringLength.checkConstraint(value));
	}

	private void testStringFalse(final String value) {
		Assert.assertFalse(constraintStringLength.checkConstraint(value));
	}

	/**
	 * Test de ConstraintNotNull.
	 */
	@Test
	public void testConstraintNotNull() {
		//		assertFalse(constraintNotNull.checkConstraint(null));
		//		assertFalse(constraintNotNull.checkConstraint(null, KDataType.Date));
		//		assertTrue(constraintNotNull.checkConstraint(true, KDataType.Date));
		//		assertTrue(constraintNotNull.checkConstraint(123));
		//		assertTrue(constraintNotNull.checkConstraint(new Date(), KDataType.Date));
	}

	/**
	 * Test de constraintBigDecimalLength.
	 */
	@Test
	public void testConstraintBigDecimalLength() {
		BigDecimal bd;

		testBDTrue(null);

		bd = new BigDecimal(123);
		testBDTrue(bd);
		testBDTrue(bd.negate());

		bd = new BigDecimal(1234);
		testBDFalse(bd);
		testBDFalse(bd.negate());

		bd = new BigDecimal(1000);
		testBDFalse(bd);
		testBDFalse(bd.negate());

		bd = new BigDecimal("999.9999");
		testBDTrue(bd);
		testBDTrue(bd.negate());
	}

	/**
	 * Test de ConstraintDoubleLength.
	 */
	@Test
	public void testConstraintDoubleLength() {
		testDoubleTrue(null);

		testDoubleFalse(Double.NaN);

		testDoubleTrue(123d);
		testDoubleTrue(-123d);

		testDoubleFalse(1234d);
		testDoubleFalse(-1234d);

		testDoubleFalse(1000d);
		testDoubleFalse(-1000d);

		final Double d = 999.9999d;
		testDoubleTrue(d);
		testDoubleTrue(-d);
	}

	/**
	 * Test de ConstraintIntegerLength.
	 */
	@Test
	public void testConstraintIntegerLength() {
		testIntegerTrue(null);

		testIntegerTrue(123);
		testIntegerTrue(-123);

		testIntegerFalse(1234);
		testIntegerFalse(-1234);

		testIntegerFalse(1000);
		testIntegerFalse(-1000);
	}

	/**
	 * Test de ConstraintLongLength.
	 */
	@Test
	public void testConstraintLongLength() {
		testLongTrue(null);

		testLongTrue(123L);
		testLongTrue(-123L);

		testLongFalse(1234L);
		testLongFalse(-1234L);

		testLongFalse(1000L);
		testLongFalse(-1000L);
	}

	/**
	 * Test de ConstraintStringLength.
	 */
	@Test
	public void testConstraintStringLength() {
		testStringTrue(null);

		testStringTrue("abc");
		testStringTrue("123");
		testStringTrue("   ");

		testStringFalse("abcd");
		testStringFalse("abc ");
		testStringFalse(" abc");
		testStringFalse("    ");
	}

	/**
	 * Test de ConstraintRegex.
	 */
	@Test
	public void testConstraintRegex() {
		Assert.assertTrue(constraintRegex.checkConstraint(null));
		Assert.assertTrue(constraintRegex.checkConstraint("ABCDEFHIJKLMNOPQRSTUVWXYZ"));
		Assert.assertTrue(constraintRegex.checkConstraint("abcdefghijklmnopqrstuvwxyz"));
		Assert.assertTrue(constraintRegex.checkConstraint("0123456789"));
		Assert.assertTrue(constraintRegex.checkConstraint("_-"));
		Assert.assertTrue(constraintRegex.checkConstraint("abc0123ABC_-"));

		Assert.assertFalse(constraintRegex.checkConstraint("&abc"));
		Assert.assertFalse(constraintRegex.checkConstraint("éabc"));
		Assert.assertFalse(constraintRegex.checkConstraint("%abc"));
		Assert.assertFalse(constraintRegex.checkConstraint("'abc"));
	}

}
