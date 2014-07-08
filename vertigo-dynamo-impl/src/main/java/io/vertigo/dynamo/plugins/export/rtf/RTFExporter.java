package io.vertigo.dynamo.plugins.export.rtf;

import io.vertigo.dynamo.impl.export.core.ExportHelper;
import io.vertigo.dynamo.plugins.export.pdfrtf.AbstractExporterIText;

import java.io.OutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.field.RtfPageNumber;
import com.lowagie.text.rtf.field.RtfTotalPageNumber;
import com.lowagie.text.rtf.headerfooter.RtfHeaderFooter;

/**
 * Handler d'export RTF avec iText.
 * Configur� par ExportParametersRTF.
 *
 * @author evernat
 * @version $Id: RTFExporter.java,v 1.1 2013/07/10 15:46:44 npiedeloup Exp $
 */
final class RTFExporter extends AbstractExporterIText {
	// l'impl�mentation de ExportHandlerRTF est majoritairement commune avec ExportHandlerPDF

	RTFExporter(final ExportHelper exportHelper) {
		super(exportHelper);
	}

	/** {@inheritDoc}*/
	@Override
	protected void createWriter(final Document document, final OutputStream out) {
		//final RtfWriter2 writer =
		RtfWriter2.getInstance(document, out);
		//writer.setViewerPreferences(PdfWriter.PageLayoutTwoColumnLeft);

		// advanced page numbers : x/y
		final Paragraph footerParagraph = new Paragraph();
		final Font font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 12, Font.NORMAL);
		footerParagraph.add(new RtfPageNumber(font));
		footerParagraph.add(new Phrase(" / ", font));
		footerParagraph.add(new RtfTotalPageNumber(font));
		footerParagraph.setAlignment(Element.ALIGN_CENTER);
		final HeaderFooter footer = new RtfHeaderFooter(footerParagraph);
		footer.setBorder(Rectangle.TOP);
		document.setFooter(footer);
	}
}
