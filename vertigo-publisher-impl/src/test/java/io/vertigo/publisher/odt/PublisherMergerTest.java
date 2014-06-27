package io.vertigo.publisher.odt;

import io.vertigo.publisher.AbstractPublisherMergerTest;

/**
 * Test de l'impl�mentation standard.
 * 
 * @author npiedeloup
 * @version $Id: PublisherMergerTest.java,v 1.1 2013/07/11 13:25:42 npiedeloup Exp $
 */
public final class PublisherMergerTest extends AbstractPublisherMergerTest {

	/** {@inheritDoc} */
	@Override
	protected final String getExtension() {
		return "odt";
	}
}
