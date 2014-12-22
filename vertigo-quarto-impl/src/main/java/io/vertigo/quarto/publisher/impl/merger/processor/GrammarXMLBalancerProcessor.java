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
package io.vertigo.quarto.publisher.impl.merger.processor;

import io.vertigo.lang.Assertion;
import io.vertigo.quarto.publisher.model.PublisherData;

import java.util.Queue;

/**
 * Replace les tags <# #> pour les inscrire correctement dans le xml
 *
 * On cherche a les replacer de fa�on � ce que la position du tag de d�but et celui de fin
 * 	(ex: <#if ... #> <#endif#>) soit au m�me niveau de l'arbre XML,
 * ainsi qu'on les suppriment ou qu'on les multiplient (cas de <#loop#>)
 * le XML produit reste correct.
 * L'ODTCleaner devient alors inutil, en tout cas il n'a plus besoin de tenter
 * de rectifier (avec plus ou moins de scc�s) un XML corrompu.
 *
 * @author npiedeloup
 */
public final class GrammarXMLBalancerProcessor implements MergerProcessor {

	/** Configure le mode de ce XML Balancer :
	 * 	soit on d�place au plus pret, soit on �tend le XML pour inclure les balises
	 *  Dans le premier on peut retirer une balise pour equilibrer le XML, alors que sinon on les garde forc�ment
	 */
	private static final boolean MODE_CLOSER_CLOSED_XML = true;

	private static final String BEGIN_XML_CODE = "&lt;#";
	private static final String BEGIN_END_XML_CODE = "&lt;#end";
	private static final String END_XML_CODE = "#&gt;";

	// private static final String BEGIN_XML_CODE = "<#";
	// private static final String BEGIN_END_XML_CODE = "<#end";
	// private static final String END_XML_CODE = "#>";
	//
	//
	// public static void main(String[] args) {
	// //-----0 10 20 30 40 50 60 70
	// //-----123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789
	// StringBuilder output = new StringBuilder("<#loo#   ><#loo#   ><#endloo#><#loo#   ><#endloo#><#endloo#>");
	// System.out.println(findEndGrammarIndex(output, "loo", 1));
	// }

	/**
	 * Trouve une balise la balise de grammaire fermante, en tenant compte de la
	 * pr�sence �ventuelle de sous-balises
	 *
	 * @param grammarTag tag de grammaire (ex : "loop")
	 * @param fromIndex index � partir duquel il faut chercher
	 * @return la position de la balise de grammaire fermante, -1 si pas trouv�.
	 */
	private static int findEndGrammarIndex(final StringBuilder output, final String grammarTag, final int fromIndex) {
		int openCount = 1;

		int currentIndex = fromIndex;
		int nextBeginGramarIndex = output.indexOf(BEGIN_XML_CODE + grammarTag, currentIndex);
		int nextEndGramarIndex = output.indexOf(BEGIN_END_XML_CODE + grammarTag, currentIndex);

		while (true) {
			if (nextEndGramarIndex == -1) {
				// Pas de balise fermante trouv�
				return -1;
			} else if (nextBeginGramarIndex > -1 && nextBeginGramarIndex < nextEndGramarIndex) {
				// La prochaine balise de grammaire est ouvrante
				// => on continue apr�s cette balise
				openCount++;
				currentIndex = nextBeginGramarIndex + 1;
			} else if (openCount > 1) {
				// La prochaine balise de grammaire est fermante, mais elle
				// ferme une sous-balise
				// => on continue apr�s cette balise
				openCount--;
				currentIndex = nextEndGramarIndex + 1;
			} else if (nextEndGramarIndex > -1) {
				// La prochaine balise de grammaire est fermante et elle est au
				// bon niveau
				// => on a trouv�
				return nextEndGramarIndex;
			}

			nextBeginGramarIndex = output.indexOf(BEGIN_XML_CODE + grammarTag, currentIndex);
			nextEndGramarIndex = output.indexOf(BEGIN_END_XML_CODE + grammarTag, currentIndex);
		}
	}

