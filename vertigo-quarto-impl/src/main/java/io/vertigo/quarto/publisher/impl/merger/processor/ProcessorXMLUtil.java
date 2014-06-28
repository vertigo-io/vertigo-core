package io.vertigo.quarto.publisher.impl.merger.processor;

import io.vertigo.kernel.lang.Assertion;

import java.util.Iterator;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe utilitaire pour une arborescence XML.
 *
 * @author npiedeloup
 * @version $Id: ProcessorXMLUtil.java,v 1.4 2014/02/27 10:32:26 pchretien Exp $
 */
public final class ProcessorXMLUtil {
	/**
	 * Constructeur priv� pour classe utilitaire
	 */
	private ProcessorXMLUtil() {
		//RAS
	}

	/**
	 * Retourne la pile des Tags pr�sents dans le xml pass� en parametre.
	 * Cette pile retourne les balises completes (ie: avec les attributs)
	 * @param content contenu XML
	 * @return pile des tag XML
	 */
	private static Stack<TagXML> extractTagStack(final char[] content) {
		// en jdk 1.6, il est un peu plus performant (synchronizeds) de faire :
		// Deque<TagXML> pileTag = new ArrayDeque<TagXML>();
		final Stack<TagXML> pileTag = new Stack<>();

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
					pileTag.push(new TagXML(currentBeginTag.toString(), currentBeginTagIndex));
				}
				beginTagEnCours = false;
				currentBeginTag.setLength(0);
			}

			if (endTagEnCours && current == '>') {
				currentEndTag.append('>');
				pileTag.push(new TagXML(currentEndTag.toString(), currentEndTagIndex));
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
	 * ferm�s correctement dans le XML pass� en parametre.
	 * @param content Extrait d'un XML correct.
	 * @return Pile des tags mal ouverts ou mal ferm�s
	 */
	static Stack<TagXML> extractUnbalancedTag(final char[] content) {
		// en jdk 1.6, il est un peu plus performant (synchronizeds) de faire :
		// Deque<String> pileTag = new ArrayDeque<String>();
		final Stack<TagXML> fullTagStack = extractTagStack(content);
		final Stack<TagXML> openedTagStack = new Stack<>();
		String tagName;
		for (final TagXML tag : fullTagStack) {
			if (!tag.isOpenTag()) {
				tagName = tag.getName();
				if (!openedTagStack.isEmpty() && tagName.equals(openedTagStack.lastElement().getName()) && openedTagStack.lastElement().isOpenTag()) {
					openedTagStack.pop();
				} else {
					openedTagStack.push(tag);
				}
			} else if (tag.hasBody()) {
				openedTagStack.push(tag);
			}
		}
		return openedTagStack;
	}

	/**
	 * Retourne les tags qui posent probl�me si l'on cherche � supprimer ou r�peter ce bloc XML.
	 * Ils s'agit donc des tags qui ne sont pas correctement ferm�s ou correctement ouverts,
	 * sauf si ils sont sans effets sur l'arbre XML. Un tag qui est ferm� puis ouvert au m�me niveau dans l'arbre XML,
	 * peut etre supprim� ou multipli� sans compromettre l'int�grit� XML.
	 * @param content extrait de XML
	 * @return Pile des tagXML perturbant la repetition de l'extrait XML
	 */
	public static Stack<TagXML> extractUnrepeatableTag(final char[] content) {
		final Stack<TagXML> openedTagStack = extractUnbalancedTag(content);

		//On retire les elements centraux sym�trique : </b></c></l><l><c><r> : on retire les c et l
		TagXML tag;
		final Stack<TagXML> closeTagStack = new Stack<>();
		for (final Iterator<TagXML> it = openedTagStack.iterator(); it.hasNext();) {
			tag = it.next();
			if (!tag.isOpenTag()) {
				closeTagStack.push(tag);
				it.remove();
			} else {
				if (!closeTagStack.isEmpty() && tag.getName().equals(closeTagStack.lastElement().getName())) {
					closeTagStack.pop();
					it.remove();
				} else {
					break;
				}
			}
		}
		while (!closeTagStack.isEmpty()) {
			openedTagStack.add(0, closeTagStack.pop());
		}

		return openedTagStack;
	}

	/**
	 * @param content contenu XML
	 * @return la position du premier body de tag trouv� dans le XML pass� en param�tre
	 */
	public static int getFirstBodyIndex(final String content) {
		if (content.charAt(0) != '<') {
			return 0;
		}

		int endTagIndex = content.indexOf('>', 0);
		Assertion.checkState(endTagIndex != -1, "Une balise est mal ferm�e (manque le >) : {0}", content);
		int beginTagIndex = content.indexOf('<', endTagIndex);

		while (beginTagIndex == endTagIndex + 1) {
			endTagIndex = content.indexOf('>', beginTagIndex);
			Assertion.checkState(endTagIndex != -1, "Une balise est mal ferm�e (manque le >) : {0}", content.substring(beginTagIndex));
			beginTagIndex = content.indexOf('<', endTagIndex);
		}

		if (endTagIndex + 1 == content.length()) {
			return -1;
		}

		return endTagIndex + 1;
	}

	/**
	 * @param content contenu XML
	 * @return la position de fin du dernier body de tag trouv� dans le XML pass� en param�tre
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
			//Assertion.invariant(indexNoBodyMatch == indexBodyMatch && indexBodyMatch != -1, "Cas non pr�vu : {0} == {1}", bodyMatcher.group(), noBodyMatcher.group());
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
				throw new IllegalStateException("Cas non pr�vu : " + bodyMatcher.group() + "==" + noBodyMatcher.group());
			}
		}
		return cleanContent;
	}

	private static int nextMatch(final Matcher matcher) {
		return matcher.find() ? matcher.start() : -1;
	}

}
