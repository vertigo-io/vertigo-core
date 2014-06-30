package io.vertigo.quarto.impl.converter;

import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.quarto.converter.ConverterManager;

import java.util.concurrent.Callable;

import javax.inject.Inject;

/**
 * Impl�mentation standard du manager des conversions de documents.
 * 
 * Conversions accept�s entre formats : 
 *  - ODT 
 *  - DOC 
 *  - RTF 
 *  - CSV 
 *  - PDF 
 * 
 * @author pchretien, npiedeloup
 * @version $Id: ConverterManagerImpl.java,v 1.5 2014/01/28 18:49:24 pchretien Exp $
 */
public final class ConverterManagerImpl implements ConverterManager {
	private final WorkManager workManager;
	private final ConverterPlugin converterPlugin;

	/**
	 * Constructeur.
	 * @param workManager Manager des works
	 */
	@Inject
	public ConverterManagerImpl(final WorkManager workManager, final ConverterPlugin converterPlugin) {
		// La connexion au serveur openOffice est instanci�e lors du start
		Assertion.checkNotNull(workManager);
		Assertion.checkNotNull(converterPlugin);
		//---------------------------------------------------------------------
		this.workManager = workManager;
		this.converterPlugin = converterPlugin;
	}

	/** {@inheritDoc} */
	public void convertASync(final KFile inputFile, final String format, final WorkResultHandler<KFile> workResultHandler) {
		Assertion.checkNotNull(inputFile);
		Assertion.checkArgNotEmpty(format);
		// ---------------------------------------------------------------------
		workManager.async(new Callable<KFile>() {
			public KFile call() {
				return convert(inputFile, format);
			}
		}, workResultHandler);
	}

	/** {@inheritDoc} */
	public KFile convert(final KFile inputFile, final String format) {
		Assertion.checkNotNull(inputFile);
		Assertion.checkArgNotEmpty(format);
		// ---------------------------------------------------------------------
		return converterPlugin.convertToFormat(inputFile, format);
	}
}
