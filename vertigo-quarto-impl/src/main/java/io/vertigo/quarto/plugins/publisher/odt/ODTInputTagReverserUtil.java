package io.vertigo.quarto.plugins.publisher.odt;

import io.vertigo.quarto.publisher.impl.merger.processor.ParserXMLHandler;
import io.vertigo.quarto.publisher.impl.merger.processor.ProcessorXMLUtil;

/**
 * Classe de nettoyage d'une arborescence XML.
 * Gestion du cas ou il y a des balises ouvertes non ferm�es.
 *
 * @author brenard
 * @version $Id: ODTInputTagReverserUtil.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
final class ODTInputTagReverserUtil {
	private static final String INPUT_TAG = "text:text-input";
	private static final String DESCRIPTION_ATTRIBUTE = "text:description";

	/**
	 * Constructeur priv� pour classe utilitaire
	 */
	private ODTInputTagReverserUtil() {
		super();
	}

	/**
	 * Corrige le contenu qui est une arborescence XML pour inverser l'attribut
	 * text:description de la balise text:text-input avec son contenu.
	 *
	 * @param xmlContent Arborescence XML.
	 * @return Arborescence corrig�e.
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
			final StringBuilder newContent = new StringBuilder(tagToInverse.length());
			final int indexFinAttribut = tagToInverse.indexOf("\"", indexAttribut + DESCRIPTION_ATTRIBUTE.length() + 2);
			final String valueAttribut = tagToInverse.substring(indexAttribut + DESCRIPTION_ATTRIBUTE.length() + 2, indexFinAttribut);
			final int indexDebutBody = tagToInverse.indexOf(bodyContent, indexFinAttribut);

			newContent.append(tagToInverse.substring(0, indexAttribut));
			newContent.append(DESCRIPTION_ATTRIBUTE);
			newContent.append("=\"");
			newContent.append(bodyContent);
			newContent.append("\">");
			newContent.append(valueAttribut);
			newContent.append(tagToInverse.substring(indexDebutBody + bodyContent.length()));
			return newContent.toString();
		}
		return "";
	}
}
