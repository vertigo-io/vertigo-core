package io.vertigo.quarto.plugins.publisher.odt;

import io.vertigo.quarto.publisher.impl.merger.processor.MergerProcessor;
import io.vertigo.quarto.publisher.model.PublisherData;

/**
 * Inversion des textInput dans le fichier ODT.
 * @author npiedeloup
 * @version $Id: ODTReverseInputProcessor.java,v 1.1 2013/07/11 13:24:48 npiedeloup Exp $
 */
final class ODTReverseInputProcessor implements MergerProcessor {

	/** {@inheritDoc} */
	public String execute(final String xmlInput, final PublisherData publisherData) {
		String xmlOutput;

		// 1. Inversion des textInput
		xmlOutput = ODTInputTagReverserUtil.reverseInputTag(xmlInput);

		return xmlOutput;
	}
}
