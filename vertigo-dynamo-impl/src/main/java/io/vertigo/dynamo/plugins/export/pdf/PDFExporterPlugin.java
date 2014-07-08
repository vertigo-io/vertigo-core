package io.vertigo.dynamo.plugins.export.pdf;

import io.vertigo.dynamo.export.Export;
import io.vertigo.dynamo.export.ExportFormat;
import io.vertigo.dynamo.impl.export.ExporterPlugin;
import io.vertigo.dynamo.impl.export.core.ExportHelper;
import io.vertigo.dynamo.persistence.PersistenceManager;

import java.io.OutputStream;

import javax.inject.Inject;

import com.lowagie.text.DocumentException;

/**
 * Plugin d'export PDF.
 *
 * @author pchretien, npiedeloup
 * @version $Id: PDFExporterPlugin.java,v 1.2 2014/01/28 18:49:44 pchretien Exp $
 */
public final class PDFExporterPlugin implements ExporterPlugin {
	private final ExportHelper exportHelper;

	@Inject
	public PDFExporterPlugin(final PersistenceManager persistenceManager) {
		exportHelper = new ExportHelper(persistenceManager);
	}

	/** {@inheritDoc} */
	public void exportData(final Export export, final OutputStream out) throws DocumentException {
		new PDFExporter(exportHelper).exportData(export, out);
	}

	/** {@inheritDoc}*/
	public boolean accept(final ExportFormat exportFormat) {
		return ExportFormat.PDF.equals(exportFormat);
	}

}
