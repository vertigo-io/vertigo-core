/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package io.vertigo.dynamo.plugins.persistence.filestore.fs;

import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.lang.Assertion;

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
		//-----
		file = new File(path);
		newFile = new File(path + EXT_SEPARATOR + new Date().getTime() + EXT_SEPARATOR + EXT_NEW);

		// création du fichier temporaire
		if (!newFile.getParentFile().exists() && !newFile.getParentFile().mkdirs()) {
			LOG.error("Can't create temp directories " + newFile.getAbsolutePath());
			throw new RuntimeException("Can't create temp directories");
		}
		try {
			if (!newFile.createNewFile()) {
				LOG.error("Can't create temp file " + newFile.getAbsolutePath());
				throw new RuntimeException("Can't create temp file.");
			}
		} catch (final IOException e) {
			LOG.error("Can't save temp file " + newFile.getAbsolutePath());
			throw new RuntimeException("Can't save temp file.", e);
		}

		// copie des données dans le fichier temporaire. Permet de vérifier l'espace disque avant d'arriver à la phase
		// de commit. Si la phase de commit a une erreur, garde trace du fichier sur le FS.
		try {
			FileUtil.copy(inputStream, newFile);
		} catch (final IOException e) {
			LOG.error("Can't copy uploaded file to : " + newFile.getAbsolutePath());
			throw new RuntimeException("Can't save uploaded file.", e);
		}

		state = State.READY;
	}

	/** {@inheritDoc} */
	@Override
	public void process() throws Exception {
		Assertion.checkArgument(State.READY.equals(state), "Le fichier n'est pas dans l'état requis 'READY' pour effectuer l'action. Etat actuel : '{0}'", state);

		// on supprime l'ancien fichier s'il existe
		if (file.exists() && !file.delete()) {
			LOG.fatal("Impossible supprimer l'ancien fichier (" + file.getAbsolutePath() + ") lors de la sauvegarde. Le fichier a sauvegarder se trouve dans " + newFile.getAbsolutePath());
			state = State.ERROR;
			throw new RuntimeException("Erreur fatale : Impossible de sauvegarder le fichier.");
		}

		// on met le fichier au bon emplacement
		if (!newFile.renameTo(file)) {
			LOG.fatal("Impossible sauvegarder le fichier. Déplacement impossible de " + newFile.getAbsolutePath() + " vers " + file.getAbsolutePath());
			state = State.ERROR;
			throw new RuntimeException("Erreur fatale : Impossible de sauvegarder le fichier.");
		}

		state = State.PROCESSED;
	}

	/** {@inheritDoc} */
	@Override
	public void clean() {
		// on ne fait pas de ménage si on a eu une erreur
		if (!State.ERROR.equals(state) && newFile.exists()) {
			newFile.delete();
		}

		state = State.END;
	}

	/** {@inheritDoc} */
	@Override
	public String getAbsolutePath() {
		return file.getAbsolutePath();
	}

}
