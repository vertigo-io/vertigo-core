package io.vertigo.quarto.publisher;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.quarto.publisher.PublisherManager;
import io.vertigo.quarto.publisher.metamodel.PublisherDataDefinition;
import io.vertigo.quarto.publisher.mock.Address;
import io.vertigo.quarto.publisher.mock.Enquete;
import io.vertigo.quarto.publisher.mock.Enqueteur;
import io.vertigo.quarto.publisher.mock.MisEnCause;
import io.vertigo.quarto.publisher.mock.PublisherMock;
import io.vertigo.quarto.publisher.mock.Ville;
import io.vertigo.quarto.publisher.model.PublisherData;
import io.vertigo.quarto.publisher.model.PublisherNode;
import io.vertigo.quarto.x.publisher.PublisherDataUtil;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test de l'impl�mentation standard.
 * 
 * @author npiedeloup
 * @version $Id: AbstractPublisherMergerTest.java,v 1.8 2014/02/27 10:25:49 pchretien Exp $
 */
public abstract class AbstractPublisherMergerTest extends AbstractTestCaseJU4 {
	private static final boolean KEEP_OUTPUT_FILE = false;
	//R�pertoire de test
	private static String OUTPUT_PATH = "c:/tmp/";

	private static final String DATA_PACKAGE = "io/vertigo/quarto/publisher/data/";

	@Inject
	private PublisherManager publisherManager;
	@Inject
	private ResourceManager resourceManager;

	/**
	 * @return Extension du model utilis�
	 */
	protected abstract String getExtension();

	@Test
	public void testMergerSimple() {
		final PublisherMock reportData = createTestPublisher();

		final PublisherData publisherData = createPublisherData("PU_PUBLISHER_MOCK");
		PublisherDataUtil.populateData(reportData, publisherData.getRootNode());

		final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModel." + getExtension());
		final KFile result = publisherManager.publish(OUTPUT_PATH + "testFusion." + getExtension(), modelFileURL, publisherData);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
		nop(result);
		Assert.assertTrue(true);
	}

	@Test
	public void testMergerSpecialsCharacters() {
		final PublisherMock reportData = createTestPublisher();

		final PublisherData publisherData = createPublisherData("PU_PUBLISHER_MOCK");
		PublisherDataUtil.populateData(reportData, publisherData.getRootNode());
		publisherData.getRootNode().setString("COMMENTAIRE", " euro:" + (char) 128 + "\n gt:>\n lt:<\n tab:>\t<\n cr:>\n<\n  amp:&\n dquote:\"\n squote:\'\n");

		final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModel." + getExtension());
		final KFile result = publisherManager.publish(OUTPUT_PATH + "testFusionSpecialsCharacters." + getExtension(), modelFileURL, publisherData);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
		nop(result);
		Assert.assertTrue(true);
	}

	@Test(expected = IllegalStateException.class)
	public void testMergerErrorModel() {
		final PublisherMock reportData = createTestPublisher();
		final PublisherData publisherData = createPublisherData("PU_PUBLISHER_MOCK");
		PublisherDataUtil.populateData(reportData, publisherData.getRootNode());
		final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModelError." + getExtension());
		final KFile result = publisherManager.publish(OUTPUT_PATH + "testFusionError." + getExtension(), modelFileURL, publisherData);
		nop(result);
		Assert.fail("La fusion ne doit pas �tre possible quand le mod�le contient une erreur");
	}

	@Test
	public void testMergerModelIfEquals() {
		final PublisherMock reportData = createTestPublisher();
		final PublisherData publisherData = createPublisherData("PU_PUBLISHER_MOCK");
		PublisherDataUtil.populateData(reportData, publisherData.getRootNode());
		publisherData.getRootNode().setString("TITRE", "NOM");
		final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModelIfEquals." + getExtension());
		final KFile result = publisherManager.publish(OUTPUT_PATH + "testFusionIfEquals." + getExtension(), modelFileURL, publisherData);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
		nop(result);
		Assert.assertTrue(true);

	}

	@Test
	public void testMergerModelIfNotEquals() {
		final PublisherMock reportData = createTestPublisher();
		final PublisherData publisherData = createPublisherData("PU_PUBLISHER_MOCK");
		PublisherDataUtil.populateData(reportData, publisherData.getRootNode());
		final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModelIfEquals." + getExtension());
		final KFile result = publisherManager.publish(OUTPUT_PATH + "testFusionIfNotEquals." + getExtension(), modelFileURL, publisherData);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
		nop(result);
		Assert.assertTrue(true);

	}

