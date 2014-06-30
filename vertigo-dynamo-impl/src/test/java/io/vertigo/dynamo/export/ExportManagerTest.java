package io.vertigo.dynamo.export;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.dynamock.domain.famille.Famille;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.MessageText;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import org.junit.Test;

/**
 * Test de l'impl�mentation standard.
 * 
 * @author dchallas
 * @version $Id: ExportManagerTest.java,v 1.6 2014/02/27 10:27:31 pchretien Exp $
 */
public final class ExportManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private ExportManager exportManager;
	//R�pertoire de test
	private static String OUTPUT_PATH = "c:/tmp/";
	private static final boolean KEEP_OUTPUT_FILE = false;

	/**
	 * Test l'export CSV.
	 */
	@Test
	public void testExportHandlerCSV() {
		final DtList<Famille> dtc = createDtc();
		final ExportDtParameters dtParameter = exportManager.createExportListParameters(dtc);

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test.csv")//
				.withExportDtParameters(dtParameter)//
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
		final ExportDtParameters dtParameter = exportManager.createExportObjectParameters(famille);

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test2.csv")//
				.withExportDtParameters(dtParameter)//
				.build();
		final KFile result = exportManager.createExportFile(export);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	/**
	 * Test l'export CSV d'un champs donn�e.
	 */
	@Test
	public void testExportField() {
		final Famille famille = new Famille();
		final DtDefinition dtFamille = DtObjectUtil.findDtDefinition(Famille.class);
		final DtField dtField = dtFamille.getField("LIBELLE");
		famille.setLibelle("Test");
		final ExportDtParameters dtParameter = exportManager.createExportObjectParameters(famille);
		dtParameter.addExportField(dtField);

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test3.csv")//
				.withExportDtParameters(dtParameter)//
				.build();
		final KFile result = exportManager.createExportFile(export);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	/**
	 * Test l'export CSV d'un champs avec label surcharg�.
	 */
	@Test
	public void testExportFieldOverrideLabel() {
		final Famille famille = new Famille();
		final DtDefinition dtFamille = DtObjectUtil.findDtDefinition(Famille.class);

		final DtField dtField = dtFamille.getField("LIBELLE");
		famille.setLibelle("Test");
		final ExportDtParameters dtParameter = exportManager.createExportObjectParameters(famille);
		dtParameter.addExportField(dtField, new MessageText("test", null));

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test3.csv")//
				.withExportDtParameters(dtParameter)//
				.build();
		final KFile result = exportManager.createExportFile(export);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	/**
	 * Test l'export CSV d'un champs avec une d�normalisation de sa valeur.
	 */
	@Test
	public void testExportFieldDenorm() {
		final DtList<Famille> list = createDtc();
		final Famille famille = new Famille();
		final DtDefinition dtFamille = DtObjectUtil.findDtDefinition(Famille.class);
		famille.setFamId(1L);
		final DtField dtFieldKey = dtFamille.getField("FAM_ID");
		final DtField dtFieldLabel = dtFamille.getField("LIBELLE");
		famille.setLibelle("Test");
		final ExportDtParameters dtParameter = exportManager.createExportObjectParameters(famille);
		dtParameter.addExportDenormField(dtFieldKey, list, dtFieldLabel);

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test4.csv")//
				.withExportDtParameters(dtParameter)//
				.build();

		final KFile result = exportManager.createExportFile(export);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	/**
	 * Test l'export CSV d'un champs avec une d�normalisation de sa valeur, et surcharge du label.
	 */
	@Test
	public void testExportFieldDenormOverrideLabel() {
		final DtList<Famille> list = createDtc();
		final Famille famille = new Famille();
		final DtDefinition dtFamille = DtObjectUtil.findDtDefinition(Famille.class);
		famille.setFamId(1L);
		final DtField dtFieldKey = dtFamille.getField("FAM_ID");
		final DtField dtFieldLabel = dtFamille.getField("LIBELLE");
		famille.setLibelle("Test");
		final ExportDtParameters dtParameter = exportManager.createExportObjectParameters(famille);
		dtParameter.addExportDenormField(dtFieldKey, list, dtFieldLabel, new MessageText("test", null));

		final Export export = new ExportBuilder(ExportFormat.CSV, OUTPUT_PATH + "test5.csv")//
				.withExportDtParameters(dtParameter)//
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
		final ExportDtParameters dtParameter = exportManager.createExportListParameters(dtc);

		final Export export = new ExportBuilder(ExportFormat.XLS, OUTPUT_PATH + "test.xls")//
				.withExportDtParameters(dtParameter)//
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
		final ExportDtParameters dtParameter = exportManager.createExportListParameters(dtc);

		final Export export = new ExportBuilder(ExportFormat.RTF, OUTPUT_PATH + "test.rtf")//
				.withAuthor("test")//
				.withTitle("test title")//
				.withExportDtParameters(dtParameter)//
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
		final ExportDtParameters dtParameter = exportManager.createExportListParameters(dtc);

		final Export export = new ExportBuilder(ExportFormat.PDF, OUTPUT_PATH + "test.pdf").withExportDtParameters(dtParameter)//
				.withAuthor("test")//
				.build();

		final KFile result = exportManager.createExportFile(export);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	/**
	 * Test l'export ODS.
	 */
	@Test
	public void testExportHandlerODS() {
		final DtList<Famille> dtc = createDtc();
		final ExportDtParameters dtParameter = exportManager.createExportListParameters(dtc);

		final Export export = new ExportBuilder(ExportFormat.ODS, OUTPUT_PATH + "test.ods")//
				.withExportDtParameters(dtParameter)//
				.withAuthor("test")//
				.build();

		final KFile result = exportManager.createExportFile(export);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	private static DtList<Famille> createDtc() {
		final DtList<Famille> dtc = new DtList<>(Famille.class);
		// les index sont donn�es par ordre alpha > null � la fin >
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
			throw new VRuntimeException(e);
		}
	}
}
