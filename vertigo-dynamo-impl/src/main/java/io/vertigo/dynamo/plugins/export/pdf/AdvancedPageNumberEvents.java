package io.vertigo.dynamo.plugins.export.pdf;

import io.vertigo.kernel.exception.VRuntimeException;

import java.io.IOException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Advanced page number x/y events.
 */
final class AdvancedPageNumberEvents extends PdfPageEventHelper {
	// This is the contentbyte object of the writer
	private PdfContentByte cb;

	// we will put the final number of pages in a template
	private PdfTemplate template;

	// this is the BaseFont we are going to use for the header / footer
	private BaseFont bf;

	/**
	 * Constructeur.
	 */
	AdvancedPageNumberEvents() {
		super();
	}

	/** {@inheritDoc} */
	@Override
	public void onGenericTag(final PdfWriter writer, final Document document, final Rectangle rect, final String text) {
		// rien
	}

	/** {@inheritDoc} */
	@Override
	public void onOpenDocument(final PdfWriter writer, final Document document) {
		try {
			bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			cb = writer.getDirectContent();
			template = cb.createTemplate(50, 50);
		} catch (final DocumentException de) {
			throw new VRuntimeException(de);
		} catch (final IOException ioe) {
			throw new VRuntimeException(ioe);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onChapter(final PdfWriter writer, final Document document, final float paragraphPosition, final Paragraph title) {
		// rien
	}

	/** {@inheritDoc} */
	@Override
	public void onEndPage(final PdfWriter writer, final Document document) {
		final int pageN = writer.getPageNumber();
		final String text = pageN + " / ";
		final float len = bf.getWidthPoint(text, 8);
		cb.beginText();
		cb.setFontAndSize(bf, 8);
		final float width = document.getPageSize().getWidth();
		cb.setTextMatrix(width / 2, 30);
		cb.showText(text);
		cb.endText();
		cb.addTemplate(template, width / 2 + len, 30);
	}

	/** {@inheritDoc} */
	@Override
	public void onCloseDocument(final PdfWriter writer, final Document document) {
		template.beginText();
		template.setFontAndSize(bf, 8);
		template.showText(String.valueOf(writer.getPageNumber() - 1));
		template.endText();
	}
}
