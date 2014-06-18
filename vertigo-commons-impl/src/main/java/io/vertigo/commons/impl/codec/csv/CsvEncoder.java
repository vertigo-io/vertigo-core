package io.vertigo.commons.impl.codec.csv;

import io.vertigo.commons.codec.Encoder;

/**
 * Codec des CSV.
 * Spécifications encodage CSV :
 * http://www.rfc-editor.org/rfc/rfc4180.txt
 *
 * Règle : If double-quotes are used to enclose fields, then a double-quote
 *	       appearing inside a field must be escaped by preceding it with
 *	       another double quote. For example:       "aaa","b""bb","ccc"
 *
 * @author pchretien
 */
public final class CsvEncoder implements Encoder<String, String> {
	// caractere de remplace du caractère 13
	private static final char CHAR_11 = (char) 11;

	/** {@inheritDoc} */
	public String encode(final String toEncode) {
		if (toEncode == null || toEncode.length() == 0) { // perf
			return "";
		}
		//On double les double-quotes et le retour chariot 13 (sinon carré dans Excel)
		// ' '
		return toEncode.replace("\"", "\"\"").replace((char) 13, CHAR_11);

	}
}