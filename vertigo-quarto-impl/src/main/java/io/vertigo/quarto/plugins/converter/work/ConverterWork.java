package io.vertigo.quarto.plugins.converter.work;

import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.kernel.lang.Assertion;

/**
 * Travail de conversion.
 * Poss�de :
 * - le fichier � convertir
 * - le format destination
 * 
 * @author npiedeloup
 * @version $Id: ConverterWork.java,v 1.5 2014/01/28 18:49:24 pchretien Exp $
 */
final class ConverterWork {
	private final KFileSerializable file;
	private final String targetFormat;

	/**
	 * Constructeur.
	 * @param file fichier � convertir
	 * @param targetFormat format destination
	 */
	ConverterWork(final KFile file, final String targetFormat) {
		Assertion.checkNotNull(file);
		Assertion.checkNotNull(targetFormat);
		//-----------------------------------------------------------------
		this.file = new KFileSerializable(file);
		this.targetFormat = targetFormat;
	}

	/** {@inheritDoc} */
	public String getName() {
		return this.getClass().getSimpleName();
	}

	/**
	 * @return fichier � convertir
	 */
	KFile getInputFile() {
		return file;
	}

	/**
	 * @return format destination
	 */
	String geTargetFormat() {
		return targetFormat;
	}
}