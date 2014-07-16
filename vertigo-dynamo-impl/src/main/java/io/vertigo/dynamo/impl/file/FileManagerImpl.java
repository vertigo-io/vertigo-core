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
package io.vertigo.dynamo.impl.file;

import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.InputStreamBuilder;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.dynamo.file.util.TempFile;
import io.vertigo.dynamo.impl.file.model.FSFile;
import io.vertigo.dynamo.impl.file.model.StreamFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.activation.MimetypesFileTypeMap;

/**
* Implémentation du gestionnaire de la définition des fichiers.
* 
* @author pchretien
*/
public final class FileManagerImpl implements FileManager {
	/** {@inheritDoc} */
	public File obtainReadOnlyFile(final KFile file) {
		return doObtainReadOnlyFile(file);
	}

	/** {@inheritDoc} */
	public KFile createFile(final String fileName, final String typeMime, final File file) {
		return new FSFile(fileName, typeMime, file);
	}

	/** {@inheritDoc} */
	public KFile createFile(final File file) {
		return new FSFile(file.getName(), new MimetypesFileTypeMap().getContentType(file), file);
	}

	/** {@inheritDoc} */
	public KFile createFile(final String fileName, final Date lastModified, final long length, final InputStreamBuilder inputStreamBuilder) {
		return createFile(fileName, new MimetypesFileTypeMap().getContentType(fileName), lastModified, length, inputStreamBuilder);
	}

	/** {@inheritDoc} */
	public KFile createFile(final String fileName, final String mimeType, final Date lastModified, final long length, final InputStreamBuilder inputStreamBuilder) {
		return new StreamFile(fileName, mimeType, lastModified, length, inputStreamBuilder);
	}

	/**
	 * Crée un fichier temporaire à partir d'un fileInfo.
	 * Attention le processus appelant doit s'assurer de la suppression de ce fichier temporaire.
	 * @param fileInfo FileInfo à utiliser
	 * @return Fichier temporaire.
	 */
	private static File createTempFile(final KFile fileInfo) {
		// TODO voir a ajouter une WeakRef sur FileInfo pour vérifier la suppression des fichiers temp après usage
		try {
			return doCreateTempFile(fileInfo);
		} catch (final IOException e) {
			throw new RuntimeException("Impossible de créer un fichier temporaire pour le FileInfo " + fileInfo.getFileName(), e);
		}
	}

	private static File doCreateTempFile(final KFile fileInfo) throws IOException {
		final File tmpFile = new TempFile("fileInfo", '.' + FileUtil.getFileExtension(fileInfo.getFileName()));
		try (final InputStream inputStream = fileInfo.createInputStream()) {
			FileUtil.copy(inputStream, tmpFile);
			return tmpFile;
		}
	}

	/**
	 * @param fileInfo FileInfo à lire
	 * @return Fichier physique readOnly (pour lecture d'un FileInfo)
	 */
	private static File doObtainReadOnlyFile(final KFile fileInfo) {
		final File inputFile;
		if (fileInfo instanceof FSFile) {
			inputFile = ((FSFile) fileInfo).getFile();
		} else {
			inputFile = createTempFile(fileInfo);
		}
		return inputFile;
	}
}
