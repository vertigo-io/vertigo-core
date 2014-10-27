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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe utilitaire pour une arborescence XML.
 *
 * @author npiedeloup
 */
public final class ProcessorXMLUtil {
	/**
	 * Constructeur privé pour classe utilitaire
	 */
	private ProcessorXMLUtil() {
		//RAS
	}

	/**
	 * Retourne la pile des Tags présents dans le xml passé en parametre.
	 * Cette pile retourne les balises completes (ie: avec les attributs)
	 * @param content contenu XML
	 * @return pile des tag XML
	 */
	private static Deque<TagXML> extractTagStack(final char[] content) {
		final Deque<TagXML> pileTag = new ArrayDeque<>();

		// On parcours le contenu
		char current;
		final StringBuilder currentEndTag = new StringBuilder();
		final StringBuilder currentBeginTag = new StringBuilder();
		int currentEndTagIndex = -1;
		int currentBeginTagIndex = -1;
		boolean beginTagEnCours = false;
		boolean endTagEnCours = false;

		final int length = content.length;
		for (int i = 0; i < length; i++) {
			current = content[i];
			// debut de tag
			if (current == '<') {
				// On regarde s'il s'agit d'une ouverture ou d'une fermeture
				if (content[i + 1] == '/') {
					currentEndTagIndex = i;
					currentEndTag.setLength(0);
					currentEndTag.append("</");
					endTagEnCours = true;
					i += 2;
					current = content[i];
				} else if (content[i + 1] != ' ') {
					currentBeginTagIndex = i;
					currentBeginTag.setLength(0);
					currentBeginTag.append('<');
					beginTagEnCours = true;
					i++;
					current = content[i];
				}
			}

			// Cas particulier du <? xml ?>
			if (beginTagEnCours && current == '?') {
				currentBeginTag.setLength(0);
				beginTagEnCours = false;
			}

			// On ajoute la tag ouvrante dans la pile lorsque l'on a atteind
			// le signe de fermeture
			if (current == '>' && beginTagEnCours) {
				if (content[i - 1] != '/') {
					currentBeginTag.append('>');
					pileTag.add(new TagXML(currentBeginTag.toString(), currentBeginTagIndex));
				}
				beginTagEnCours = false;
				currentBeginTag.setLength(0);
			}

			if (endTagEnCours && current == '>') {
				currentEndTag.append('>');
				pileTag.add(new TagXML(currentEndTag.toString(), currentEndTagIndex));
				currentEndTag.setLength(0);
				endTagEnCours = false;
			}

			if (beginTagEnCours) {
				currentBeginTag.append(current);
			}
			if (endTagEnCours) {
				currentEndTag.append(current);
			}
		}
		return pileTag;
	}

	/**
	 * Recupere la pile des tags qui ne sont pas ouvert et ceux qui ne sont pas
	 * fermés correctement dans le XML passé en parametre.
	 * @param content Extrait d'un XML correct.
	 * @return Pile des tags mal ouverts ou mal fermés
	 */
	static Deque<TagXML> extractUnbalancedTag(final char[] content) {
		// en jdk 1.6, il est un peu plus performant (synchronizeds) de faire :
		// Deque<String> pileTag = new ArrayDeque<String>();
		final Deque<TagXML> fullTagStack = extractTagStack(content);
		final Deque<TagXML> openedTagStack = new ArrayDeque<>();
		String tagName;
		for (final TagXML tag : fullTagStack) {
			if (!tag.isOpenTag()) {
				tagName = tag.getName();
				if (!openedTagStack.isEmpty() && tagName.equals(openedTagStack.getLast().getName()) && openedTagStack.getLast().isOpenTag()) {
					openedTagStack.pollLast();
				} else {
					openedTagStack.addLast(tag);
				}
			} else if (tag.hasBody()) {
				openedTagStack.addLast(tag);
			}
		}
		return openedTagStack;
	}

	/**
	 * Retourne les tags qui posent problème si l'on cherche à supprimer ou répéter ce bloc XML.
	 * Ils s'agit donc des tags qui ne sont pas correctement fermés ou correctement ouverts,
	 * sauf si ils sont sans effets sur l'arbre XML. Un tag qui est fermé puis ouvert au même niveau dans l'arbre XML,
	 * peut etre supprimé ou multiplié sans compromettre l'intégrité XML.
	 * @param content extrait de XML
	 * @return Pile des tagXML perturbant la repetition de l'extrait XML
	 */
	public static Queue<TagXML> extractUnrepeatableTag(final char[] content) {
		final Deque<TagXML> openedTagStack = extractUnbalancedTag(content);

		//On retire les elements centraux symétrique : </b></c></l><l><c><r> : on retire les c et l
		TagXML tag;
		final Deque<TagXML> closeTagStack = new ArrayDeque<>();
		for (final Iterator<TagXML> it = openedTagStack.iterator(); it.hasNext();) {
			tag = it.next();
			if (!tag.isOpenTag()) {
				closeTagStack.addLast(tag);
				it.remove();
			} else {
				if (!closeTagStack.isEmpty() && tag.getName().equals(closeTagStack.getLast().getName())) {
					closeTagStack.pollLast();
					it.remove();
				} else {
					break;
				}
			}
		}
		while (!closeTagStack.isEmpty()) {
			openedTagStack.addFirst(closeTagStack.pollLast());
		}

		return openedTagStack;
	}

