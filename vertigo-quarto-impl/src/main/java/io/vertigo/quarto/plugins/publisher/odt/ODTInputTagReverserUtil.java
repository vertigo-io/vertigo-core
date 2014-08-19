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

import io.vertigo.quarto.publisher.impl.merger.processor.ParserXMLHandler;
import io.vertigo.quarto.publisher.impl.merger.processor.ProcessorXMLUtil;

/**
 * Classe de nettoyage d'une arborescence XML.
 * Gestion du cas ou il y a des balises ouvertes non fermées.
 *
 * @author brenard
 * @version $Id: ODTInputTagReverserUtil.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
final class ODTInputTagReverserUtil {
	private static final String INPUT_TAG = "text:text-input";
	private static final String DESCRIPTION_ATTRIBUTE = "text:description";

	/**
	 * Constructeur privé pour classe utilitaire
	 */
	private ODTInputTagReverserUtil() {
		super();
	}

	/**
	 * Corrige le contenu qui est une arborescence XML pour inverser l'attribut
	 * text:description de la balise text:text-input avec son contenu.
	 *
	 * @param xmlContent Arborescence XML.
	 * @return Arborescence corrigée.
	 */
	static String reverseInputTag(final String xmlContent) {
		final StringBuilder contentClean = ProcessorXMLUtil.parseXMLContent(xmlContent, INPUT_TAG, new TagInverser());
		return contentClean.toString();
	}

	static final class TagInverser implements ParserXMLHandler {

		/** {@inheritDoc} */
		public void onNoBodyEndTag(final String tagXML, final StringBuilder output) {
			//rien
		}

		/** {@inheritDoc} */
		public void onBodyEndTag(final String tagXML, final String bodyContent, final StringBuilder output) {
			output.append(ODTInputTagReverserUtil.doInversion(tagXML, bodyContent));
		}
	}

	static String doInversion(final String tagToInverse, final String bodyContent) {
		final int indexAttribut = tagToInverse.indexOf(DESCRIPTION_ATTRIBUTE);
		if (indexAttribut > 0) {
			final int indexFinAttribut = tagToInverse.indexOf("\"", indexAttribut + DESCRIPTION_ATTRIBUTE.length() + 2);
			final String valueAttribut = tagToInverse.substring(indexAttribut + DESCRIPTION_ATTRIBUTE.length() + 2, indexFinAttribut);
			final int indexDebutBody = tagToInverse.indexOf(bodyContent, indexFinAttribut);

			return new StringBuilder(tagToInverse.length())//
					.append(tagToInverse.substring(0, indexAttribut))//
					.append(DESCRIPTION_ATTRIBUTE)//
					.append("=\"")//
					.append(bodyContent)//
					.append("\">")//
					.append(valueAttribut)//
					.append(tagToInverse.substring(indexDebutBody + bodyContent.length()))//
					.toString();//
		}
		return "";
	}
}