	@Test
	public void testMergerEnquete() {
		final Enquete dtoEnquete = DataHelper.createEnquete();
		final Enqueteur dtoEnqueteur = DataHelper.createEnqueteur();
		final Address dtoAdresse = DataHelper.createAdresse();
		final Ville dtoVille = DataHelper.createVille();
		final DtList<? extends MisEnCause> dtcMisEnCause = DataHelper.createMisEnCauseList();

		final PublisherData publisherData = createPublisherData("PU_ENQUETE");
		final PublisherNode pnEnquete = publisherData.getRootNode();
		PublisherDataUtil.populateData(dtoEnquete, pnEnquete);

		final PublisherNode pnEnqueteur = pnEnquete.createNode("ENQUETEUR");
		PublisherDataUtil.populateData(dtoEnqueteur, pnEnqueteur);
		pnEnquete.setNode("ENQUETEUR", pnEnqueteur);

		final PublisherNode pnAdresse = pnEnqueteur.createNode("ADRESSE_RATACHEMENT");
		PublisherDataUtil.populateData(dtoAdresse, pnAdresse);
		pnEnqueteur.setNode("ADRESSE_RATACHEMENT", pnAdresse);

		final PublisherNode pnVille = pnAdresse.createNode("VILLE");
		PublisherDataUtil.populateData(dtoVille, pnVille);
		pnAdresse.setNode("VILLE", pnVille);

		final List<PublisherNode> publisherNodes = new ArrayList<>();
		for (final MisEnCause dtoMisEnCause : dtcMisEnCause) {
			final PublisherNode pnMisEnCause = pnEnquete.createNode("MIS_EN_CAUSE");
			PublisherDataUtil.populateData(dtoMisEnCause, pnMisEnCause);
			publisherNodes.add(pnMisEnCause);
		}
		pnEnquete.setNodes("MIS_EN_CAUSE", publisherNodes);

		final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModelEnquete." + getExtension());
		final KFile result = publisherManager.publish(OUTPUT_PATH + "testFusionEnquete." + getExtension(), modelFileURL, publisherData);
		nop(result);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	@Test
	public void testMergerEnquetePerField() {
		final Enquete dtoEnquete = DataHelper.createEnquete();
		final Enqueteur dtoEnqueteur = DataHelper.createEnqueteur();
		final Address dtoAdresse = DataHelper.createAdresse();
		final Ville dtoVille = DataHelper.createVille();
		final DtList<? extends MisEnCause> dtcMisEnCause = DataHelper.createMisEnCauseList();

		final PublisherData publisherData = createPublisherData("PU_ENQUETE");
		final PublisherNode pnEnquete = publisherData.getRootNode();
		PublisherDataUtil.populateData(dtoEnquete, pnEnquete);

		final PublisherNode pnEnqueteur = PublisherDataUtil.populateField(pnEnquete, "ENQUETEUR", dtoEnqueteur);
		final PublisherNode pnAdresse = PublisherDataUtil.populateField(pnEnqueteur, "ADRESSE_RATACHEMENT", dtoAdresse);
		PublisherDataUtil.populateField(pnAdresse, "VILLE", dtoVille);
		PublisherDataUtil.populateField(pnEnquete, "MIS_EN_CAUSE", dtcMisEnCause);

		final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModelEnquete." + getExtension());
		final KFile result = publisherManager.publish(OUTPUT_PATH + "testFusionEnquetePerField." + getExtension(), modelFileURL, publisherData);
		nop(result);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
	}

	@Test
	public void testMergerBlock() {
		final PublisherMock reportData = createTestPublisher();

		final PublisherData publisherData = createPublisherData("PU_PUBLISHER_MOCK");
		PublisherDataUtil.populateData(reportData, publisherData.getRootNode());

		final URL modelFileURL = resourceManager.resolve(DATA_PACKAGE + "ExempleModel2." + getExtension());
		final KFile result = publisherManager.publish(OUTPUT_PATH + "testFusionBlock." + getExtension(), modelFileURL, publisherData);
		if (KEEP_OUTPUT_FILE) {
			save(result);
		}
		nop(result);
		Assert.assertTrue(true);
	}

	//	/**
	//	 */
	//	public void testMergerImageJpg() {
	//		final DtObject reportData = createTestPublisher();
	//		final PublisherDataDtoExtractor dataExtractor = new PublisherDataDtoExtractor(reportData);
	//
	//		final KFile image = TestUtil.createKFile("data/logo.jpg", getClass());
	//		final PublisherData publisherData = dataExtractor.getPublisherData();
	//		publisherData.getRoot().addImage("LOGO", image);
	//		final PublisherWork publisherWork = publisherManager.createWork("c:/xxx/testFusionImage."+getExtension(), PACKAGE + "/data/ExempleModelImage."+getExtension(), "report", publisherData);
	//
	//		final KFile result = workManager.process(publisherWork);
	//		log.trace("testMergerImageJpg result : " + result);
	//		assertTrue(true);
	//	}
	//
	//	/**
	//	 */
	//	public void testMergerImageRatio() {
	//		final DtObject reportData = createTestPublisher();
	//		final PublisherDataDtoExtractor dataExtractor = new PublisherDataDtoExtractor(reportData);
	//
	//		final KFile image = TestUtil.createKFile("data/logoMasque.gif", getClass());
	//		final PublisherData publisherData = dataExtractor.getPublisherData();
	//		publisherData.getRoot().addImage("LOGO", image);
	//		final PublisherWork publisherWork = publisherManager.createWork("c:/xxx/testFusionImage2."+getExtension(), PACKAGE + "/data/ExempleModelImage2."+getExtension(), "report", publisherData);
	//
	//		final KFile result = workManager.process(publisherWork);
	//		log.trace("testMergerImageRatio result : " + result);
	//		assertTrue(true);
	//	}

	private PublisherMock createTestPublisher() {
		final PublisherMock reportData = new PublisherMock();
		reportData.setTitre("Mon titre");
		reportData.setNom("BITUM");
		reportData.setPrenom("John");
		reportData.setAddress("38, rue de la corniche\n01345 - rivera");
		reportData.setCommentaire("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium urna pulvinar massa placerat imperdiet. Curabitur vestibulum dui eget nibh consequat eget ultrices velit iaculis. Ut justo ipsum, euismod nec pulvinar sit amet, consectetur in dui. Ut sed ligula ligula. Phasellus libero enim, congue nec volutpat dignissim, pulvinar luctus urna.\n\tLorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium urna pulvinar massa placerat imperdiet. Curabitur vestibulum dui eget nibh consequat eget ultrices velit iaculis. Ut justo ipsum, euismod nec pulvinar sit amet, consectetur in dui. Ut sed ligula ligula. Phasellus libero enim, congue nec volutpat dignissim, pulvinar luctus urna.\n\t1-\tLorem ipsum dolor\n\t2-\tLorem ipsum dolor\n\t3-\tLorem ipsum dolor");
		reportData.setBoolean1(true);
		reportData.setBoolean2(false);
		reportData.setBoolean3(true);
		reportData.setTestDummy("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium urna pulvinar massa placerat imperdiet. Curabitur vestibulum dui eget nibh consequat eget ultrices velit iaculis. Ut justo ipsum, euismod nec pulvinar sit amet, consectetur in dui. Ut sed ligula ligula. Phasellus libero enim, congue nec volutpat dignissim, pulvinar luctus urna.");
		reportData.setTestLong(45676L);
		reportData.setTestDouble(79613D);
		reportData.setTestInteger(79413);
		reportData.setTestDate(new Date());

		//		final DtList<DtObject> testList = TestHelper.createDtList(AbstractPublisherMock.DEFINITION_URN);
		//		for (int i = 0; i < 50; i++) {
		//			testList.add(createTestPublisherRandom(String.valueOf(i)));
		//		}
		//		setFieldValue(reportData, "DTC_TEST", testList);

		return reportData;
	}

	//	private DtObject createTestPublisherRandom(final String name) {
	//		final DtObject reportData = TestHelper.createDtObject(AbstractPublisherMock.DEFINITION_URN);
	//		setFieldValue(reportData, "TITRE", "Obj dyn " + name);
	//		setFieldValue(reportData, "NOM", "BITUM");
	//		setFieldValue(reportData, "PRENOM", "John");
	//		setFieldValue(reportData, "ADDRESS", "38, rue de la corniche\n01345 - rivera");
	//		setFieldValue(reportData, "COMMENTAIRE", "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium urna pulvinar massa placerat imperdiet. Curabitur vestibulum dui eget nibh consequat eget ultrices velit iaculis. Ut justo ipsum, euismod nec pulvinar sit amet, consectetur in dui. Ut sed ligula ligula. Phasellus libero enim, congue nec volutpat dignissim, pulvinar luctus urna.\n\tLorem ipsum dolor sit amet, consectetur adipiscing elit. Curabitur pretium urna pulvinar massa placerat imperdiet. Curabitur vestibulum dui eget nibh consequat eget ultrices velit iaculis. Ut justo ipsum, euismod nec pulvinar sit amet, consectetur in dui. Ut sed ligula ligula. Phasellus libero enim, congue nec volutpat dignissim, pulvinar luctus urna.\n\t1-\tLorem ipsum dolor\n\t2-\tLorem ipsum dolor\n\t3-\tLorem ipsum dolor");
	//		setFieldValue(reportData, "TEST_LONG", Math.round(Math.random() * 1000));
	//		setFieldValue(reportData, "TEST_DOUBLE", Math.random() * 1000);
	//		setFieldValue(reportData, "TEST_INTEGER", (int) Math.round(Math.random() * 100));
	//		setFieldValue(reportData, "TEST_DATE", new Date());
	//		return reportData;
	//	}

	private PublisherData createPublisherData(final String definitionName) {
		final PublisherDataDefinition publisherDataDefinition = Home.getDefinitionSpace().resolve(definitionName, PublisherDataDefinition.class);
		Assert.assertNotNull(publisherDataDefinition);

		final PublisherData publisherData = new PublisherData(publisherDataDefinition);
		Assert.assertNotNull(publisherData);

		return publisherData;
	}

	private static void save(final KFile result) {
		try {
			FileUtil.copy(result.createInputStream(), new File(result.getFileName()));
		} catch (final IOException e) {
			throw new VRuntimeException(e);
		}
	}
}
