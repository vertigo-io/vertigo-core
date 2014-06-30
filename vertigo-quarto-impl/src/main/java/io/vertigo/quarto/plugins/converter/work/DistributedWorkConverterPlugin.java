package io.vertigo.quarto.plugins.converter.work;

import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.quarto.impl.converter.ConverterPlugin;

import javax.inject.Inject;

/**
 * Plugin de Conversion des fichiers par un work (pour la distribution).
 * 
 * @author npiedeloup
 * @version $Id: DistributedWorkConverterPlugin.java,v 1.6 2014/01/28 18:49:24 pchretien Exp $
 */
public final class DistributedWorkConverterPlugin implements ConverterPlugin {
	private final WorkManager workManager;

	/**
	 * Constructeur.
	 * @param workManager Manager des works
	 */
	@Inject
	public DistributedWorkConverterPlugin(final WorkManager workManager) {
		Assertion.checkNotNull(workManager);
		//---------------------------------------------------------------------
		this.workManager = workManager;
	}

	/** {@inheritDoc} */
	public KFile convertToFormat(final KFile file, final String targetFormat) {
		return workManager.process(new ConverterWork(file, targetFormat), new WorkEngineProvider<>(ConverterWorkEngine.class));
	}
}
