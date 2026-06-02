/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.util;

import java.text.MessageFormat;
import java.util.regex.Pattern;

import io.vertigo.core.lang.Assertion;

/**
 * The StringUtil class provides useful methods to manipulate strings.
 *
 * @author  pchretien
 */
public final class StringUtil {
	private static final Pattern UPPER_CAMEL_CASE_PATTERN = Pattern.compile("[A-Z][a-zA-Z0-9]*");
	private static final Pattern LOWER_CAMEL_CASE_PATTERN = Pattern.compile("[a-z][a-zA-Z0-9]*");

	/**
	 * Constructor
	 */
	private StringUtil() {
		//private constructor
	}

	/**
	 * Tests if a string is blank.
	 * ie null or blank (space, \t \n \r \p ...)
	 * @param strValue String
	 * @return true if the string contains only blank characters
	 * @see java.lang.Character isWhitespace(char)
	 */
	public static boolean isBlank(final String strValue) {
		return strValue == null || strValue.isBlank();
	}

	/**
	 * Lowercases the first letter.
	 * @param strValue non null String
	 * @return String with the first letter in lowercase
	 */
	public static String first2LowerCase(final String strValue) {
		Assertion.check().isNotNull(strValue);
		//-----
		if (strValue.isEmpty() || !Character.isUpperCase(strValue.codePointAt(0))) {
			return strValue;
		}
		return Character.toLowerCase(strValue.charAt(0)) + strValue.substring(1);
	}

	/**
	 * Uppercases the first letter.
	 *
	 * @param strValue non null String
	 * @return String with the first letter in uppercase
	 */
	public static String first2UpperCase(final String strValue) {
		Assertion.check().isNotNull(strValue);
		//-----
		if (strValue.isEmpty() || !Character.isLowerCase(strValue.codePointAt(0))) {
			return strValue;
		}
		return Character.toUpperCase(strValue.charAt(0)) + strValue.substring(1);
	}

	/**
	 * XXX_YYY_ZZZ -> xxxYyyZzz.
	 * @param str the string to transform
	 * @return camelCase
	 */
	public static String constToLowerCamelCase(final String str) {
		return constToCamelCase(str, false);
	}

	/**
	 * XXX_YYY_ZZZ -> XxxYyyZzz.
	 * @param str the string to transform
	 * @return CamelCase
	 */
	public static String constToUpperCamelCase(final String str) {
		return constToCamelCase(str, true);
	}

	/**
	 * XXX_YYY_ZZZ -> XxxYyyZzz ou xxxYyyZzz.
	 * @param str the string to transform
	 * @param first2UpperCase whether the first letter should be uppercased
	 * @return a string corresponding to str in lowercase without underscores,
	 * except for the first letters after underscores in str
	 */
	private static String constToCamelCase(final String str, final boolean first2UpperCase) {
		Assertion.check()
				.isNotNull(str)
				.isTrue(str.length() > 0, "Invalid string to transform (must not be empty)")
				.isFalse(str.contains("__"), "Invalid string to transform : {0} (__ is forbidden)", str);
		//-----
		final StringBuilder result = new StringBuilder();
		boolean upper = first2UpperCase;
		Boolean digit = null;
		final int length = str.length();
		char c;
		for (int i = 0; i < length; i++) {
			c = str.charAt(i);
			if (c == '_') {
				if (digit != null
						&& digit
						&& i + 1 < length && Character.isDigit(str.charAt(i + 1))) {
					result.append('_');
				}
				digit = null;
				upper = true;
			} else {
				if (digit != null) {
					Assertion.check().isTrue(digit.equals(Character.isDigit(c)), "Invalid string to transform : {0} (letters and digits must always be separated by _)", str);
				}
				digit = Character.isDigit(c);

				if (upper) {
					result.append(Character.toUpperCase(c));
					upper = false;
				} else {
					result.append(Character.toLowerCase(c));
				}
			}
		}
		return result.toString();
	}

	/**
	 * Digits are treated as uppercase letters.
	 * XxxYyyZzz or xxxYyyZzz -> XXX_YYY_ZZZ
	 * XxxYZzz or xxxYZzz -> XXX_Y_ZZZ
	 * Xxx123 -->XXX_123
	 * XxxYzw123 --> (forbidden)
	 * Xxx123Y --> XXX_123_Y.
	 * Xxx123y --> XXX_123Y.
	 * @param str the string to transform
	 * @return Constant case string (inverse of caseTransform)
	 */
	public static String camelToConstCase(final String str) {
		return camelToConstCase(str, false);
	}

