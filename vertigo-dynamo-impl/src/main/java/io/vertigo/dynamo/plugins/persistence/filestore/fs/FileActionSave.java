/**
 * 
 */
package io.vertigo.dynamo.plugins.persistence.filestore.fs;

import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * Classe de gestion de la sauvegarde d'un fichier.
 * 
 * @author skerdudou
 */
final class FileActionSave implements FileAction {
	private static final String EXT_NEW = "toSave";
	private static final char EXT_SEPARATOR = '.';
	private static final Logger LOG = Logger.getLogger(FileActionSave.class.getName());

	private State state;
	private final File file;
	private final File newFile;

	/**
	 * Constructeur.
	 * 
	 * @param inputStream l'inputStream du fichier
	 * @param path le chemin de destination du fichier
	 */
	public FileActionSave(final InputStream inputStream, final String path) {
		Assertion.checkNotNull(inputStream);
		Assertion.checkNotNull(path);
		//---------------------------------------------------------------------
		file = new File(path);
		newFile = new File(path + EXT_SEPARATOR + new Date().getTime() + EXT_SEPARATOR + EXT_NEW);

		// création du fichier temporaire
		newFile.getParentFile().mkdirs();
		try {
			if (!newFile.createNewFile()) {
				LOG.error("Impossible de créer le fichier temporaire " + newFile.getAbsolutePath());
				throw new VRuntimeException("Impossible d'ajouter le fichier.");
			}
		} catch (final IOException e) {
			LOG.error("Impossible de créer le fichier temporaire " + newFile.getAbsolutePath());
			throw new VRuntimeException("Impossible d'ajouter le fichier.", e);
		}

		// copie des données dans le fichier temporaire. Permet de vérifier l'espace disque avant d'arriver à la phase
		// de commit. Si la phase de commit a une erreur, garde trace du fichier sur le FS.
		try {
			FileUtil.copy(inputStream, newFile);
		} catch (final IOException e) {
			LOG.error("Impossible de copier les données du fichier uploadé dans : " + newFile.getAbsolutePath());
			throw new VRuntimeException("Impossible d'enregistrer les données du fichier.", e);
		}

		state = State.READY;
	}

	/** {@inheritDoc} */
	public void process() throws Exception {
		Assertion.checkArgument(State.READY.equals(state), "Le fichier n'est pas dans l'état requis 'READY' pour effectuer l'action. Etat actuel : '{0}'", state);

		// on supprime l'ancien fichier s'il existe
		if (file.exists() && !file.delete()) {
			LOG.fatal("Impossible supprimer l'ancien fichier (" + file.getAbsolutePath() + ") lors de la sauvegarde. Le fichier a sauvegarder se trouve dans " + newFile.getAbsolutePath());
			state = State.ERROR;
			throw new VRuntimeException("Erreur fatale : Impossible de sauvegarder le fichier.");
		}

		// on met le fichier au bon emplacement
		if (!newFile.renameTo(file)) {
			LOG.fatal("Impossible sauvegarder le fichier. Déplacement impossible de " + newFile.getAbsolutePath() + " vers " + file.getAbsolutePath());
			state = State.ERROR;
			throw new VRuntimeException("Erreur fatale : Impossible de sauvegarder le fichier.");
		}

		state = State.PROCESSED;
	}

	/** {@inheritDoc} */
	public void clean() {
		// on ne fait pas de ménage si on a eu une erreur
		if (!State.ERROR.equals(state) && newFile.exists()) {
			newFile.delete();
		}

		state = State.END;
	}

	/** {@inheritDoc} */
	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}

}
