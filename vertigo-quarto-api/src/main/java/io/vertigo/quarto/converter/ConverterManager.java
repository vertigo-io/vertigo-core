package io.vertigo.quarto.converter;

import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.component.Manager;

/**
 * Gestionnaire centralis� des conversions de documents.
 * 
 * Exemple : 
 *  - doc-->pdf 
 *  - odt-->doc
 * 
 * @author pchretien, npiedeloup
 * @version $Id: ConverterManager.java,v 1.4 2014/01/28 18:49:24 pchretien Exp $
 */
public interface ConverterManager extends Manager {
	/**
	 * Conversion d'un document � un format cible.
	 * 
	 * @param inputFile Document source � convertir
	 * @param format Format du document � cible
	 * @return Document converti au format pass� en param�tre.
	 */
	KFile convert(KFile inputFile, String format);

	/**
	 * Conversion asynchrone d'un document � un format cible.
	 * 
	 * @param inputFile Document source � convertir
	 * @param format Format du document � cible
	 * param Handler de r�sultat sur l'ex�cution de la tache de conversion
	 */
	void convertASync(final KFile inputFile, final String format, final WorkResultHandler<KFile> workResultHandler);
}
