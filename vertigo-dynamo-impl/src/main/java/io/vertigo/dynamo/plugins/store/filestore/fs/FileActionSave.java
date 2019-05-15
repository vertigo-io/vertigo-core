/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, vertigo-io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.plugins.store.filestore.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.commons.transaction.VTransactionAfterCompletionFunction;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;
import io.vertigo.lang.WrappedException;

/**
 * Classe de gestion de la sauvegarde d'un fichier.
 *
 * @author skerdudou
 */
final class FileActionSave implements VTransactionAfterCompletionFunction {
	private static final String EXT_NEW = "toSave";
	private static final char EXT_SEPARATOR = '.';
	private static final Logger LOG = LogManager.getLogger(FileActionSave.class.getName());

	//ref the file before this save action
	private final File txPrevFile;
	//ref the new file to save on this transaction
	private final File txNewFile;

	/**
	 * Constructor.
	 *
	 * @param inputStream l'inputStream du fichier
	 * @param path le chemin de destination du fichier
	 */
	public FileActionSave(final InputStream inputStream, final String path) {
		Assertion.checkNotNull(inputStream);
		Assertion.checkNotNull(path);
		//-----
		txPrevFile = new File(path);
		txNewFile = new File(path + EXT_SEPARATOR + System.currentTimeMillis() + EXT_SEPARATOR + EXT_NEW);

		// création du fichier temporaire
		if (!txNewFile.getParentFile().exists() && !txNewFile.getParentFile().mkdirs()) {
			LOG.error("Can't create temp directories {}", txNewFile.getAbsolutePath());
			throw new VSystemException("Can't create temp directories");
		}
		try {
			if (!txNewFile.createNewFile()) {
				LOG.error("Can't create temp file {}", txNewFile.getAbsolutePath());
				throw new VSystemException("Can't create temp file.");
			}
		} catch (final IOException e) {
			LOG.error("Can't save temp file {}", txNewFile.getAbsolutePath());
			throw WrappedException.wrap(e, "Can't save temp file.");
		}

		// copie des données dans le fichier temporaire. Permet de vérifier l'espace disque avant d'arriver à la phase
		// de commit. Si la phase de commit a une erreur, garde trace du fichier sur le FS.
		try {
			FileUtil.copy(inputStream, txNewFile);
		} catch (final IOException e) {
			LOG.error("Can't copy uploaded file to : {}", txNewFile.getAbsolutePath());
			throw WrappedException.wrap(e, "Can't save uploaded file.");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void afterCompletion(final boolean txCommited) {
		if (txCommited) {
			doCommit();
		} else {
			doRollback();
		}
	}

	private void doCommit() {
		// on supprime l'ancien fichier s'il existe
		if (txPrevFile.exists() && !txPrevFile.delete()) {
			LOG.fatal("Impossible supprimer l'ancien fichier ({}) lors de la sauvegarde. Le fichier a sauvegarder se trouve dans {}", txPrevFile.getAbsolutePath(), txNewFile.getAbsolutePath());
			throw new VSystemException("Erreur fatale : Impossible de sauvegarder le fichier.");
		}

		// on met le fichier au bon emplacement
		if (!txNewFile.renameTo(txPrevFile)) {
			LOG.fatal("Impossible sauvegarder le fichier. Déplacement impossible de {} vers {}", txNewFile.getAbsolutePath(), txPrevFile.getAbsolutePath());
			throw new VSystemException("Erreur fatale : Impossible de sauvegarder le fichier.");
		}
	}

	private void doRollback() {
		// on ne fait pas de ménage si on a eu une erreur
		if (txNewFile.exists()) {
			if (!txNewFile.delete()) {
				LOG.error("Can't rollback and delete file : {}", txNewFile.getAbsolutePath());
			}
		}
	}
}
