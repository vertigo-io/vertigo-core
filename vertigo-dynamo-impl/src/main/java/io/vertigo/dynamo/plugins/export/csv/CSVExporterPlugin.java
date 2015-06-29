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
package io.vertigo.dynamo.plugins.export.csv;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.dynamo.export.model.Export;
import io.vertigo.dynamo.export.model.ExportFormat;
import io.vertigo.dynamo.impl.export.ExporterPlugin;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.lang.Assertion;

import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

/**
 * Plugin d'export CSV.
 *
 * @author pchretien, npiedeloup
 */
public final class CSVExporterPlugin implements ExporterPlugin {
	private final CodecManager codecManager;
	private final StoreManager storeManager;

	/**
	 * Constructeur.
	 *
	 * @param codecManager Manager des mécanismes de codage/décodage.
	 */
	@Inject
	public CSVExporterPlugin(final StoreManager storeManager, final CodecManager codecManager) {
		Assertion.checkNotNull(storeManager);
		Assertion.checkNotNull(codecManager);
		//-----
		this.codecManager = codecManager;
		this.storeManager = storeManager;
	}

	/** {@inheritDoc} */
	@Override
	public void exportData(final Export export, final OutputStream out) throws IOException {
		new CSVExporter(codecManager, storeManager).exportData(export, out);
	}

	/** {@inheritDoc} */
	@Override
	public boolean accept(final ExportFormat exportFormat) {
		return ExportFormat.CSV.equals(exportFormat);
	}
}
