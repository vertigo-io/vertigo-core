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

import io.vertigo.kernel.util.StringUtil;

import java.util.Stack;

import org.apache.log4j.Logger;

/**
 * Classe de nettoyage d'une arborescence XML.
 * Gestion du cas ou il y a des balises ouvertes non ferm�es.
 *
 * @author brenard
 * @version $Id: ODTCleanerUtil.java,v 1.2 2014/02/27 10:40:19 pchretien Exp $
 */
final class ODTCleanerUtil {

	private static final Logger LOGGER = Logger.getLogger(ODTCleanerUtil.class);

	/**
	 * Constructeur priv� pour classe utilitaire.
	 */
	private ODTCleanerUtil() {
		//RAS
	}

	/**
	 * Corrige le contenu qui est une arborescence XML pour g�rer le cas
	 * ou il y a des balises ouvertes non ferm�es.
	 *
	 * @param xmlContent Arborescence XML.
	 * @return Arborescence nettoy�e.
	 */
	static String clean(final String xmlContent) {
		final StringBuilder contentClean = new StringBuilder();
		cleanContent(xmlContent.toCharArray(), contentClean);
		return contentClean.toString();
	}

	private static void cleanContent(final char[] content, final StringBuilder contentClean) {
		// en jdk 1.6, il est un peu plus performant (synchronizeds) de faire :
		// Deque<String> pileBalise = new ArrayDeque<String>();
		final Stack<String> pileBalise = new Stack<>();

		// On parcours le contenu
		char current;
		final StringBuilder currentOuvrante = new StringBuilder();
		final StringBuilder currentFermante = new StringBuilder();
		boolean baliseOuvrante = false;
		boolean baliseOuvranteEnCours = false;
		boolean baliseFermante = false;
		boolean baliseFermanteEnCours = false;

		final int length = content.length;
		for (int i = 0; i < length; i++) {
			current = content[i];
			// debut de balise
			if (current == '<') {
				// On regarde s'il s'agit d'une ouverture ou d'une fermeture
				if (content[i + 1] == '/') {
					baliseFermante = true;
					baliseFermanteEnCours = true;
					i += 2;
					current = content[i];
				} else if (content[i + 1] != ' ') {
					contentClean.append(current);
					i++;
					current = content[i];
					baliseOuvrante = true;
					baliseOuvranteEnCours = true;
				}
			}

			// On ajoute le contenu courant que dans le cas ou l'on ne traite pas de
			// balise fermante. En effet si la balise fermante courante ne correspond
			// pas � le derni�re balise ouvrante alors il faut inserer la bonne avant
			// de mettre la balise fermante courante.
			if (!baliseFermanteEnCours) {
				contentClean.append(current);
			}

			// Cas particulier du <? xml ?>
			if (baliseOuvrante && current == '?') {
				currentOuvrante.setLength(0);
				baliseOuvrante = false;
				baliseOuvranteEnCours = false;
			}

			// Si on rencontre un de ces caract�res alors on connait le nom de la balise
			// ouvrante
			if (baliseOuvrante && (current == '/' || current == ' ' || current == '>')) {
				baliseOuvrante = false;
			}

			// On ajoute la balise ouvrante dans la pile lorsque l'on a atteind
			// le signe de fermeture
			if (current == '>' && baliseOuvranteEnCours) {
				if (content[i - 1] != '/') {
					pileBalise.push(currentOuvrante.toString());
				}
				baliseOuvranteEnCours = false;
				currentOuvrante.setLength(0);
			}

			// Lorsque l'on a atteind la fin de la balise fermante alors on regarde
			// si elle correspond bien � la derni�re balise ouvrante
			if (baliseFermante && current == '>') {
				fermeBalisesOuvertes(contentClean, pileBalise, currentFermante);
				currentFermante.setLength(0);
				baliseFermante = false;
				baliseFermanteEnCours = false;
			}

			if (baliseOuvrante) {
				currentOuvrante.append(current);
			}

			if (baliseFermante) {
				currentFermante.append(current);
			}
		}
	}

	private static void fermeBalisesOuvertes(final StringBuilder contentClean, final Stack<String> pileBalise, final StringBuilder currentFermante) {
		//Si la balise fermante n'est pas pr�sent dans la pile des balises d�j� ouvertes, c'est qu'elle a disparu lors de la fusion,
		//on retire alors la balise fermante (corrige le nullPointer lors des pop() ).
		if (!pileBalise.contains(currentFermante.toString())) {
			LOGGER.warn(StringUtil.format("La balise fermante </{0}> n'est plus ouverte dans le document g�n�r�, elle est retir� du document.", currentFermante));
		} else {
			String lastBalise = pileBalise.pop();
			//Tant que la balise fermante ne correspond pas � la
			// balise ouvrante alors on ferme les balises ouvrantes
			while (currentFermante.length() != lastBalise.length() && currentFermante.indexOf(lastBalise) != 0) {
				contentClean.append("</").append(lastBalise).append('>');
				lastBalise = pileBalise.pop();
			}
			contentClean.append("</").append(currentFermante).append('>');
		}
	}
}
