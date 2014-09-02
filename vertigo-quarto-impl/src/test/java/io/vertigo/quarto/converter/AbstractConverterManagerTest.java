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
package io.vertigo.quarto.converter;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.dynamo.file.util.TempFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * Test de l'implémentation standard.
 * 
 * @author npiedeloup
 */
public abstract class AbstractConverterManagerTest extends AbstractTestCaseJU4 {
	/** Logger. */
	private final Logger log = Logger.getLogger(getClass());

	@Inject
	private ConverterManager converterManager;

	@Inject
	private FileManager fileManager;

	private KFile resultFile;

	/** {@inheritDoc} */
	@Override
	protected void doSetUp() throws Exception {
		//rien
	}

	/** {@inheritDoc} */
	@Override
	protected void doTearDown() throws Exception {
		if (resultFile != null) {
			resultFile = null; //Les fichiers temporaires étant en WeakRef, cela supprime le fichier
		}
	}

	/**
	 * Converssion de Odt vers Odt.
	 * Liste des formats dans io.vertigo.quarto.plugins.converter.openoffice.ConverterFormat
	 */
	@Test
	public void testConvertOdt2Odt() {
		final KFile inputFile = createKFile(fileManager, "../data/testFile.odt", this.getClass());
		resultFile = converterManager.convert(inputFile, "ODT");

		log("Odt2Odt", resultFile);
	}

	/**
	 * Converssion de Odt vers Doc.
	 */
	@Test
	public void testConvertOdt2Doc() {
		final KFile inputFile = createKFile(fileManager, "../data/testFile.odt", this.getClass());
		resultFile = converterManager.convert(inputFile, "DOC");

		log("Odt2Doc", resultFile);
	}

	/**
	 * Converssion de Odt vers Rtf.
	 */
	@Test
	public void testConvertOdt2Rtf() {
		final KFile inputFile = createKFile(fileManager, "../data/testFile.odt", this.getClass());
		resultFile = converterManager.convert(inputFile, "RTF");

		log("Odt2Rtf", resultFile);
	}

	/**
	 * Converssion de Odt vers Pdf.
	 */
	@Test
	public void testConvertOdt2Pdf() {
		final KFile inputFile = createKFile(fileManager, "../data/testFile.odt", this.getClass());
		resultFile = converterManager.convert(inputFile, "PDF");

		log("Odt2Pdf", resultFile);
	}

	/**
	 * Converssion de Odt vers Txt.
	 */
	@Test
	public void testConvertOdt2Txt() {
		final KFile inputFile = createKFile(fileManager, "../data/testFile.odt", this.getClass());
		resultFile = converterManager.convert(inputFile, "TXT");

		log("Odt2Txt", resultFile);
	}

	/**
	 * Converssion de Txt vers Odt.
	 */
	@Test
	public void testConvertTxt2Odt() {
		final KFile inputFile = createKFile(fileManager, "../data/testFile.txt", this.getClass());
		resultFile = converterManager.convert(inputFile, "ODT");

		log("Txt2Odt", resultFile);
	}

	/**
	 * Converssion de Txt vers Doc.
	 */
	@Test
	public void testConvertTxt2Doc() {
		final KFile inputFile = createKFile(fileManager, "../data/testFile.txt", this.getClass());
		resultFile = converterManager.convert(inputFile, "DOC");

		log("Txt2Doc", resultFile);
	}

	/**
	 * Converssion de Txt vers Rtf.
	 */
	@Test
	public void testConvertTxt2Rtf() {
		final KFile inputFile = createKFile(fileManager, "../data/testFile.txt", this.getClass());
		resultFile = converterManager.convert(inputFile, "RTF");

		log("Txt2Rtf", resultFile);
	}

	/**
	 * Converssion de Txt vers Pdf.
	 */
	@Test
	public void testConvertTxt2Pdf() {
		final KFile inputFile = createKFile(fileManager, "../data/testFile.txt", this.getClass());
		resultFile = converterManager.convert(inputFile, "PDF");

		log("Txt2Pdf", resultFile);
	}

	/**
	 * Converssion de Txt vers Pdf.
	 */
	@Test
	public void testConvertTxt2Txt() {
		final KFile inputFile = createKFile(fileManager, "../data/testFile.txt", this.getClass());
		resultFile = converterManager.convert(inputFile, "PDF");

		log("Txt2Txt", resultFile);
	}

	private void log(final String methode, final KFile kFile) {
		log.info(methode + " => " + fileManager.obtainReadOnlyFile(kFile).getAbsolutePath());
	}

	private static KFile createKFile(final FileManager fileManager, final String fileName, final Class<?> baseClass) {
		try (final InputStream in = baseClass.getResourceAsStream(fileName)) {
			Assertion.checkNotNull(in, "fichier non trouvé : {0}", fileName);
			final File file = new TempFile("tmp", '.' + FileUtil.getFileExtension(fileName));
			FileUtil.copy(in, file);
			return fileManager.createFile(file);
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
