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
package io.vertigo.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test de l'utilitaitre de manipulation des strings.
 * @author pchretien
 */
public final class StringUtilTest {
	private static final String XXX_YYY_ZZZ = "XXX_YYY_ZZZ";

	@Test
	public void testIsEmpty() {
		Assert.assertTrue(StringUtil.isEmpty(null));
		Assert.assertTrue(StringUtil.isEmpty(""));
		Assert.assertTrue(StringUtil.isEmpty("  "));
		Assert.assertFalse(StringUtil.isEmpty("a"));
		Assert.assertFalse(StringUtil.isEmpty(" a "));
	}

	/**
	 * Test de la fonction de remplacement.
	 */
	@Test
	public void testStringReplace() {
		Assert.assertEquals("azertyuiop", StringUtil.replace("azertYYuiop", "YY", "y"));
		Assert.assertEquals("yazertyuiopy", StringUtil.replace("YYazertYYuiopYY", "YY", "y"));
		Assert.assertEquals("yyyaYay", StringUtil.replace("YYYYYYaYaYY", "YY", "y"));
		Assert.assertEquals("YY", StringUtil.replace("YYY", "YY", "Y"));

		//On vérifie si la chaine à remplacer n'existe pas.
		Assert.assertEquals("azertyuiop", StringUtil.replace("azertyuiop", "ZZ", "Y"));
	}

	@Test(expected = NullPointerException.class)
	public void testStringReplaceWithNull() {
		StringUtil.replace((String) null, "YY", "Y");
	}

