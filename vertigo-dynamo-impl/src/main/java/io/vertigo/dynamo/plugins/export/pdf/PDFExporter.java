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
package io.vertigo.dynamo.plugins.export.pdf;

import io.vertigo.dynamo.impl.export.core.ExportHelper;
import io.vertigo.dynamo.plugins.export.pdfrtf.AbstractExporterIText;

import java.io.OutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Export PDF avec iText.
 * Configuré par ExportParametersPDF.
 *
 * @author evernat
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
