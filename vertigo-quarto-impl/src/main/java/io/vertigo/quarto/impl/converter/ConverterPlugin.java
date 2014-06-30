package io.vertigo.quarto.impl.converter;

import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.kernel.component.Plugin;

/**
 * Plugin de Conversion des fichiers.
 * 
 * @author npiedeloup
 * @version $Id: ConverterPlugin.java,v 1.3 2014/01/28 18:49:24 pchretien Exp $
 */
public interface ConverterPlugin extends Plugin {
	/**
	 * Retourne le fichier converti
	 * L'appel � l'OOO distant est synchroniz�, car il supporte mal les converssions concurrentes.
	 * @param file Fichier � convertir
	 * @param targetFormat Type de document de sortie ODT,RTF,DOC,CSV,PDF
	 * @return Fichier converti
	 */
	KFile convertToFormat(final KFile file, final String targetFormat);

}