	@Test(expected = NullPointerException.class)
	public void testStringReplaceWithNullOldString() {
		StringUtil.replace("azertYYuiop", null, "Y");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testStringReplaceWithEmptyOldString() {
		StringUtil.replace("azertYYuiop", "", "Y");
	}

	@Test(expected = NullPointerException.class)
	public void testStringReplaceWithNullNewString() {
		StringUtil.replace("azertYYuiop", "YY", null);
	}

	@Test
	public void testCaseTransform() {
		Assert.assertEquals("XxxYyyZzz", StringUtil.constToUpperCamelCase(XXX_YYY_ZZZ));
		Assert.assertEquals("xxxYyyZzz", StringUtil.constToLowerCamelCase(XXX_YYY_ZZZ));
		Assert.assertEquals("xxxYZzz", StringUtil.constToLowerCamelCase("XXX_Y_ZZZ"));
		Assert.assertEquals("xxxYyy12", StringUtil.constToLowerCamelCase("XXX_YYY_12"));

		Assert.assertEquals("xxxYyy12Ppp", StringUtil.constToLowerCamelCase("XXX_YYY_12_PPP"));
		Assert.assertEquals("xxxYyy1", StringUtil.constToLowerCamelCase("XXX_YYY_1"));
		Assert.assertEquals("xxxYyy12_3", StringUtil.constToLowerCamelCase("XXX_YYY_12_3"));
		Assert.assertEquals("TAdresseAdr", StringUtil.constToUpperCamelCase("T_ADRESSE_ADR"));
		Assert.assertEquals("x2Yyy", StringUtil.constToLowerCamelCase("X_2_YYY"));
		Assert.assertEquals("X2Yyy", StringUtil.constToUpperCamelCase("X_2_YYY"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCaseTransformWithErrors() {
		StringUtil.constToLowerCamelCase("XXX_YYY12_PPP");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCaseTransformWithErrors2() {
		StringUtil.constToLowerCamelCase("XXX_YYY_12PPP");
	}

	@Test
	public void testCaseUnTransform() {
		Assert.assertEquals("X_Z_YYY", StringUtil.camelToConstCase("xZYyy"));
		Assert.assertEquals("X_Z_YYY", StringUtil.camelToConstCase("XZYyy"));
		Assert.assertEquals(XXX_YYY_ZZZ, StringUtil.camelToConstCase("XxxYyyZzz"));
		Assert.assertEquals(XXX_YYY_ZZZ, StringUtil.camelToConstCase("xxxYyyZzz"));
		Assert.assertEquals("XXX_Y_ZZZ", StringUtil.camelToConstCase("xxxYZzz"));
		Assert.assertEquals("XXX_YYY_12", StringUtil.camelToConstCase("xxxYyy12"));
		Assert.assertEquals("XXX_YYY_12PPP", StringUtil.camelToConstCase("xxxYyy12ppp"));
		Assert.assertEquals("XXX_YYY_12_PPP", StringUtil.camelToConstCase("xxxYyy12Ppp"));
		Assert.assertEquals("XXX_YYY_12_3", StringUtil.camelToConstCase("xxxYyy12_3"));
		Assert.assertEquals("XXX_Y_Y_Y_1", StringUtil.camelToConstCase("XxxYYY1"));
		Assert.assertEquals("T_ADRESSE_ADR", StringUtil.camelToConstCase("TAdresseAdr"));
	}

	@Test
	public void testCaseTransformBijection() {
		final String[] values = { XXX_YYY_ZZZ, "XXX_YYY_12", "XXX_YYY_12_PPP", "XXX_YYY_1", "XXX_YYY_12_3", "RESTE_A_PAYER", "T_ADRESSE_ADR", "XXX_2_Y", "X_2_YYY", "XXX_Z_Y", "X_Z_YYY", "2_YYY", "12_YYY", };

		for (final String value : values) {
			Assert.assertEquals(value, StringUtil.camelToConstCase(StringUtil.constToLowerCamelCase(value)));
			Assert.assertEquals(value, StringUtil.camelToConstCase(StringUtil.constToUpperCamelCase(value)));
		}
	}

	@Test
	public void testSimpleLetter() {
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('a'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('b'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('c'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('x'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('y'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('z'));
		//----
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('A'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('B'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('C'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('X'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('Y'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('Z'));
		//----
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('0'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('1'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('2'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('3'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('4'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('5'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('6'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('7'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('8'));
		Assert.assertTrue(StringUtil.isSimpleLetterOrDigit('9'));
	}

	@Test
	public void testNoSimpleLetter() {
		Assert.assertFalse(StringUtil.isSimpleLetterOrDigit('é'));
		Assert.assertFalse(StringUtil.isSimpleLetterOrDigit('à'));
		Assert.assertFalse(StringUtil.isSimpleLetterOrDigit('ù'));
		Assert.assertFalse(StringUtil.isSimpleLetterOrDigit('â'));
		Assert.assertFalse(StringUtil.isSimpleLetterOrDigit('"'));
		Assert.assertFalse(StringUtil.isSimpleLetterOrDigit('+'));
		Assert.assertFalse(StringUtil.isSimpleLetterOrDigit('-'));
		Assert.assertFalse(StringUtil.isSimpleLetterOrDigit('&'));
	}

	@Test
	public void testformat() {
		Assert.assertEquals("bonjour le monde", StringUtil.format("bonjour le monde"));
		Assert.assertEquals("bonjour le monde", StringUtil.format("bonjour {0} monde", "le"));
		Assert.assertEquals("bonjour le monde", StringUtil.format("bonjour {0} {1}", "le", "monde"));
		Assert.assertEquals("bonjour 'le' monde", StringUtil.format("bonjour ''{0}'' monde", "le"));
		Assert.assertEquals("bonjour 'le' monde", StringUtil.format("bonjour '{0}' monde", "le"));
		Assert.assertEquals("bonjour 'le' monde", StringUtil.format("bonjour ''{0}' monde", "le"));
	}

	@Test(expected = NullPointerException.class)
	public void testformatWithNull() {
		StringUtil.format(null);
	}

	@Test(expected = Exception.class)
	public void testformatWithError() {
		//Si on oublie de fermer une parenthèse
		StringUtil.format("bonjour {0 monde", "le");
	}
}
