package io.vertigo.dynamo.plugins.export.pdf;

import io.vertigo.dynamo.impl.export.core.ExportHelper;
import io.vertigo.dynamo.plugins.export.pdfrtf.AbstractExporterIText;

import java.io.OutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Export PDF avec iText.
 * Configur� par ExportParametersPDF.
 *
 * @author evernat
 * @version $Id: PDFExporter.java,v 1.1 2013/07/10 15:46:44 npiedeloup Exp $
 */
final class PDFExporter extends AbstractExporterIText {
	PDFExporter(final ExportHelper exportHelper) {
		super(exportHelper);
	}

	/** {@inheritDoc}*/
	@Override
	protected void createWriter(final Document document, final OutputStream out) throws DocumentException {
		final PdfWriter writer = PdfWriter.getInstance(document, out);
		// writer.setViewerPreferences(PdfWriter.PageLayoutTwoColumnLeft);

		// simple page numbers : x
		// HeaderFooter footer = new HeaderFooter(new Phrase(), true);
		// footer.setAlignment(Element.ALIGN_RIGHT);
		// footer.setBorder(Rectangle.TOP);
		// document.setFooter(footer);

		// add the event handler for advanced page numbers : x/y
		writer.setPageEvent(new AdvancedPageNumberEvents());
	}
}
