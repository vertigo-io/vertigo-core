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
/*!! StringUtil */

package io.vertigo.assertion;

import io.vertigo.kernel.util.StringUtil;

import org.junit.Test;


/*!
#	StringUtil
It's a small tool that offers transformations on Strings
#	Examples

*!- Plumbing 
*/
public class MDStringUtil {

	@Test
	public void testIsEmpty() {
		/*! 
		##	StringUtil.isEmpty
		return True if string is null or contains only blanks characters
		*/
		boolean resultTrue = StringUtil.isEmpty(null);
		resultTrue = StringUtil.isEmpty("");
		resultTrue = StringUtil.isEmpty("  ");
		final boolean resultFalse = StringUtil.isEmpty(" abc");
		/*!- Plumbing */
		nop(resultTrue);
		nop(resultFalse);
	}

	/**
	 * Test de la fonction de remplacement.
	 */
	@Test
	public void testReplace() {
		/*! 
		##	StringUtil.replace
		This function replaces a string fragment by another
		*/
		final String result = StringUtil.replace("azertYYuiop", "YY", "y"); //result = "azertyuiop"
		/*!- Plumbing */
		nop(result);
	}

	@Test
	public void testCaseTransform() {
		/*! 
		##	StringUtil.constToCamelCase
		This function transform CONST to camelCase 
		
		The last boolean argument determines if the first character in upper or lowe case.
		*/
		String result = StringUtil.constToCamelCase("YELLOW_SUBMARINE", true); //==>YellowSubmarine

		result = StringUtil.constToCamelCase("YELLOW_SUBMARINE_49", true); //==>YellowSubmarine49

		result = StringUtil.constToCamelCase("U_S_A", true); //=>USA

		/*! you can not use ambigous CONST such as PLAYER1 */
		/*!- Plumbing */
		nop(result);
	}

	@Test
	public void testCaseUnTransform() {
		/*! 
		##	StringUtil.camelToConstCase
		This function transform camelCase to CONST
		*/
		String result = StringUtil.camelToConstCase("YellowSubmarine"); //==>YELLOW_SUBMARINE

		result = StringUtil.camelToConstCase("YellowSubmarine49"); //==>YELLOW_SUBMARINE_49

		result = StringUtil.constToCamelCase("USA", true); //=>U_S_A
		/*!- Plumbing */
		nop(result);
	}

	@Test
	public void testSimpleLetter() {
		/*! 
		##	StringUtil.isSimpleLetterOrDigit
		This function check if a character is a letter [a..z] or [A..Z] or a digit [0..9] 
		
		All other letters will return false : any accent, +, -,  , *.....
		
		*/
		boolean resultTrue = StringUtil.isSimpleLetterOrDigit('a');
		resultTrue = StringUtil.isSimpleLetterOrDigit('t');
		resultTrue = StringUtil.isSimpleLetterOrDigit('B');
		resultTrue = StringUtil.isSimpleLetterOrDigit('5');
		//----	
		final boolean resultFalse = StringUtil.isSimpleLetterOrDigit('+');
		/*!- Plumbing */
		nop(resultTrue);
		nop(resultFalse);
	}

	@Test
	public void testFormat() {
		/*! 
		##	StringUtil.format
		This function allow to format with single quotes
		the first argument can be 

		-	a String
		-	a stringBuilder 
		
		The two following samples return "hello world" 
		*/
		String result = StringUtil.format("hello world");
		result = StringUtil.format("hello {0}", "world");

		/*! In the following sample quotes are kept*/
		result = StringUtil.format("your command number '{0}' is ...", "Z45F"); //your command number 'Z45F' is ...
		//
		/*!- Plumbing */
		nop(result);
	}

	private static void nop(Object o) {
		//nop
	}
}
