package io.vertigo.dynamo.plugins.export.ods;

import io.vertigo.dynamo.export.Export;
import io.vertigo.dynamo.export.ExportFormat;
import io.vertigo.dynamo.impl.export.ExporterPlugin;
import io.vertigo.dynamo.impl.export.core.ExportHelper;
import io.vertigo.dynamo.persistence.PersistenceManager;

import java.io.OutputStream;

import javax.inject.Inject;

/**
 * Plugin d'export ODS.
 *
 * @author oboitel, npiedeloup
 * @version $Id: ODSExporterPlugin.java,v 1.2 2014/01/28 18:49:44 pchretien Exp $
 */
public final class ODSExporterPlugin implements ExporterPlugin {
	private final ExportHelper exportHelper;

	@Inject
	public ODSExporterPlugin(final PersistenceManager persistenceManager) {
		exportHelper = new ExportHelper(persistenceManager);
	}

	/** {@inheritDoc} */
	public void exportData(final Export exportWork, final OutputStream out) throws Exception {
		new ODSExporter(exportHelper).exportData(exportWork, out);
	}
	/** {@inheritDoc}*/
	public boolean accept(final ExportFormat exportFormat) {
		return ExportFormat.ODS.equals(exportFormat);
	}
}
