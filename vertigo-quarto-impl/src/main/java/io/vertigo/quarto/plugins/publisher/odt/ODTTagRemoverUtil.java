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
 * Gestion de la suppression d'une balise.
 *
 * @author brenard
 */
final class ODTTagRemoverUtil {
	/**
	 * Constructeur privé pour classe utilitaire
	 */
	private ODTTagRemoverUtil() {
		//RAS
	}

	/**
	 * Corrige le contenu qui est une arborescence XML pour supprimer la balise et son contenu.
	 *
	 * @param xmlContent Arborescence XML.
	 * @param tag Balise à supprimer.
	 * @return Arborescence corrigée.
	 */
	public static String removeTag(final String xmlContent, final String tag) {
		return removeTag(xmlContent, tag, false);
	}

	/**
	 * Corrige le contenu qui est une arborescence XML pour supprimer la balise.
	 *
	 * @param xmlContent Arborescence XML.
	 * @param tag Balise à supprimer.
	 * @param keepBody Indique si le body doit etre conservé lors de la suppression du tag
	 * @return Arborescence corrigée.
	 */
	public static String removeTag(final String xmlContent, final String tag, final boolean keepBody) {
		final StringBuilder contentClean = ProcessorXMLUtil.parseXMLContent(xmlContent, tag, new TagRemover(keepBody));
		return contentClean.toString();
	}

	private static final class TagRemover implements ParserXMLHandler {
		final boolean keepBody;

		TagRemover(final boolean keepBody) {
			this.keepBody = keepBody;
		}

		/** {@inheritDoc} */
		public void onNoBodyEndTag(final String tagXML, final StringBuilder output) {
			//rien
		}

		/** {@inheritDoc} */
		public void onBodyEndTag(final String tagXML, final String bodyContent, final StringBuilder output) {
			if (keepBody) {
				output.append(bodyContent);
			}
		}
	}

}
