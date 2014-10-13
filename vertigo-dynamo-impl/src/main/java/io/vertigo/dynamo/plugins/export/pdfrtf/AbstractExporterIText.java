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
package io.vertigo.dynamo.plugins.export.pdfrtf;

import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.export.Export;
import io.vertigo.dynamo.export.ExportSheet;
import io.vertigo.dynamo.export.ExportField;
import io.vertigo.dynamo.impl.export.core.ExportHelper;

import java.awt.Color;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Table;

/**
 * @author pchretien, npiedeloup
 */
public abstract class AbstractExporterIText {
	private static final String CREATOR = "System";
	private final Map<DtField, Map<Object, String>> referenceCache = new HashMap<>();
	private final Map<DtField, Map<Object, String>> denormCache = new HashMap<>();

	private final ExportHelper exportHelper;

	protected AbstractExporterIText(final ExportHelper exportHelper) {
		Assertion.checkNotNull(exportHelper);
		//---------------------------------------------------------------------
		this.exportHelper = exportHelper;
	}

	/**
	 * We create a writer that listens to the document and directs a PDF-stream to out
	 * @param document Document
	 * @param out OutputStream
	 */
	protected abstract void createWriter(final Document document, final OutputStream out) throws DocumentException;

	/**
	 * Méthode principale qui gère l'export d'un tableau vers un fichier ODS.
	 * 
	 * @param export paramètres du document à exporter
	 * @param out flux de sortie
	 */
	public final void exportData(final Export export, final OutputStream out) throws DocumentException {
		// step 1: creation of a document-object
		final boolean landscape = export.getOrientation() == Export.Orientation.Landscape;
		final Rectangle pageSize = landscape ? PageSize.A4.rotate() : PageSize.A4;
		final Document document = new Document(pageSize, 20, 20, 50, 50); // left, right, top, bottom
		// step 2: we create a writer that listens to the document and directs a PDF-stream to out
		createWriter(document, out);

		// we add some meta information to the document, and we open it
		final String title = export.getTitle();
		if (title != null) {
			final HeaderFooter header = new HeaderFooter(new Phrase(title), false);
			header.setAlignment(Element.ALIGN_LEFT);
			header.setBorder(Rectangle.NO_BORDER);
			document.setHeader(header);
			document.addTitle(title);
		}

		final String author = export.getAuthor();
		document.addAuthor(author);
		document.addCreator(CREATOR);
		document.open();
		try {
			// pour ajouter l'ouverture automatique de la boîte de dialogue imprimer
			// (print(false) pour imprimer directement)
			// ((PdfWriter) writer).addJavaScript("this.print(true);", false);

			for (final ExportSheet exportSheet : export.getSheets()) {
				// table
				final Table datatable = new Table(exportSheet.getExportFields().size());
				datatable.setCellsFitPage(true);
				datatable.setPadding(4);
				datatable.setSpacing(0);

				// headers
				renderHeaders(exportSheet, datatable);

				// data rows
				renderList(exportSheet, datatable);

				document.add(datatable);
			}
		} finally {
			// we close the document
			document.close();
		}
	}

	/**
	 * Effectue le rendu des headers.
	 * @param parameters Paramètres
	 * @param datatable Table
	 */
	private static void renderHeaders(final ExportSheet parameters, final Table datatable) throws BadElementException {
		// size of columns
		// datatable.setWidths(headerwidths);
		// datatable.setWidth(100f);

		// table header
		final Font font = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD);
		datatable.getDefaultCell().setBorderWidth(2);
		datatable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		// datatable.getDefaultCell().setGrayFill(0.75f);

		for (final ExportField exportColumn : parameters.getExportFields()) {
			datatable.addCell(new Phrase(exportColumn.getLabel().getDisplay(), font));
		}
		// end of the table header
		datatable.endHeaders();
	}

	/**
	 * Effectue le rendu de la liste.
	 * @param parameters Paramètres
	 * @param datatable Table
	 */
	private void renderList(final ExportSheet parameters, final Table datatable) throws BadElementException {
		// data rows
		final Font font = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);
		final Font whiteFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.NORMAL);
		whiteFont.setColor(Color.WHITE);
		datatable.getDefaultCell().setBorderWidth(1);
		datatable.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
		// datatable.getDefaultCell().setGrayFill(0);

		// Parcours des DTO de la DTC
		for (final DtObject dto : parameters.getDtList()) {
			for (final ExportField exportColumn : parameters.getExportFields()) {
				final DtField dtField = exportColumn.getDtField();
				final Object value = dtField.getDataAccessor().getValue(dto);
				final int horizontalAlignement;
				if (value instanceof Number || value instanceof Date) {
					horizontalAlignement = Element.ALIGN_RIGHT;
				} else if (value instanceof Boolean) {
					horizontalAlignement = Element.ALIGN_CENTER;
				} else {
					horizontalAlignement = Element.ALIGN_LEFT;
				}
				datatable.getDefaultCell().setHorizontalAlignment(horizontalAlignement);

				String text = exportHelper.getText(referenceCache, denormCache, dto, exportColumn);
				if (text == null) {
					text = "";
				}
				datatable.addCell(new Phrase(8, text, font));
			}
		}
	}
}
