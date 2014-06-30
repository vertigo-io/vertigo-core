package io.vertigo.quarto.converter.remote;

import io.vertigo.quarto.converter.AbstractConverterManagerTest;

/**
 * Test de l'impl�mentation avec le plugin OpenOfficeRemoteConverterPlugin.
 * 
 * @author npiedeloup
 * @version $Id: ConverterManagerRemoteTest.java,v 1.2 2014/06/26 12:30:48 npiedeloup Exp $
 */
public final class ConverterManagerRemoteTest extends AbstractConverterManagerTest {
	/** {@inheritDoc} */
	@Override
	protected String[] getManagersXmlFileName() {
		return new String[] { "./managers-test-remote.xml" };
	}
}
