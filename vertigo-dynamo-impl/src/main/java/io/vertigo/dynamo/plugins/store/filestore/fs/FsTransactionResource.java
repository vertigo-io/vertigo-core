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
package io.vertigo.dynamo.plugins.store.filestore.fs;

import io.vertigo.dynamo.transaction.VTransactionResource;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Classe de ressource, gérant la transaction des fichiers.
 *
 * @author skerdudou
 */
public final class FsTransactionResource implements VTransactionResource {

	private static final Logger LOG = Logger.getLogger(FsTransactionResource.class.getName());
	private final List<FileAction> fileActions = new ArrayList<>();

	/** {@inheritDoc} */
	@Override
	public void commit() throws Exception {
		Exception firstException = null;
		// on effectue les actions, on essaie d'en faire le maximum quelque soit les erreurs
		for (final FileAction fileAction : fileActions) {
			try {
				fileAction.process();
			} catch (final Exception e) {
				LOG.fatal(e);
				if (firstException == null) {
					firstException = e;
				}
			}
		}

		// on retourne a l'utilisateur la première exception levée
		if (firstException != null) {
			throw firstException;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void rollback() {
		// RAF
	}

	/** {@inheritDoc} */
	@Override
	public void release() {
		for (final FileAction fileAction : fileActions) {
			fileAction.clean();
		}
		fileActions.clear();
	}

	/**
	 * Sauvegarde du fichier au commit.
	 *
	 * @param inputStream l'inputStream du fichier
	 * @param path le chemin de destination du fichier
	 */
	void saveFile(final InputStream inputStream, final String path) {
		fileActions.add(new FileActionSave(inputStream, path));
	}

	/**
	 * Suppression du fichier au commit. Si on avait des insertions mémorisées sur ce fichier (cas uniquement pour TNR),
	 * on les retire et on ne met pas la suppression dans la liste des opérations à faire.
	 *
	 * @param path le chemin de destination du fichier
	 */
	void deleteFile(final String path) {
		final File file = new File(path);
		final String absPath = file.getAbsolutePath();
		boolean found = false;
		for (int i = fileActions.size() - 1; i >= 0; i--) {
			final FileAction act = fileActions.get(i);
			if (act instanceof FileActionSave && absPath.equals(act.getAbsolutePath())) {
				found = true;
				act.clean();
				fileActions.remove(i);
			}
		}
		if (!found) {
			fileActions.add(new FileActionDelete(path));
		}
	}
}