	/**
	 * @param content contenu XML
	 * @return la position du premier body de tag trouvé dans le XML passé en paramètre
	 */
	public static int getFirstBodyIndex(final String content) {
		if (content.charAt(0) != '<') {
			return 0;
		}

		int endTagIndex = content.indexOf('>', 0);
		Assertion.checkState(endTagIndex != -1, "Une balise est mal fermée (manque le >) : {0}", content);
		int beginTagIndex = content.indexOf('<', endTagIndex);

		while (beginTagIndex == endTagIndex + 1) {
			endTagIndex = content.indexOf('>', beginTagIndex);
			Assertion.checkState(endTagIndex != -1, "Une balise est mal fermée (manque le >) : {0}", content.substring(beginTagIndex));
			beginTagIndex = content.indexOf('<', endTagIndex);
		}

		if (endTagIndex + 1 == content.length()) {
			return -1;
		}

		return endTagIndex + 1;
	}

	/**
	 * @param content contenu XML
	 * @return la position de fin du dernier body de tag trouvé dans le XML passé en paramètre
	 */
	public static int getLastBodyEndIndex(final String content) {
		final int lastIndex = content.length() - 1;
		if (content.charAt(lastIndex) != '>') {
			return content.length();
		}

		int beginTagIndex = content.lastIndexOf('<', lastIndex);
		Assertion.checkState(beginTagIndex != -1, "Une balise est mal ouverte (manque le <) : {0}", content);
		int endTagIndex = content.lastIndexOf('>', beginTagIndex);

		while (beginTagIndex == endTagIndex + 1 && endTagIndex != -1) {
			beginTagIndex = content.lastIndexOf('<', endTagIndex);
			Assertion.checkState(beginTagIndex != -1, "Une balise est mal ouverte (manque le <) : {0}", content);
			endTagIndex = content.lastIndexOf('>', beginTagIndex);
		}

		if (beginTagIndex == 0) {
			return -1;
		}

		return beginTagIndex;
	}

	public static StringBuilder parseXMLContent(final String xmlContent, final String tag, final ParserXMLHandler handler) {
		final StringBuilder cleanContent = new StringBuilder(xmlContent.length());

		final Pattern noBodyPattern = Pattern.compile("<" + tag + "[^>]*/>");
		final Pattern bodyPattern = Pattern.compile("<" + tag + "[^>]*[^/]>(.*?)</" + tag + ">");
		final Matcher noBodyMatcher = noBodyPattern.matcher(xmlContent);
		final Matcher bodyMatcher = bodyPattern.matcher(xmlContent);
		int index = 0;
		int indexNoBodyMatch = nextMatch(noBodyMatcher);
		int indexBodyMatch = nextMatch(bodyMatcher);

		while (index < xmlContent.length()) {
			//Assertion.invariant(indexNoBodyMatch == indexBodyMatch && indexBodyMatch != -1, "Cas non prévu : {0} == {1}", bodyMatcher.group(), noBodyMatcher.group());
			//System.out.println("noBody length:" + (indexNoBodyMatch > 0 ? noBodyMatcher.end() - noBodyMatcher.start() : ""));
			//System.out.println("body length:" + (indexBodyMatch > 0 ? bodyMatcher.end() - bodyMatcher.start() : ""));

			if (indexNoBodyMatch == -1 && indexBodyMatch == -1) {
				cleanContent.append(xmlContent, index, xmlContent.length());
				index = xmlContent.length();
			} else if (indexNoBodyMatch >= 0 && (indexNoBodyMatch < indexBodyMatch || indexBodyMatch == -1)) {
				cleanContent.append(xmlContent, index, indexNoBodyMatch);
				index = noBodyMatcher.end();
				handler.onNoBodyEndTag(noBodyMatcher.group(), cleanContent);
				indexNoBodyMatch = nextMatch(noBodyMatcher);
			} else if (indexBodyMatch >= 0 && (indexBodyMatch < indexNoBodyMatch || indexNoBodyMatch == -1)) {
				cleanContent.append(xmlContent, index, indexBodyMatch);
				index = bodyMatcher.end();
				handler.onBodyEndTag(bodyMatcher.group(), bodyMatcher.group(1), cleanContent);
				indexBodyMatch = nextMatch(bodyMatcher);
			} else {
				throw new IllegalStateException("Cas non prévu : " + bodyMatcher.group() + "==" + noBodyMatcher.group());
			}
		}
		return cleanContent;
	}

	private static int nextMatch(final Matcher matcher) {
		return matcher.find() ? matcher.start() : -1;
	}

}
