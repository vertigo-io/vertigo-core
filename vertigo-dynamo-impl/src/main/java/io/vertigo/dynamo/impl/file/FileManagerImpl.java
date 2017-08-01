/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.Optional;

import javax.activation.MimetypesFileTypeMap;
import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.commons.daemon.DaemonScheduled;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.InputStreamBuilder;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.dynamo.impl.file.model.FSFile;
import io.vertigo.dynamo.impl.file.model.StreamFile;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.util.TempFile;

/**
* Implémentation du gestionnaire de la définition des fichiers.
*
* @author pchretien
*/
public final class FileManagerImpl implements FileManager {

	private final Optional<Integer> purgeDelayMinutesOpt;

	/**
	 * Constructor.
	 * @param purgeDelayMinutesOpt Temp file purge delay.
	 */
	@Inject
	public FileManagerImpl(@Named("purgeDelayMinutes") final Optional<Integer> purgeDelayMinutesOpt) {
		this.purgeDelayMinutesOpt = purgeDelayMinutesOpt;
		final File documentRootFile = new File(TempFile.VERTIGO_TMP_DIR_PATH);
		Assertion.checkState(documentRootFile.exists(), "Vertigo temp dir doesn't exists ({0})", TempFile.VERTIGO_TMP_DIR_PATH);
		Assertion.checkState(documentRootFile.canRead(), "Vertigo temp dir can't be read ({0})", TempFile.VERTIGO_TMP_DIR_PATH);
		Assertion.checkState(documentRootFile.canWrite(), "Vertigo temp dir can't be write ({0})", TempFile.VERTIGO_TMP_DIR_PATH);
	}

	/** {@inheritDoc} */
	@Override
	public File obtainReadOnlyFile(final VFile file) {
		return doObtainReadOnlyFile(file);
	}

	/** {@inheritDoc} */
	@Override
	public VFile createFile(final String fileName, final String typeMime, final File file) {
		return new FSFile(fileName, typeMime, file);
	}

	/** {@inheritDoc} */
	@Override
	public VFile createFile(final File file) {
		return new FSFile(file.getName(), new MimetypesFileTypeMap().getContentType(file), file);
	}

	/** {@inheritDoc} */
	@Override
	public VFile createFile(final String fileName, final Date lastModified, final long length, final InputStreamBuilder inputStreamBuilder) {
		return createFile(fileName, new MimetypesFileTypeMap().getContentType(fileName), lastModified, length, inputStreamBuilder);
	}

	/** {@inheritDoc} */
	@Override
	public VFile createFile(final String fileName, final String mimeType, final Date lastModified, final long length, final InputStreamBuilder inputStreamBuilder) {
		return new StreamFile(fileName, mimeType, lastModified, length, inputStreamBuilder);
	}

	/** {@inheritDoc} */
	@Override
	public VFile createFile(final String fileName, final String typeMime, final URL resourceUrl) {
		final long length;
		final long lastModified;
		try {
			final URLConnection connection = resourceUrl.openConnection();
			try {
				length = connection.getContentLength();
				lastModified = connection.getLastModified();
			} finally {
				connection.getInputStream().close();
			}
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Can't get file meta from url");
		}
		Assertion.checkArgument(length >= 0, "Can't get file meta from url");
		final InputStreamBuilder inputStreamBuilder = resourceUrl::openStream;
		return createFile(fileName, typeMime, new Date(lastModified), length, inputStreamBuilder);
	}

	/**
	 * Crée un fichier temporaire à partir d'un fileInfo.
	 * Attention le processus appelant doit s'assurer de la suppression de ce fichier temporaire.
	 * @param vFile FileInfo à utiliser
	 * @return Fichier temporaire.
	 */
	private static File createTempFile(final VFile vFile) {
		// TODO voir a ajouter une WeakRef sur FileInfo pour vérifier la suppression des fichiers temp après usage
		try {
			return doCreateTempFile(vFile);
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Can't create temp file for FileInfo " + vFile.getFileName());
		}
	}

	private static File doCreateTempFile(final VFile fileInfo) throws IOException {
		final File tmpFile = new TempFile("fileInfo", '.' + FileUtil.getFileExtension(fileInfo.getFileName()));
		try (final InputStream inputStream = fileInfo.createInputStream()) {
			FileUtil.copy(inputStream, tmpFile);
			return tmpFile;
		}
	}

	/**
	 * @param vFile FileInfo à lire
	 * @return Fichier physique readOnly (pour lecture d'un FileInfo)
	 */
	private static File doObtainReadOnlyFile(final VFile vFile) {
		final File inputFile;
		if (vFile instanceof FSFile) {
			inputFile = ((FSFile) vFile).getFile();
		} else {
			inputFile = createTempFile(vFile);
		}
		return inputFile;
	}

	/**
	 * Daemon for deleting old files.
	 */
	@DaemonScheduled(name = "DMN_PRUGE_TEMP_FILE", periodInSeconds = 5 * 60)
	public void deleteOldFiles() {
		final File documentRootFile = new File(TempFile.VERTIGO_TMP_DIR_PATH);
		final long maxTime = System.currentTimeMillis() - purgeDelayMinutesOpt.orElse(60) * 60L * 1000L;
		doDeleteOldFiles(documentRootFile, maxTime);
	}

	private static void doDeleteOldFiles(final File documentRootFile, final long maxTime) {
		for (final File subFiles : documentRootFile.listFiles()) {
			if (subFiles.isDirectory() && subFiles.canRead()) { //canRead pour les pbs de droits
				doDeleteOldFiles(subFiles, maxTime);
			} else if (subFiles.lastModified() < maxTime) {
				final boolean succeeded = subFiles.delete();
				if (!succeeded) {
					subFiles.deleteOnExit();
				}
			}
		}
	}
}
