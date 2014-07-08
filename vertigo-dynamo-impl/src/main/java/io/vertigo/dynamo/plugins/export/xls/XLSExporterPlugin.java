package io.vertigo.dynamo.plugins.export.xls;

import io.vertigo.dynamo.export.Export;
import io.vertigo.dynamo.export.ExportFormat;
import io.vertigo.dynamo.impl.export.ExporterPlugin;
import io.vertigo.dynamo.impl.export.core.ExportHelper;
import io.vertigo.dynamo.persistence.PersistenceManager;

import java.io.IOException;
import java.io.OutputStream;

import javax.inject.Inject;

/**
 * Plugin d'export Excel.
 *
 * @author pchretien, npiedeloup
 * @version $Id: ExcelExporterPlugin.java,v 1.2 2014/01/28 18:49:44 pchretien Exp $
 */
public final class XLSExporterPlugin implements ExporterPlugin {
	private final ExportHelper exportHelper;

	@Inject
	public XLSExporterPlugin(final PersistenceManager persistenceManager) {
		exportHelper = new ExportHelper(persistenceManager);
	}

	/** {@inheritDoc}*/
	public void exportData(final Export export, final OutputStream out) throws IOException {
		new XLSExporter(exportHelper).exportData(export, out);
	}

	/** {@inheritDoc}*/
	public boolean accept(final ExportFormat exportFormat) {
		return ExportFormat.XLS.equals(exportFormat);
	}

}