	/** {@inheritDoc} */
	@Override
	public String execute(final String input, final PublisherData publisherData) {
		final StringBuilder output = new StringBuilder(input);
		String grammarTag;
		int beginGramarIndex;
		int beginGramarLenght;
		int endGramarIndex;
		int endGramarLenght;
		int beginXmlMoveToIndex;
		int endXmlMoveToIndex;
		Queue<TagXML> pileTag;
		int indexOffset;
		int firstBodyIndex;
		int lastBodyEndIndex;

		beginGramarIndex = nextBeginGramarIndex(output, 0);

		while (beginGramarIndex != -1) {

			final int endTagNameIndex = output.indexOf(" ", beginGramarIndex);

			Assertion.checkState(endTagNameIndex > 0, "La gramaire du XML source est incorrecte, le tag {0} (i:{1}) n''est pas construit correctement (il manque l'espace apres le nom de tag) ", output.substring(beginGramarIndex, beginGramarIndex + 10) + "...", beginGramarIndex);
			grammarTag = output.substring(beginGramarIndex + BEGIN_XML_CODE.length(), endTagNameIndex);
			beginGramarLenght = output.indexOf(END_XML_CODE, beginGramarIndex + BEGIN_XML_CODE.length()) - beginGramarIndex + END_XML_CODE.length();
			Assertion.checkState(beginGramarLenght > 0, "La gramaire du XML source est incorrecte, le tag <#{0} (i:{1}) n''est pas ferm� correctement (il manque #>) ", grammarTag, beginGramarIndex);

			endGramarIndex = findEndGrammarIndex(output, grammarTag, beginGramarIndex + beginGramarLenght);
			endGramarLenght = output.indexOf(END_XML_CODE, endGramarIndex + (BEGIN_END_XML_CODE + grammarTag).length()) - endGramarIndex + END_XML_CODE.length();
			Assertion.checkState(endGramarIndex > 0, "La gramaire du XML source est incorrecte, le tag {0} (i:{1}) n''est pas construit correctement (la balise de fin est introuvable)", grammarTag, beginGramarIndex);
			Assertion.checkState(endGramarLenght > 0, "La gramaire du XML source est incorrecte, le tag <#end{0} (i:{1}) n''est pas ferm� correctement (il manque #>)", grammarTag, endGramarIndex);
			// on determine la pile des tags internes non closes ou ouverte :
			final String content = output.substring(beginGramarIndex + beginGramarLenght, endGramarIndex);
			pileTag = ProcessorXMLUtil.extractUnrepeatableTag(content.toCharArray());
			int nbIteration = 0;
			while (!pileTag.isEmpty()) {
				TagXML firstOpenTag = null;
				TagXML lastCloseTag = null;
				for (final TagXML tag : pileTag) {
					if (!tag.isOpenTag()) {
						lastCloseTag = tag;
					} else {
						firstOpenTag = tag;
						break; // on a termin�
					}
				}
				beginXmlMoveToIndex = beginGramarIndex;
				endXmlMoveToIndex = endGramarIndex;
				indexOffset = beginGramarIndex + beginGramarLenght;

				// Les balises ne peuvent �tre d�plac�e au dela des elements affich�s, on cherche les bornes le 1er et le dernier corps de tag
				firstBodyIndex = ProcessorXMLUtil.getFirstBodyIndex(content) + indexOffset;
				lastBodyEndIndex = ProcessorXMLUtil.getLastBodyEndIndex(content) + indexOffset;

				if (lastCloseTag != null) {
					// si c'est un tag fermant, on �tend le d�but
					// on regarde pour inclure le tag ouvrant
					final int moveBackwardToIndex = output.lastIndexOf('<' + lastCloseTag.getName(), beginGramarIndex);
					// Ou exclure le tag fermant
					final int moveForwardToIndex = lastCloseTag.getIndex() + indexOffset + lastCloseTag.getLength();
					Assertion.checkState(moveBackwardToIndex != -1, "Le XML source est incorrect, la tag de d�but : {0} n''a pas �t� trouv�e", '<' + lastCloseTag.toString() + '>');
					if (!MODE_CLOSER_CLOSED_XML || beginGramarIndex - moveBackwardToIndex <= moveForwardToIndex - beginGramarIndex - beginGramarLenght || moveForwardToIndex > firstBodyIndex) {
						beginXmlMoveToIndex = moveBackwardToIndex;
					} else {
						beginXmlMoveToIndex = moveForwardToIndex;
					}
				}
				if (firstOpenTag != null) {
					// si c'est un tag ouvrant, on �tend la fin
					// on regarde pour inclure le tag fermant
					int moveForwardToIndex = output.indexOf("</" + firstOpenTag.getName(), endGramarIndex);
					moveForwardToIndex = output.indexOf(">", moveForwardToIndex) + 1;
					// ou pour exclure le tag ouvrant
					final int moveBackwardToIndex = firstOpenTag.getIndex() + indexOffset;
					Assertion.checkState(moveForwardToIndex != -1, "Le XML source est incorrect, la tag de fin : {0} n''a pas �t� trouv�e", "</" + firstOpenTag + '>');

					if (MODE_CLOSER_CLOSED_XML && endGramarIndex - moveBackwardToIndex < moveForwardToIndex - endGramarIndex - endGramarLenght && moveBackwardToIndex >= lastBodyEndIndex) {
						endXmlMoveToIndex = moveBackwardToIndex;
					} else {
						endXmlMoveToIndex = moveForwardToIndex;
					}
				}

				// on deplace
				if (beginXmlMoveToIndex != beginGramarIndex) {
					moveGramar(beginGramarIndex, beginXmlMoveToIndex, beginGramarLenght, output);

					// On recalcul la position pour faciliter le test de postcondition
					if (beginXmlMoveToIndex > beginGramarIndex) {
						beginGramarIndex = beginXmlMoveToIndex - beginGramarLenght;
					} else {
						beginGramarIndex = beginXmlMoveToIndex;
					}
				}
				if (endXmlMoveToIndex != endGramarIndex) {
					moveGramar(endGramarIndex, endXmlMoveToIndex, endGramarLenght, output);

					// On recalcul la position pour faciliter le test de postcondition
					if (endXmlMoveToIndex > endGramarIndex) {
						endGramarIndex = endXmlMoveToIndex - endGramarLenght;
					} else {
						endGramarIndex = endXmlMoveToIndex;
					}
				}

				// on test
				pileTag = ProcessorXMLUtil.extractUnrepeatableTag(output.substring(beginGramarIndex + beginGramarLenght, endGramarIndex).toCharArray());
				Assertion.checkState(nbIteration++ < 2 || pileTag.isEmpty(), "Le XML n''a pas �t� corrig� apr�s 3 it�rations : il reste : {1}\ndans le corps de la balise {2}\ncontent: {0}", output.toString(), pileTag, output.substring(beginGramarIndex, beginGramarIndex + beginGramarLenght));
			}
			Assertion.checkState(pileTag.isEmpty(), "Le XML n''a pas �t� corrig� : il reste : {1}\ndans le corps de la balise {2}\ncontent: {0}", output.toString(), pileTag, output.substring(beginGramarIndex, beginGramarIndex + beginGramarLenght));
			// On recup�re le prochain
			beginGramarIndex = nextBeginGramarIndex(output, beginGramarIndex + beginGramarLenght);
		}

		return output.toString();
	}

	private static void moveGramar(final int gramarIndex, final int moveToIndex, final int gramarLenght, final StringBuilder output) {
		final String copy = output.substring(gramarIndex, gramarIndex + gramarLenght);
		if (moveToIndex < gramarIndex) {
			output.delete(gramarIndex, gramarIndex + gramarLenght);
			output.insert(moveToIndex, copy);
		} else {
			output.insert(moveToIndex, copy);
			output.delete(gramarIndex, gramarIndex + gramarLenght);
		}
	}

	private static int nextBeginGramarIndex(final StringBuilder output, final int fromIndex) {
		int index = output.indexOf(BEGIN_XML_CODE, fromIndex);
		while (index != -1 && (BEGIN_END_XML_CODE.equals(output.substring(index, index + BEGIN_END_XML_CODE.length())) || output.charAt(index + BEGIN_XML_CODE.length()) == '=')) {
			index = output.indexOf(BEGIN_XML_CODE, index + BEGIN_END_XML_CODE.length());
		}
		return index;
	}
}
