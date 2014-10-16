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
package io.vertigo.dynamo.impl.export;

import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.export.ExportManager;
import io.vertigo.dynamo.export.model.Export;
import io.vertigo.dynamo.export.model.ExportFormat;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.file.util.TempFile;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamo.work.WorkResultHandler;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

/**
 * Implémentation standard du manager des exports.
 * 
 * @author pchretien, npiedeloup
 */
public final class ExportManagerImpl implements ExportManager {
	private final WorkManager workManager;
	private final FileManager fileManager;
	private final List<ExporterPlugin> exporterPlugins;

	/**
	 * Constructeur.
	 */
	@Inject
	public ExportManagerImpl(final WorkManager workManager, final FileManager fileManager, final List<ExporterPlugin> exporterPlugins) {
		Assertion.checkNotNull(workManager);
		Assertion.checkNotNull(fileManager);
		Assertion.checkNotNull(exporterPlugins);
		// ---------------------------------------------------------------------
		this.workManager = workManager;
		this.fileManager = fileManager;
		this.exporterPlugins = Collections.unmodifiableList(exporterPlugins);
	}

	/**
	 * Récupère le plugin d'export associé au format.
	 * 
	 * @param exportFormat
	 *            Format d'export souhaité
	 * @return Plugin d'export associé au format
	 */
	private ExporterPlugin getExporterPlugin(final ExportFormat exportFormat) {
		Assertion.checkNotNull(exportFormat);
		// ---------------------------------------------------------------------
		for (final ExporterPlugin exporterPlugin : exporterPlugins) {
			if (exporterPlugin.accept(exportFormat)) {
				return exporterPlugin;
			}
		}
		throw new RuntimeException("aucun plugin trouve pour le format " + exportFormat);
	}

	/** {@inheritDoc} */
	public void createExportFileASync(final Export export, final WorkResultHandler<KFile> workResultHandler) {
		Assertion.checkNotNull(export);
		// ---------------------------------------------------------------------
		workManager.schedule(new Callable<KFile>() {
			public KFile call() {
				return createExportFile(export);
			}
		}, workResultHandler);
	}

	/** {@inheritDoc} */
	public KFile createExportFile(final Export export) {
		Assertion.checkNotNull(export);
		// ---------------------------------------------------------------------
		try {
			return generateFile(export);
		} catch (final Exception e) {
			// Quelle que soit l'exception on l'encapsule pour préciser le nom
			// du fichier.
			final String msg = "La génération du fichier a échoué.<!-- " + e.getMessage() + "--> pour le fichier " + export.getFileName();
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * Ecrire dans un fichier temporaire en passant par le writer le données
	 * (CSV, XML, DOC,...).
	 * 
	 * @return Fichier temporaire généré
	 * @param export
	 *            Paramètres de l'export
	 * @throws Exception
	 *             Exception lors de la création du fichier
	 */
	private KFile generateFile(final Export export) throws Exception {
		final ExporterPlugin exporterPlugin = getExporterPlugin(export.getFormat());

		final File file = new TempFile("csvGenerated", "." + export.getFormat().name().toLowerCase());
		try (final FileOutputStream fileOutputStream = new FileOutputStream(file)) {
			exporterPlugin.exportData(export, fileOutputStream);
		} catch (final Exception e) {
			if (!file.delete()) {
				file.deleteOnExit();
			}
			throw e;
		}
		return fileManager.createFile(export.getFileName() + "." + export.getFormat().name().toLowerCase(), export.getFormat().getTypeMime(), file);
	}

}
