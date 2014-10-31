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
package io.vertigo.quarto.impl.converter;

import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.lang.Assertion;
import io.vertigo.quarto.converter.ConverterManager;

import javax.inject.Inject;

/**
 * Implémentation standard du manager des conversions de documents.
 *
 * Conversions acceptés entre formats :
 *  - ODT
 *  - DOC
 *  - RTF
 *  - CSV
 *  - PDF
 *
 * @author pchretien, npiedeloup
 */
public final class ConverterManagerImpl implements ConverterManager {
	private final ConverterPlugin converterPlugin;

	/**
	 * Constructeur.
	 */
	@Inject
	public ConverterManagerImpl(final ConverterPlugin converterPlugin) {
		// La connexion au serveur openOffice est instanciée lors du start
		Assertion.checkNotNull(converterPlugin);
		//---------------------------------------------------------------------
		this.converterPlugin = converterPlugin;
	}

	/** {@inheritDoc} */
	public KFile convert(final KFile inputFile, final String format) {
		Assertion.checkNotNull(inputFile);
		Assertion.checkArgNotEmpty(format);
		// ---------------------------------------------------------------------
		return converterPlugin.convertToFormat(inputFile, format);
	}
}
