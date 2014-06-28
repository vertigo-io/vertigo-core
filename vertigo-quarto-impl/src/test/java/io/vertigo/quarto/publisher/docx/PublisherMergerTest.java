package io.vertigo.quarto.publisher.docx;

import io.vertigo.quarto.publisher.AbstractPublisherMergerTest;

/**
 * Test de l'impl�mentation DOCX.
 * 
 * @author npiedeloup
 * @version $Id: PublisherMergerTest.java,v 1.1 2013/07/11 13:25:42 npiedeloup Exp $
 */
public final class PublisherMergerTest extends AbstractPublisherMergerTest {
	/** {@inheritDoc} */
	@Override
	protected final String getExtension() {
		return "docx";
	}
}
