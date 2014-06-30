package io.vertigo.quarto.plugins.converter.work;

import io.vertigo.dynamo.work.WorkEngine;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.quarto.converter.ConverterManager;

import javax.inject.Inject;

/**
 * WorkEngine de conversion de document. 
 * Reentre sur le manager. 
 * Attention � utiliser un plugin qui effectue directement la conversion sans work, pour ne pas boucler. 
 * @author npiedeloup
 * @version $Id: ConverterWorkEngine.java,v 1.4 2014/01/20 18:56:52 pchretien Exp $
 */
public final class ConverterWorkEngine implements WorkEngine<KFileSerializable, ConverterWork> {
	private final ConverterManager converterManager;

	/**
	 * Constructeur.
	 * @param converterManager Manager de conversion
	 */
	@Inject
	public ConverterWorkEngine(final ConverterManager converterManager) {
		Assertion.checkNotNull(converterManager);
		//-----------------------------------------------------------------
		this.converterManager = converterManager;
	}

	/** {@inheritDoc} */
	public KFileSerializable process(final ConverterWork work) {
		return new KFileSerializable(converterManager.convert(work.getInputFile(), work.geTargetFormat()));
	}
}
