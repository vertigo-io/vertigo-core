package io.vertigo.studio.impl.mda;

import io.vertigo.kernel.util.StringUtil;
import io.vertigo.studio.mda.Result;

import java.io.File;
import java.io.PrintStream;

import org.apache.log4j.Logger;

/**
 * Résultat de la génération.
 *
 * @author pchretien
 */
final class ResultImpl implements Result {
	private final Logger logger = Logger.getLogger(getClass());

	/** Nombre de fichiers écrits . */
	private int writtenFiles;
	/** Nombre de fichiers en erreurs. */
	private int errorFiles;
	/** Nombre de fichiers identiques. */
	private int identicalFiles;

	//	/** Liste des fichiers en erreur. */
	//	private final List<File> fileErrorList = new ArrayList<File>();

	private final long start = System.currentTimeMillis();

	/** {@inheritDoc} */
	public void displayResultMessage(final PrintStream out) {
		final long duration = System.currentTimeMillis() - start;
		out.append(StringUtil.format("\nGénération de {0} fichiers, {1} fichiers identiques et {2} problemes en {3} ms", writtenFiles, identicalFiles, errorFiles, duration));
	}

	/** {@inheritDoc} */
	public void addFileWritten(final File file, final boolean success) {
		if (success) {
			writtenFiles++;
			logger.trace("Fichier généré : " + file.getAbsolutePath());
		} else {
			errorFiles++;
			//Ajout d'un fichier en erreur.
			logger.trace("Fichier en erreur : " + file.getAbsolutePath());
		}
	}

	/** {@inheritDoc} */
	public void addIdenticalFile(final File file) {
		identicalFiles++;
		logger.trace("Fichier identique : " + file.getAbsolutePath());
	}
}
