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
package io.vertigo.shell.util;

import java.util.Map.Entry;

import com.google.gson.JsonElement;

/**
 * @author pchretien
 */
public final class JSonBeautifier {

	public static String beautify(final JsonElement jsonElement) {
		final StringBuilder sb = new StringBuilder();
		beautify(sb, jsonElement, 0);
		return sb.toString();
	}

	private static void appendCRLF(final StringBuilder sb) {
		sb.append("\r\n");
	}

	private static void appendSpaces(final StringBuilder sb, final int offset) {
		for (int i = 0; i < offset; i++) {
			sb.append("    ");
		}
	}

	private static void beautify(final StringBuilder sb, final JsonElement jsonElement, final int inOffset) {
		int offset = inOffset;
		if (jsonElement.isJsonArray()) {
			offset++;
			for (final JsonElement jsonElement2 : jsonElement.getAsJsonArray()) {
				//sb.append("- ");
				beautify(sb, jsonElement2, offset);
			}
			offset--;
		} else if (jsonElement.isJsonObject()) {
			for (final Entry<String, JsonElement> entry : jsonElement.getAsJsonObject().entrySet()) {
				appendSpaces(sb, offset);
				sb.append(entry.getKey());
				sb.append(" : ");
				if (entry.getValue().isJsonPrimitive()) {
					sb.append(entry.getValue());
					appendCRLF(sb);
				} else {
					offset++;
					appendCRLF(sb);
					beautify(sb, entry.getValue(), offset);
					offset--;
				}
			}
		} else if (jsonElement.isJsonPrimitive()) {
			//					appendSpaces(sb, offset);
			final String s = jsonElement.toString();
			if (s.startsWith("\"") && s.endsWith("\"")) {
				sb.append(s.substring(1, s.length() - 1));
			} else {
				sb.append(s);
			}
			appendCRLF(sb);
		} else {
			sb.append("???");
			//null
		}
	}
}
