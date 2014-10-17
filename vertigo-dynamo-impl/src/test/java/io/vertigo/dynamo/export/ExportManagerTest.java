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
package io.vertigo.dynamo.export;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.export.model.Export;
import io.vertigo.dynamo.export.model.ExportBuilder;
import io.vertigo.dynamo.export.model.ExportFormat;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.dynamock.domain.DtDefinitions.FamilleFields;
import io.vertigo.dynamock.domain.famille.Famille;
import io.vertigo.lang.MessageText;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.junit.Test;

/**
 * Test de l'implémentation standard.
 *
 * @author dchallas
 */
public final class ExportManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private ExportManager exportManager;
	// Répertoire de test
	private static String OUTPUT_PATH = "c:/tmp/";
	private static final boolean KEEP_OUTPUT_FILE = false;

	/**
	 * Test l'export CSV.
	 */
	@Test
	public void testExportHandlerCSV() {
		final DtList<Famille> dtc = createDtc();

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test.csv")
				.beginSheet(dtc, "famille").endSheet()
				.build();
		final KFile result = exportManager.createExportFile(export);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	/**
	 * Test l'export CSV d'un objet.
	 */
	@Test
	public void testExportObject() {
		final Famille famille = new Famille();
		famille.setLibelle("Test");

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test2.csv")
				.beginSheet(famille, "famille").endSheet()
				.build();
		final KFile result = exportManager.createExportFile(export);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	/**
	 * Test l'export CSV d'un champs donnée.
	 */
	@Test
	public void testExportField() {
		final Famille famille = new Famille();
		famille.setLibelle("Test");

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test3.csv")
				.beginSheet(famille, "famille").withField(FamilleFields.LIBELLE).endSheet()
				.build();

		final KFile result = exportManager.createExportFile(export);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	/**
	 * Test l'export CSV d'un champs avec label surchargé.
	 */
	@Test
	public void testExportFieldOverrideLabel() {
		final Famille famille = new Famille();
		famille.setLibelle("Test");

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test3.csv")
				.beginSheet(famille, "famille").withField(FamilleFields.LIBELLE, new MessageText("test", null)).endSheet()
				.build();

		final KFile result = exportManager.createExportFile(export);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	/**
	 * Test l'export CSV d'un champs avec une dénormalisation de sa valeur.
	 */
	@Test
	public void testExportFieldDenorm() {
		final DtList<Famille> list = createDtc();
		final Famille famille = new Famille();
		famille.setFamId(1L);
		famille.setLibelle("Test");

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test4.csv")
				.beginSheet(famille, "famille").withField(FamilleFields.FAM_ID, list, FamilleFields.LIBELLE).endSheet()
				.build();

		final KFile result = exportManager.createExportFile(export);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	/**
	 * Test l'export CSV d'un champs avec une dénormalisation de sa valeur, et
	 * surcharge du label.
	 */
	@Test
	public void testExportFieldDenormOverrideLabel() {
		final DtList<Famille> list = createDtc();
		final Famille famille = new Famille();
		famille.setFamId(1L);
		famille.setLibelle("Test");

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test5.csv")
				.beginSheet(famille, "famille").withField(FamilleFields.FAM_ID, list, FamilleFields.LIBELLE, new MessageText("test", null)).endSheet()
				.build();

		final KFile result = exportManager.createExportFile(export);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	/**
	 * Test l'export Excel.
	 */
	@Test
	public void testExportHandlerExcel() {
		final DtList<Famille> dtc = createDtc();

		final Export export = new ExportBuilder(ExportFormat.XLS, OUTPUT_PATH + "test.xls")
				.beginSheet(dtc, "famille").endSheet()
				.build();

		final KFile result = exportManager.createExportFile(export);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	/**
	 * Test l'export RTF.
	 */
	@Test
	public void testExportHandlerRTF() {
		final DtList<Famille> dtc = createDtc();

		final Export export = new ExportBuilder(ExportFormat.RTF, OUTPUT_PATH + "test.rtf")
				.withAuthor("test")
				.withTitle("test title")
				.beginSheet(dtc, "famille").endSheet()
				.build();

		final KFile result = exportManager.createExportFile(export);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	/**
	 * Test l'export PDF.
	 */
	@Test
	public void testExportHandlerPDF() {
		final DtList<Famille> dtc = createDtc();

		final Export export = new ExportBuilder(ExportFormat.PDF, OUTPUT_PATH + "test.pdf")
				.beginSheet(dtc, "famille").endSheet()
				.withAuthor("test")
				.build();

		final KFile result = exportManager.createExportFile(export);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	// /**
	// * Test l'export ODS.
	// */
	// @Test
	// public void testExportHandlerODS() {
	// final DtList<Famille> dtc = createDtc();
	// final ExportDtParameters dtParameter =
	// exportManager.createExportListParameters(dtc);
	//
	// final Export export = new ExportBuilder(ExportFormat.ODS, OUTPUT_PATH +
	// "test.ods")//
	// .withSheet(dtParameter)//
	// .withAuthor("test")//
	// .build();
	//
	// final KFile result = exportManager.createExportFile(export);
	// if (KEEP_OUTPUT_FILE) {
	// save(result);
	// }
	// }

	private static DtList<Famille> createDtc() {
		final DtList<Famille> dtc = new DtList<>(Famille.class);
		// les index sont données par ordre alpha > null à la fin >
		final Famille mockB = new Famille();
		mockB.setFamId(1L);
		mockB.setLibelle("Ba");
		dtc.add(mockB);

		final Famille mockNull = new Famille();
		mockB.setFamId(2L);
		// On ne renseigne pas le libelle > null
		dtc.add(mockNull);

		final Famille mocka = new Famille();
		mockB.setFamId(3L);
		mocka.setLibelle("aaa");
		dtc.add(mocka);

		final Famille mockb = new Famille();
		mockB.setFamId(4L);
		mockb.setLibelle("bb");
		dtc.add(mockb);

		return dtc;
	}

	private static void save(final KFile result) {
		try {
			FileUtil.copy(result.createInputStream(), new File(result.getFileName()));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}
