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
package io.vertigo.quarto.plugins.converter.xdocreport;

import fr.opensagres.xdocreport.converter.ConverterRegistry;
import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.IConverter;
import fr.opensagres.xdocreport.converter.Options;
import fr.opensagres.xdocreport.converter.XDocConverterException;
import fr.opensagres.xdocreport.core.document.DocumentKind;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.file.util.TempFile;
import io.vertigo.dynamo.impl.file.model.FSFile;
import io.vertigo.lang.Assertion;
import io.vertigo.quarto.impl.converter.ConverterPlugin;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Plugin de conversion du format ODT au format PDF.
 * Utilisant la librairie xdocreport.
 *
 * @author jgarnier
 */
public final class XDocReportConverterPlugin implements ConverterPlugin {

	@Override
	public VFile convertToFormat(final VFile file, final String targetFormat) {
		final ConverterFormat targetConverterFormat = ConverterFormat.find(targetFormat);
		Assertion.checkArgument(!targetConverterFormat.getTypeMime().equals(file.getMimeType()),
				"Le format de sortie est identique à celui d'entrée ; la conversion est inutile");
		Assertion.checkArgument(file.getMimeType().equalsIgnoreCase(ConverterFormat.ODT.getTypeMime()),
				"Seul le format ODT peut être utilisé en entrée");
		Assertion.checkArgument(targetFormat.equalsIgnoreCase(ConverterFormat.PDF.name()),
				"Seul le format PDF peut être utilisé en sortie");
		//-----
		final Options options = Options.getFrom(DocumentKind.ODT).to(ConverterTypeTo.PDF);
		final IConverter converter = ConverterRegistry.getRegistry().getConverter(options);
		try (InputStream in = file.createInputStream()) {
			String fileName = file.getFileName();
			if (fileName.indexOf('.') > 0) {
				fileName = fileName.substring(0, fileName.indexOf('.'));
			}
			final TempFile resultFile = new TempFile(fileName, '.' + targetFormat.toLowerCase());
			final OutputStream out = new FileOutputStream(resultFile);
			converter.convert(in, out, options);
			final VFile pdf = new FSFile(resultFile.getName(), ConverterFormat.PDF.getTypeMime(), resultFile);
			return pdf;
		} catch (final IOException | XDocConverterException e) {
			throw new RuntimeException(e);
		}
	}
}
