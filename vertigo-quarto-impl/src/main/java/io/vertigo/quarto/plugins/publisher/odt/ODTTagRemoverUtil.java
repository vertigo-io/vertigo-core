package io.vertigo.quarto.plugins.publisher.odt;

import io.vertigo.quarto.publisher.impl.merger.processor.ParserXMLHandler;
import io.vertigo.quarto.publisher.impl.merger.processor.ProcessorXMLUtil;

/**
 * Classe de nettoyage d'une arborescence XML.
 * Gestion de la suppression d'une balise.
 *
 * @author brenard
 * @version $Id: ODTTagRemoverUtil.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
final class ODTTagRemoverUtil {
	/**
	 * Constructeur priv� pour classe utilitaire
	 */
	private ODTTagRemoverUtil() {
		//RAS
	}

	/**
	 * Corrige le contenu qui est une arborescence XML pour supprimer la balise et son contenu.
	 *
	 * @param xmlContent Arborescence XML.
	 * @param tag Balise � supprimer.
	 * @return Arborescence corrig�e.
	 */
	public static String removeTag(final String xmlContent, final String tag) {
		return removeTag(xmlContent, tag, false);
	}

	/**
	 * Corrige le contenu qui est une arborescence XML pour supprimer la balise.
	 *
	 * @param xmlContent Arborescence XML.
	 * @param tag Balise � supprimer.
	 * @param keepBody Indique si le body doit etre conserv� lors de la suppression du tag
	 * @return Arborescence corrig�e.
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
