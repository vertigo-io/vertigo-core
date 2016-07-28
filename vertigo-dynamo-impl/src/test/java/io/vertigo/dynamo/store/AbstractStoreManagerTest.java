/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.store;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.dynamo.TestUtil;
import io.vertigo.dynamo.database.SqlDataBaseManager;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DomainBuilder;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURI;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.dynamo.impl.store.util.DAO;
import io.vertigo.dynamo.store.data.domain.car.Car;
import io.vertigo.dynamo.store.data.domain.car.CarDataBase;
import io.vertigo.dynamo.store.data.domain.famille.Famille;
import io.vertigo.dynamo.store.data.fileinfo.FileInfoStd;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionWritable;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.dynamox.task.TaskEngineSelect;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ListBuilder;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public abstract class AbstractStoreManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private SqlDataBaseManager dataBaseManager;
	@Inject
	protected StoreManager storeManager;
	@Inject
	protected FileManager fileManager;
	@Inject
	protected VTransactionManager transactionManager;
	@Inject
	protected TaskManager taskManager;

	protected DtDefinition dtDefinitionFamille;
	private DtDefinition dtDefinitionCar;
	private DtListURI allCarsUri;

	private DAO<Famille, Integer> familleDAO;
	private long initialDbCarSize = 0;

	@Override
	protected void doSetUp() throws Exception {
		dtDefinitionFamille = DtObjectUtil.findDtDefinition(Famille.class);
		familleDAO = new DAO<>(dtDefinitionFamille, storeManager, taskManager);

		dtDefinitionCar = DtObjectUtil.findDtDefinition(Car.class);
		allCarsUri = new DtListURIForCriteria<>(dtDefinitionCar, null, null);

		initMainStore();
	}

	protected void initMainStore() {
		//A chaque test on recrée la table famille
		createDataBase(getCreateMainStoreRequests(), "TK_INIT_MAIN", Optional.<String> empty());

		final CarDataBase carDataBase = new CarDataBase();
		carDataBase.loadDatas();
		initialDbCarSize = carDataBase.size();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			for (final Car car : carDataBase.getAllCars()) {
				car.setId(null);
				storeManager.getDataStore().create(car);
			}
			transaction.commit();
		}
	}

	protected void createDataBase(final List<String> requests, final String taskName, final Optional<String> collection) {
		//A chaque test on recrée la table famille
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			for (final String request : requests) {
				final TaskDefinitionBuilder taskDefinitionBuilder = new TaskDefinitionBuilder(taskName)
						.withEngine(TaskEngineProc.class)
						.withRequest(request);
				if (collection.isPresent()) {
					taskDefinitionBuilder.withDataSpace(collection.get());
				}
				final Task task = new TaskBuilder(taskDefinitionBuilder.build()).build();
				taskManager.execute(task);
			}
		}
	}

	protected List<String> getCreateMainStoreRequests() {
		return new ListBuilder<String>()
				.addAll(getCreateFamilleRequests())
				.addAll(getCreateCarRequests())
				.addAll(getCreateFileInfoRequests())
				.build();
	}

	protected final List<String> getCreateFamilleRequests() {
		return new ListBuilder<String>()
				.add(" create table famille(fam_id BIGINT , LIBELLE varchar(255));")
				.add(" create sequence SEQ_FAMILLE start with 10001 increment by 1;")
				.build();
	}

	protected final List<String> getCreateCarRequests() {
		return new ListBuilder<String>()
				.add(" create table fam_car_location(fam_id BIGINT , ID BIGINT);")
				.add(" create table car(ID BIGINT, FAM_ID BIGINT, MAKE varchar(50), MODEL varchar(255), DESCRIPTION varchar(512), YEAR INT, KILO INT, PRICE INT, CONSOMMATION NUMERIC(8,2), MOTOR_TYPE varchar(50) );")
				.add(" create sequence SEQ_CAR start with 10001 increment by 1;")
				.build();
	}

	protected final List<String> getCreateFileInfoRequests() {
		return new ListBuilder<String>()
				.add(" create table VX_FILE_INFO(FIL_ID BIGINT , FILE_NAME varchar(255), MIME_TYPE varchar(255), LENGTH BIGINT, LAST_MODIFIED date, FILE_DATA BLOB);")
				.add(" create sequence SEQ_VX_FILE_INFO start with 10001 increment by 1;")
				.build();
	}

	@Override
	protected void doTearDown() throws Exception {
		shutDown("TK_SHUT_DOWN", Optional.<String> empty());
	}

	protected void shutDown(final String taskName, final Optional<String> collectionOption) {
		if (dataBaseManager != null) {
			try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				final TaskDefinitionBuilder taskDefinitionBuilder = new TaskDefinitionBuilder(taskName)
						.withEngine(TaskEngineProc.class)
						.withRequest("shutdown;");
				if (collectionOption.isPresent()) {
					taskDefinitionBuilder.withDataSpace(collectionOption.get());
				}
				final Task task = new TaskBuilder(taskDefinitionBuilder.build()).build();
				taskManager.execute(task);

				//A chaque fin de test on arréte la base.
				transaction.commit();
			}
		}
	}

	@Test
	public void testSelectCarCachedRowMax() {
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final DtListURI someCars = new DtListURIForCriteria<>(dtDefinitionCar, null, 3);
			final DtList<Car> dtc1 = storeManager.getDataStore().findAll(someCars);
			Assert.assertEquals(3, dtc1.size());
			//-----
			final DtListURI allCars = new DtListURIForCriteria<>(dtDefinitionCar, null, null);
			final DtList<Car> dtc = storeManager.getDataStore().findAll(allCars);
			Assert.assertEquals(9, dtc.size());
		}
	}

	@Test
	public void testSelectCountCars() {
		final TaskDefinition taskDefinition = new TaskDefinitionBuilder("TK_COUNT_CARS")
				.withEngine(TaskEngineSelect.class)
				.withRequest("select count(*) from CAR")
				.withOutAttribute("count", new DomainBuilder("DO_COUNT", DataType.Long).build(), true)
				.build();

		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final Task task = new TaskBuilder(taskDefinition).build();
			final long count = taskManager
					.execute(task)
					.getResult();
			//-----
			Assert.assertEquals(9, count);
		}
	}

	protected final void nativeInsertCar(final Car car) {
		Assertion.checkArgument(car.getId() == null, "L'id n'est pas null {0}", car.getId());
		//-----
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final Domain doCar = definitionSpace.resolve("DO_DT_CAR_DTO", Domain.class);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder("TK_INSERT_CAR")
				.withEngine(TaskEngineProc.class)
				.withRequest("insert into CAR (ID, FAM_ID,MAKE, MODEL, DESCRIPTION, YEAR, KILO, PRICE, MOTOR_TYPE) values "
						//syntaxe HsqlDb pour sequence.nextval
						+ "(NEXT VALUE FOR SEQ_CAR, #DTO_CAR.FAM_ID#, #DTO_CAR.MAKE#, #DTO_CAR.MODEL#, #DTO_CAR.DESCRIPTION#, #DTO_CAR.YEAR#, #DTO_CAR.KILO#, #DTO_CAR.PRICE#, #DTO_CAR.MOTOR_TYPE#)")
				.addInAttribute("DTO_CAR", doCar, true)
				.build();

		final Task task = new TaskBuilder(taskDefinition)
				.addValue("DTO_CAR", car)
				.build();
		final TaskResult taskResult = taskManager
				.execute(task);
		nop(taskResult);
	}

	protected final DtList<Car> nativeLoadCarList() {
		final DefinitionSpace definitionSpace = getApp().getDefinitionSpace();
		final Domain doCarList = definitionSpace.resolve("DO_DT_CAR_DTC", Domain.class);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder("TK_LOAD_ALL_CARS")
				.withEngine(TaskEngineSelect.class)
				.withRequest("select * from CAR")
				.withOutAttribute("dtc", doCarList, true)
				.build();

		final Task task = new TaskBuilder(taskDefinition)
				.build();
		return taskManager
				.execute(task)
				.getResult();
	}

	/**
	 * On vérifie que la liste est vide.
	 */
	@Test
	public void testGetFamille() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final DtListURI allFamilles = new DtListURIForCriteria<>(dtDefinitionFamille, null, null);
			final DtList<Famille> dtc = storeManager.getDataStore().findAll(allFamilles);
			Assert.assertNotNull(dtc);
			Assert.assertTrue("La liste des famille est vide", dtc.isEmpty());
			transaction.commit();
		}
	}

	/**
	 * On charge une liste, ajoute un element et recharge la liste pour verifier l'ajout.
	 */
	@Test
	public void testAddFamille() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final DtListURI allFamilles = new DtListURIForCriteria<>(dtDefinitionFamille, null, null);
			DtList<Famille> dtc = storeManager.getDataStore().findAll(allFamilles);
			Assert.assertEquals(0, dtc.size());
			//-----
			final Famille famille = new Famille();
			famille.setLibelle("encore un");
			storeManager.getDataStore().create(famille);
			// on attend un objet avec un ID non null ?
			Assert.assertNotNull(famille.getFamId());
			//-----
			dtc = storeManager.getDataStore().findAll(allFamilles);
			Assert.assertEquals(1, dtc.size());
			transaction.commit();
		}
	}

	/**
	 * on vérifier l'exception levée si une contrainte bdd n'est pas respecté.
	 */
	@Test(expected = Exception.class)
	public void testCreateFamilleFail() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final DecimalFormat df = new DecimalFormat("000000000:");
			//-----
			final Famille famille = new Famille();
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 4000; i++) {
				sb.append(df.format(i));
			}
			// libelle
			famille.setLibelle(sb.toString());
			//On doit échouer car le libellé est trop long
			storeManager.getDataStore().create(famille);
			Assert.fail();
		}
	}

	@Test
	public void testCreateFile() throws Exception {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//1.Création du fichier depuis un fichier texte du FS

			final VFile vFile = TestUtil.createVFile(fileManager, "data/lautreamont.txt", AbstractStoreManagerTest.class);
			//2. Sauvegarde en BDD
			final FileInfo fileInfo = new FileInfoStd(vFile);
			storeManager.getFileStore().create(fileInfo);

			//3.relecture du fichier
			final FileInfo readFileInfo = storeManager.getFileStore().read(fileInfo.getURI());

			//4. comparaison du fichier créé et du fichier lu.

			final String source;
			try (final OutputStream sourceOS = new java.io.ByteArrayOutputStream()) {
				FileUtil.copy(vFile.createInputStream(), sourceOS);
				source = sourceOS.toString();
			}

			final String read;
			try (final OutputStream readOS = new java.io.ByteArrayOutputStream()) {
				FileUtil.copy(readFileInfo.getVFile().createInputStream(), readOS);
				read = readOS.toString();
			}
			//on vérifie que le contenu des fichiers est identique.
			//assertEquals("toto", "toto");
			//assertEquals("toto", "ti");
			Assert.assertEquals(source, read);
			Assert.assertTrue("Test contenu du fichier", read.startsWith("Chant I"));
			Assert.assertTrue("Test contenu du fichier : " + secureSubString(read, 16711, "ses notes langoureuses,"), read.indexOf("ses notes langoureuses,") > 0);
			Assert.assertTrue("Test contenu du fichier : " + secureSubString(read, 11004, "mal : \"Adolescent,"), read.indexOf("mal : \"Adolescent,") > 0);

			//On désactive pour l'instant
			//Ne marche pas sur la PIC pour cause de charset sur le àé			//Assert.assertTrue("Test contenu du fichier : " + secureSubString(read, 15579, "adieu !à ;"), read.indexOf("adieu !à ;") > 0);
		}
	}

	protected static String secureSubString(final String read, final int index, final String searchString) {
		if (read != null && read.length() > index) {
			return read.substring(index, Math.min(read.length() - 1, index + searchString.length()));
		}
		return "N/A";
	}

	/**
	 * Test que les listes NN ne reste pas en cache après une mise à jour.
	 * Ici l'entité en cache est la destination de la navigation : Car
	 */
	@Test
	public void testGetFamilleLocationCars() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on crée une famille
			final Famille famille = new Famille();
			famille.setLibelle("Ma famille");
			storeManager.getDataStore().create(famille);

			//on récupère la liste des voitures
			final DtList<Car> cars = storeManager.getDataStore().findAll(allCarsUri);
			Assert.assertNotNull(cars);
			Assert.assertFalse("La liste des cars est vide", cars.isEmpty());

			//on associe la liste de voiture à la famille en NN
			final List<URI> carUriList = new ArrayList<>();
			for (final Car car : cars) {
				carUriList.add(new URI(dtDefinitionCar, car.getId()));
			}
			familleDAO.updateNN(famille.getVoituresLocationDtListURI(), carUriList);

			//On garde le résultat de l'association NN
			final DtList<Car> firstResult = famille.getVoituresLocationList();
			Assert.assertEquals("Test tailles du nombre de voiture dans une NN", cars.size(), firstResult.size());

			//On met à jour l'association en retirant le premier élément
			carUriList.remove(0);
			familleDAO.updateNN(famille.getVoituresLocationDtListURI(), carUriList);

			//on garde le résultat en lazy : il doit avoir le meme nombre de voiture qu'au début
			final DtList<Car> lazyResult = famille.getVoituresLocationList();
			Assert.assertEquals("Test tailles du nombre de voiture pour une NN", firstResult.size(), lazyResult.size());

			//on recharge la famille et on recharge la liste issus de l'association NN : il doit avoir une voiture de moins qu'au début
			final DtDefinition dtFamille = DtObjectUtil.findDtDefinition(Famille.class);
			final Famille famille2 = storeManager.getDataStore().<Famille> read(new URI<Famille>(dtFamille, famille.getFamId()));
			final DtList<Car> secondResult = famille2.getVoituresLocationList();
			Assert.assertEquals("Test tailles du nombre de voiture dans une NN", firstResult.size() - 1, secondResult.size());
			transaction.commit();
		}
	}

	/**
	 * Test que les listes 1N ne reste pas en cache après une mise à jour.
	 * Ici l'entité en cache est la destination de la navigation : Car
	 */
	@Test
	public void testGetFamilliesCars() {
		//on crée une famille
		final Famille famille = new Famille();
		famille.setLibelle("Ma famille");

		final DtList<Car> firstResult;
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().create(famille);

			//on récupère la liste des voitures
			final DtList<Car> cars = storeManager.getDataStore().findAll(allCarsUri);
			Assert.assertNotNull(cars);
			Assert.assertFalse("La liste des cars est vide", cars.isEmpty());

			//on associe la liste de voiture à la famille en 1N
			for (final Car car : cars) {
				car.setFamId(famille.getFamId());
				storeManager.getDataStore().update(car);
			}

			//On garde le résultat de l'association 1N
			firstResult = famille.getVoituresFamilleList();

			//On met à jour l'association en retirant le premier élément
			final Car firstCar = cars.get(0);
			firstCar.setFamId(null);
			storeManager.getDataStore().update(firstCar);
			transaction.commit(); //sans commit le cache n'est pas rafraichit
		}

		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {

			//on garde le résultat en lazy : il doit avoir le meme nombre de voiture qu'au début
			final DtList<Car> lazyResult = famille.getVoituresFamilleList();
			Assert.assertEquals("Test tailles du nombre de voiture pour une 1-N", firstResult.size(), lazyResult.size());

			//on recharge la famille et on recharge la liste issus de l'association 1N : il doit avoir une voiture de moins qu'au début
			final DtDefinition dtFamille = DtObjectUtil.findDtDefinition(Famille.class);
			final Famille famille2 = storeManager.getDataStore().<Famille> read(new URI<Famille>(dtFamille, famille.getFamId()));
			final DtList<Car> secondResult = famille2.getVoituresFamilleList();
			Assert.assertEquals("Test tailles du nombre de voiture pour une 1-N", firstResult.size() - 1, secondResult.size());
			transaction.commit();
		}
	}

	@Test
	public void testTxCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkCrudCarsCount(0);
		}
	}

	@Test
	public void testTxNativeSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			checkNativeCarsCount(0);
		}
	}

	@Test
	public void testTxCrudInsertCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			storeManager.getDataStore().create(car);

			//on récupère la liste des voitures
			checkCrudCarsCount(1);
		}
	}

	@Test
	public void testTxNativeInsertCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);

			//on récupère la liste des voitures
			checkCrudCarsCount(1);
		}
	}

	@Test
	public void testTxCrudInsertNativeSelectRollback() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			storeManager.getDataStore().create(car);

			//on récupère la liste des voitures
			checkNativeCarsCount(1);
		}
	}

	@Test
	public void testTxNativeInsertNativeSelectRollback() {
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);

			//on récupère la liste des voitures
			checkNativeCarsCount(1);
		}
	}

	@Test
	public void testTxCrudInsertRollbackCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			storeManager.getDataStore().create(car);
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkCrudCarsCount(0);
		}
	}

	@Test
	public void testTxNativeInsertRollbackCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkCrudCarsCount(0);
		}
	}

	@Test
	public void testTxCrudInsertRollbackNativeSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			storeManager.getDataStore().create(car);
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			checkNativeCarsCount(0);
		}
	}

	@Test
	public void testTxNativeInsertRollbackNativeSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			checkNativeCarsCount(0);
		}
	}

	@Test
	public void testTxNativeInsertCrudInsertCommit() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			final Car car2 = createNewCar();
			nativeInsertCar(car2);
			storeManager.getDataStore().create(car);
			transaction.commit();
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testTxCrudInsertTwoCommit() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			storeManager.getDataStore().create(car);
			transaction.commit();
			transaction.commit();
		}
	}

	@Test
	public void testTxCrudInsertCommitCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			storeManager.getDataStore().create(car);
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkCrudCarsCount(1);
		}
	}

	@Test
	public void testTxNativeInsertCommitCrudSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkCrudCarsCount(1);
		}
	}

	@Test
	public void testTxCrudInsertCommitNativeSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			storeManager.getDataStore().create(car);
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkNativeCarsCount(1);
		}
	}

	@Test
	public void testTxNativeInsertCommitNativeSelectRollback() {
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar();
			nativeInsertCar(car);
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			checkNativeCarsCount(1);
		}
	}

	@Test(expected = NullPointerException.class)
	public void testCrudInsertNoTx() {
		final Car car = createNewCar();
		storeManager.getDataStore().create(car);
	}

	@Test(expected = NullPointerException.class)
	public void testNativeInsertNoTx() {
		final Car car = createNewCar();
		nativeInsertCar(car);
	}

	@Test(expected = NullPointerException.class)
	public void testCrudSelectNoTx() {
		checkCrudCarsCount(0);
	}

	@Test(expected = NullPointerException.class)
	public void testNativeSelectNoTx() {
		checkNativeCarsCount(0);
	}

	@Test
	public void testPerfCrudInsertCrudSelectRollback() {
		final long start = System.currentTimeMillis();
		int execCount = 0;
		while (System.currentTimeMillis() - start < 1000) {
			testTxCrudInsertCrudSelectRollback();
			execCount++;
		}
		final long time = System.currentTimeMillis() - start;
		System.out.println(execCount + " exec en 1s. moy=" + time * 1000 / execCount / 1000d + "ms");
	}

	@Test
	public void testPerfNativeInsertNativeSelectRollback() {
		final long start = System.currentTimeMillis();
		int execCount = 0;
		while (System.currentTimeMillis() - start < 1000) {
			testTxNativeInsertNativeSelectRollback();
			execCount++;
		}
		final long time = System.currentTimeMillis() - start;
		System.out.println(execCount + " exec en 1s. moy=" + time * 1000 / execCount / 1000d + "ms");
	}

	@Test
	public void testCrudCountCars() {
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final long count = storeManager.getDataStore().count(dtDefinitionCar);
			//-----
			Assert.assertEquals(9, count);
		}
	}

	@Test
	public void testTxCrudInsertDeleteCommit() {
		final Car car = createNewCar();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().create(car);
			//Check cars count
			checkCrudCarsCount(1);
			final URI<Car> carUri = DtObjectUtil.createURI(car);
			storeManager.getDataStore().delete(carUri);
			checkCrudCarsCount(1); //car is cacheable : list was'nt flush here
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			checkCrudCarsCount(0); //car is cacheable : must wait commit to see delete
		}
	}

	@Test
	public void testTxCrudInsertCommitCrudDeleteCommit() {
		final Car car = createNewCar();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().create(car);
			checkCrudCarsCount(1);
			transaction.commit();
		}
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final URI<Car> carUri = DtObjectUtil.createURI(car);
			storeManager.getDataStore().delete(carUri);
			checkCrudCarsCount(0);
			transaction.commit();
		}
	}

	@Test
	public void testTxCrudLockCommit() {
		final Car car = createNewCar();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			storeManager.getDataStore().create(car);
			//Check cars count
			checkCrudCarsCount(1);
			final URI<Car> carUri = DtObjectUtil.createURI(car);
			storeManager.getDataStore().readForUpdate(carUri);
			checkCrudCarsCount(1);
			transaction.commit();
		}
	}

	private void checkNativeCarsCount(final int deltaCount) {
		final DtList<Car> cars = nativeLoadCarList();
		Assert.assertNotNull(cars);
		Assert.assertEquals("Test du nombre de voiture", initialDbCarSize + deltaCount, cars.size());
	}

	private void checkCrudCarsCount(final int deltaCount) {
		final DtList<Car> cars = storeManager.getDataStore().findAll(allCarsUri);
		Assert.assertNotNull(cars);
		Assert.assertEquals("Test du nombre de voiture", initialDbCarSize + deltaCount, cars.size());
	}

	private static Car createNewCar() {
		final Car car = new Car();
		car.setId(null);
		car.setPrice(5600);
		car.setMake("Peugeot");
		car.setModel("407");
		car.setYear(2014);
		car.setKilo(20000);
		car.setMotorType("essence".toLowerCase());
		car.setDescription("Vds 407 de test, 2014, 20000 kms, rouge, TBEG");
		return car;
	}
}
