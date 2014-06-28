package io.vertigo.quarto.plugins.publisher.odt;

import io.vertigo.quarto.publisher.impl.merger.processor.MergerProcessor;
import io.vertigo.quarto.publisher.model.PublisherData;

/**
 * Cleaner de xml de fichier ODT.
 * Ce processor effectue plusieurs op�rations de rectification du XML d'un fichier ODT.
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
		 * Malgr� le preprocessor qui replace les balises, on laisse le cleaner car il peut etre n�cessaire pour ceux n'utilisant que les balises <% et pas <#
		 */
		// 1. Nettoyage du XML en fermant les balises
		xmlOutput = ODTCleanerUtil.clean(xmlInput);

		// 2. Suppression des balises de script
		xmlOutput = ODTTagRemoverUtil.removeTag(xmlOutput, SCRIPT_TAG, false);

		// On peut retirer les balises text-input, car les \n sont encod�s en <text:line-break/>
		// il r�agit tr�s mal avec la justification totale, mais de la m�me fa�on que la balise text-input
		xmlOutput = ODTTagRemoverUtil.removeTag(xmlOutput, INPUT_TAG, true);

		return xmlOutput;
	}
}
