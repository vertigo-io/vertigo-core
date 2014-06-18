/**
 * 
 */
package io.vertigo.dynamo.plugins.persistence.filestore.fs;

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.io.File;

import org.apache.log4j.Logger;

/**
 * Classe de gestion de la sauvegarde d'un fichier.
 * 
 * @author skerdudou
 */
final class FileActionDelete implements FileAction {
	private static final Logger LOG = Logger.getLogger(FileActionDelete.class.getName());

	private State state;
	private final File file;

	/**
	 * Constructeur.
	 * 
	 * @param path le chemin de destination du fichier
	 */
	FileActionDelete(final String path) {
		Assertion.checkNotNull(path);
		//---------------------------------------------------------------------
		file = new File(path);

		if (!file.exists()) {
			LOG.error("Impossible de trouver le fichier pour suppression : " + file.getAbsolutePath());
			throw new VRuntimeException("Impossible de trouver le fichier à supprimer.");
		}
		if (!file.canWrite()) {
			LOG.error("Impossible de supprimer le fichier : " + file.getAbsolutePath());
			throw new VRuntimeException("Impossible de supprimer le fichier.");
		}

		state = State.READY;
	}

	/** {@inheritDoc} */
	public void process() throws Exception {
		Assertion.checkArgument(State.READY.equals(state), "Le fichier n'est pas dans l'état requis 'READY' pour effectuer l'action. Etat actuel : '{0}'", state);

		// on supprime le fichier
		if (!file.delete()) {
			LOG.fatal("Impossible de supprimer le fichier " + file.getAbsolutePath());
			state = State.ERROR;
			throw new VRuntimeException("Erreur fatale : Impossible de supprimer le fichier.");
		}

		state = State.PROCESSED;
	}

	/** {@inheritDoc} */
	public void clean() {
		state = State.END;
	}

	/** {@inheritDoc} */
	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}

}
