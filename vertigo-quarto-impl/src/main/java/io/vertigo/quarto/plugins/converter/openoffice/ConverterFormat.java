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
package io.vertigo.quarto.plugins.converter.openoffice;

import io.vertigo.lang.Assertion;

/**
 * Formats de sortie supportés par Open Office.
 * @author pchretien, npiedeloup
 */
enum ConverterFormat {

	/**
	 * OpenOffice Text.
	 */
	ODT("application/vnd.oasis.opendocument.text"),

	/**
	 * Document Word.
	 */
	DOC("application/vnd.ms-word"),

	/**
	 * Document RTF.
	 */
	RTF("text/rtf"),

	/**
	 * Document TXT.
	 */
	TXT("text/plain"),

	/**
	 * Document PDF.
	 */
	PDF("application/pdf"),

	//Types non gérés
	// Document Word XML.
	//DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),

	// Comma Separated Value.
	//CSV("application/csv.ms-excel"),
	;

	private final String typeMime;

	/**
	 * Constructeur.
	 * @param typeMime Type mime associé
	 */
	private ConverterFormat(final String typeMime) {
		this.typeMime = typeMime;
	}

	/**
	 * @return Type mime associé
	 */
	String getTypeMime() {
		return typeMime;
	}

	/**
	 * Récupère le Format associé à ce code de format.
	 * @param sFormat code de format (non null et doit être en majuscule)
	 * @return Format associé.
	 */
	static ConverterFormat find(final String sFormat) {
		Assertion.checkNotNull(sFormat);
		Assertion.checkArgument(sFormat.equals(sFormat.trim().toUpperCase()), "Le format doit être en majuscule, et sans espace");
		//-----
		return valueOf(sFormat);
	}
}