	/**
	 * Digits are treated as uppercase letters.
	 * XxxYyyZzz or xxxYyyZzz -> xxx_yyy_zzz
	 * XxxYZzz or xxxYZzz -> xxx_y_zzz
	 * Xxx123 -->xxx_123
	 * XxxYzw123 --> (forbidden)
	 * Xxx123Y --> xxx_123_y.
	 * Xxx123y --> xxx_123y.
	 * @param str the string to transform
	 * @return Snake case string (inverse of caseTransform)
	 */
	public static String camelToSnakeCase(final String str) {
		return camelToConstCase(str, true);
	}

	private static String camelToConstCase(final String str, final boolean lowerCase) {
		Assertion.check()
				.isNotNull(str)
				.isTrue(str.length() > 0, "Invalid string to transform");
		//-----
		final StringBuilder result = new StringBuilder();
		final int length = str.length();
		char c;
		boolean isDigit = false;
		for (int i = 0; i < length; i++) {
			c = str.charAt(i);
			if (Character.isDigit(c) || c == '_') {
				if (i > 0 && !isDigit) {
					result.append('_');
				}
				isDigit = true;
			} else if (Character.isUpperCase(c)) {
				if (i > 0) {
					result.append('_');
				}
				isDigit = false;
			} else {
				isDigit = false;
			}
			result.append(lowerCase ? Character.toLowerCase(c) : Character.toUpperCase(c));
		}
		return result.toString();
	}

	/**
	 * Tests if a string is in UpperCamelCase.
	 * @param testString string to test
	 * @return boolean
	 */
	public static boolean isUpperCamelCase(final String testString) {
		return UPPER_CAMEL_CASE_PATTERN.matcher(testString).matches();
	}

	/**
	 * Tests if a string is in lowerCamelCase.
	 * @param testString string to test
	 * @return boolean
	 */
	public static boolean isLowerCamelCase(final String testString) {
		return LOWER_CAMEL_CASE_PATTERN.matcher(testString).matches();
	}

	/**
	 * Tests if a character is a simple letter (lowercase or uppercase, no accent)
	 * or a digit.
	 * @param c character
	 * @return boolean
	 */
	public static boolean isSimpleLetterOrDigit(final char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9';
	}

	/**
	 * Replaces all occurrences of a pattern within a string with another.
	 * Replacement is forward-only, not recursive.
	 * Note: for char replacement, String.replace(char old, char new) is more efficient.
	 *
	 * @param str String
	 * @param oldStr String to replace
	 * @param newStr Replacement string
	 * @return Replaced string
	 */
	public static String replace(final String str, final String oldStr, final String newStr) {
		Assertion.check()
				.isNotNull(str)
				.isNotNull(oldStr)
				.isTrue(oldStr.length() > 0, "The string to replace must not be empty")
				.isNotNull(newStr);
		//-----
		return str.replace(oldStr, newStr);
	}

	/**
	 * Replaces all occurrences of a pattern within a StringBuilder with another.
	 * Replacement is forward-only, not recursive.
	 * The StringBuilder is modified in place, hence no return value.
	 * @param str StringBuilder
	 * @param oldStr String to replace
	 * @param newStr Replacement string
	 */
	public static void replace(final StringBuilder str, final String oldStr, final String newStr) {
		Assertion.check()
				.isNotNull(str)
				.isNotNull(oldStr)
				.isTrue(oldStr.length() > 0, "The string to replace must not be empty")
				.isNotNull(newStr);
		//-----
		int index = str.indexOf(oldStr);
		while (index != -1) {
			str.replace(index, index + oldStr.length(), newStr);
			index = str.indexOf(oldStr, index + newStr.length());
		}
	}

	/**
	 * Merges a string with parameters using MessageFormat.
	 * Characters { } are forbidden unless escaped with \\.
	 * @param msg String in MessageFormat format
	 * @param params message parameters
	 * @return Merged string
	 */
	public static String format(final String msg, final Object... params) {
		Assertion.check().isNotNull(msg);
		//-----
		if (params == null || params.length == 0) {
			return msg;
		}
		//Handle double quotes
		//Single-quote existing double quotes.
		//Then double all single quotes so no unmatched single quote remains.
		final StringBuilder newMsg = new StringBuilder(msg);
		replace(newMsg, "''", "'");
		replace(newMsg, "'", "''");
		replace(newMsg, "\\{", "'{'");
		replace(newMsg, "\\}", "'}'");
		return MessageFormat.format(newMsg.toString(), params);
	}
}
