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
package io.vertigo.kernel.util;

import io.vertigo.kernel.util.StringUtil;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test de l'utilitaitre de manipulation des strings.
 *
 * @author pchretien
 * @version $Id: StringUtilTest.java,v 1.1 2013/10/09 14:04:13 pchretien Exp $
 */
public final class StringUtilTest {
	private static final String XXX_YYY_ZZZ = "XXX_YYY_ZZZ";

	@Test
	public void testIsEmpty() {
		Assert.assertEquals(true, StringUtil.isEmpty(null));
		Assert.assertEquals(true, StringUtil.isEmpty(""));
		Assert.assertEquals(true, StringUtil.isEmpty("  "));
		Assert.assertEquals(false, StringUtil.isEmpty("a"));
		Assert.assertEquals(false, StringUtil.isEmpty(" a "));
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

		//On v�rifie si la chaine � remplacer n'existe pas.
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
		Assert.assertEquals("XxxYyyZzz", StringUtil.constToCamelCase(XXX_YYY_ZZZ, true));
		Assert.assertEquals("xxxYyyZzz", StringUtil.constToCamelCase(XXX_YYY_ZZZ, false));
		Assert.assertEquals("xxxYZzz", StringUtil.constToCamelCase("XXX_Y_ZZZ", false));
		Assert.assertEquals("xxxYyy12", StringUtil.constToCamelCase("XXX_YYY_12", false));

		Assert.assertEquals("xxxYyy12Ppp", StringUtil.constToCamelCase("XXX_YYY_12_PPP", false));
		Assert.assertEquals("xxxYyy1", StringUtil.constToCamelCase("XXX_YYY_1", false));
		Assert.assertEquals("xxxYyy12_3", StringUtil.constToCamelCase("XXX_YYY_12_3", false));
		Assert.assertEquals("TAdresseAdr", StringUtil.constToCamelCase("T_ADRESSE_ADR", true));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCaseTransformWithErrors() {
		StringUtil.constToCamelCase("XXX_YYY12_PPP", false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCaseTransformWithErrors2() {
		StringUtil.constToCamelCase("XXX_YYY_12PPP", false);
	}

	@Test
	public void testCaseUnTransform() {
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
		final String[] values = { XXX_YYY_ZZZ, "XXX_YYY_12", "XXX_YYY_12_PPP", "XXX_YYY_12_3", "RESTE_A_PAYER", "T_ADRESSE_ADR" };

		for (final String value : values) {
			Assert.assertEquals(value, StringUtil.camelToConstCase(StringUtil.constToCamelCase(value, false)));
			Assert.assertEquals(value, StringUtil.camelToConstCase(StringUtil.constToCamelCase(value, true)));
		}
	}

	@Test
	public void testSimpleLetter() {
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('a'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('b'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('c'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('x'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('y'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('z'));
		//----	
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('A'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('B'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('C'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('X'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('Y'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('Z'));
		//----
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('0'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('1'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('2'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('3'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('4'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('5'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('6'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('7'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('8'));
		Assert.assertEquals(true, StringUtil.isSimpleLetterOrDigit('9'));
	}

	@Test
	public void testNoSimpleLetter() {
		Assert.assertEquals(false, StringUtil.isSimpleLetterOrDigit('�'));
		Assert.assertEquals(false, StringUtil.isSimpleLetterOrDigit('�'));
		Assert.assertEquals(false, StringUtil.isSimpleLetterOrDigit('�'));
		Assert.assertEquals(false, StringUtil.isSimpleLetterOrDigit('�'));
		Assert.assertEquals(false, StringUtil.isSimpleLetterOrDigit('"'));
		Assert.assertEquals(false, StringUtil.isSimpleLetterOrDigit('+'));
		Assert.assertEquals(false, StringUtil.isSimpleLetterOrDigit('-'));
		Assert.assertEquals(false, StringUtil.isSimpleLetterOrDigit('&'));
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
		//Si on oublie de fermer une parenth�se
		StringUtil.format("bonjour {0 monde", "le");
	}
}
