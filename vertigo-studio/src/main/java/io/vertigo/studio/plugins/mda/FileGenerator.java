package io.vertigo.studio.plugins.mda;

import io.vertigo.studio.mda.Result;

/**
 * Interface des generateurs de fichier.
 * 
 * @author dchallas
 * @version $Id: FileGenerator.java,v 1.1 2013/07/11 10:04:04 npiedeloup Exp $
 */
public interface FileGenerator {
	/**
	 * Génèration d'un fichier.
	 * La génération n'est effectique que si
	 * - le fichier n'existe pas déjà 
	 * ou bien
	 * - le fichier existe déjà et si le fichier peut être regénéré 
	 * 
	 * @param override Si le fichier peut être regénéré 
	 * @param result Résultat de la génération (statistiques)
	 */
	void generateFile(final Result result, final boolean override);
}
