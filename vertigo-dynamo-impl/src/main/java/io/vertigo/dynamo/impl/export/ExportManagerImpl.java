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

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.export.Export;
import io.vertigo.dynamo.export.ExportDtParameters;
import io.vertigo.dynamo.export.ExportFormat;
import io.vertigo.dynamo.export.ExportManager;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.file.util.TempFile;
import io.vertigo.dynamo.impl.export.core.ExportDtParametersImpl;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;

/**
 * Impl�mentation standard du manager des exports.
 *
 * @author pchretien, npiedeloup
 * @version $Id: ExportManagerImpl.java,v 1.8 2014/02/27 10:23:30 pchretien Exp $
 */
public final class ExportManagerImpl implements ExportManager {
	private final WorkManager workManager;
	private final FileManager fileManager;
	@Inject
	private List<ExporterPlugin> exporterPlugins;

	/**
	 * Constructeur.
	 */
	@Inject
	public ExportManagerImpl(final WorkManager workManager, final FileManager fileManager) {
		Assertion.checkNotNull(workManager);
		Assertion.checkNotNull(fileManager);
		//---------------------------------------------------------------------
		this.workManager = workManager;
		this.fileManager = fileManager;
	}

	/** {@inheritDoc} */
	public ExportDtParameters createExportObjectParameters(final DtObject dto) {
		return new ExportDtParametersImpl(dto);
	}

	/** {@inheritDoc} */
	public ExportDtParameters createExportListParameters(final DtList<?> dtc) {
		return new ExportDtParametersImpl(dtc);
	}

	/**
	 * R�cup�re le plugin d'export associ� au format.
	 * @param exportFormat Format d'export souhait� 
	 * @return Plugin d'export associ� au format
	 */
	private ExporterPlugin getExporterPlugin(final ExportFormat exportFormat) {
		Assertion.checkNotNull(exportFormat);
		//---------------------------------------------------------------------
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
		//---------------------------------------------------------------------
		workManager.schedule(new Callable<KFile>() {
			public KFile call() {
				return createExportFile(export);
			}
		}, workResultHandler);
	}

	/** {@inheritDoc} */
	public KFile createExportFile(final Export export) {
		Assertion.checkNotNull(export);
		//---------------------------------------------------------------------
		try {
			return generateFile(export);
		} catch (final Exception e) {
			//Quelle que soit l'exception on l'encapsule pour pr�ciser le nom du fichier.
			final String msg = "La g�n�ration du fichier a �chou�.<!-- " + e.getMessage() + "--> pour le fichier " + export.getFileName();
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * Ecrire dans un fichier temporaire en passant par le writer le donn�es (CSV, XML, DOC,...).
	 * @return Fichier temporaire g�n�r�
	 * @param export Param�tres de l'export
	 * @throws Exception Exception lors de la cr�ation du fichier
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
