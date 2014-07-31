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
package io.vertigo.quarto.plugins.publisher.odt;

import io.vertigo.quarto.publisher.impl.merger.processor.MergerProcessor;
import io.vertigo.quarto.publisher.model.PublisherData;

/**
 * Cleaner de xml de fichier ODT.
 * Ce processor effectue plusieurs opérations de rectification du XML d'un fichier ODT.
 * - 1. Nettoyage du XML en fermant les balises
 * - 2. Suppression des balises de script
 * @author npiedeloup
 * @version $Id: ODTCleanerProcessor.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
final class ODTCleanerProcessor implements MergerProcessor {
	private static final String SCRIPT_TAG = "text:script";
	private static final String INPUT_TAG = "text:text-input";

	/** {@inheritDoc} */
	public String execute(final String xmlInput, final PublisherData publisherData) {
		String xmlOutput;
		/*
		 * Malgré le preprocessor qui replace les balises, on laisse le cleaner car il peut etre nécessaire pour ceux n'utilisant que les balises <% et pas <#
		 */
		// 1. Nettoyage du XML en fermant les balises
		xmlOutput = ODTCleanerUtil.clean(xmlInput);

		// 2. Suppression des balises de script
		xmlOutput = ODTTagRemoverUtil.removeTag(xmlOutput, SCRIPT_TAG, false);

		// On peut retirer les balises text-input, car les \n sont encodés en <text:line-break/>
		// il réagit très mal avec la justification totale, mais de la même façon que la balise text-input
		xmlOutput = ODTTagRemoverUtil.removeTag(xmlOutput, INPUT_TAG, true);

		return xmlOutput;
	}
}
