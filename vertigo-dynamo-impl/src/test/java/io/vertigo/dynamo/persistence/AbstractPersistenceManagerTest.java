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
package io.vertigo.dynamo.persistence;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.Home;
import io.vertigo.dynamo.TestUtil;
import io.vertigo.dynamo.database.SqlDataBaseManager;
import io.vertigo.dynamo.database.connection.SqlConnection;
import io.vertigo.dynamo.database.statement.SqlCallableStatement;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.FileInfo;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.dynamo.file.util.FileUtil;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionWritable;
import io.vertigo.dynamock.domain.car.Car;
import io.vertigo.dynamock.domain.car.CarDataBase;
import io.vertigo.dynamock.domain.famille.Famille;
import io.vertigo.dynamock.fileinfo.FileInfoStd;
import io.vertigo.dynamox.task.TaskEngineProc;
import io.vertigo.dynamox.task.TaskEngineSelect;
import io.vertigo.lang.Assertion;

import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public abstract class AbstractPersistenceManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private SqlDataBaseManager dataBaseManager;
	@Inject
	private PersistenceManager persistenceManager;
	@Inject
	private FileManager fileManager;
	@Inject
	private KTransactionManager transactionManager;
	@Inject
	private TaskManager taskManager;

	private DtDefinition dtDefinitionFamille;

	private TaskDefinition taskLoadCar;
	private TaskDefinition taskInsertCar;
	private TaskDefinition taskUpdateCar;
	private TaskDefinition taskLoadCars;

	private long initialDbCarSize = 0;

	@Override
	protected void doSetUp() throws Exception {
		dtDefinitionFamille = DtObjectUtil.findDtDefinition(Famille.class);

		//A chaque test on recrée la table famille
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final SqlConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
			execCallableStatement(connection, "create table famille(fam_id BIGINT , LIBELLE varchar(255));");
			execCallableStatement(connection, "create sequence SEQ_FAMILLE start with 10001 increment by 1");

			execCallableStatement(connection, "create table fam_car_location(fam_id BIGINT , ID BIGINT);");

			execCallableStatement(connection, "create table car(ID BIGINT, FAM_ID BIGINT, MAKE varchar(50), MODEL varchar(255), DESCRIPTION varchar(512), YEAR INT, KILO INT, PRICE INT, CONSOMMATION NUMERIC(8,2), MOTOR_TYPE varchar(50) );");
			execCallableStatement(connection, "create sequence SEQ_CAR start with 10001 increment by 1");
			//execPreparedStatement(connection, "SELECT * FROM   INFORMATION_SCHEMA.SYSTEM_SESSIONS;");
			//execPreparedStatement(connection, "call next value for SEQ_CAR;");

			execCallableStatement(connection, "create table KX_FILE_INFO(FIL_ID BIGINT , FILE_NAME varchar(255), MIME_TYPE varchar(255), LENGTH BIGINT, LAST_MODIFIED date, FILE_DATA BLOB);");
			execCallableStatement(connection, "create sequence SEQ_KX_FILE_INFO start with 10001 increment by 1");
			transaction.commit();
		}

		final CarDataBase carDataBase = new CarDataBase();
		carDataBase.loadDatas();
		initialDbCarSize = carDataBase.size();
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			for (final Car car : carDataBase.createList()) {
				car.setId(null);
				persistenceManager.getBroker().save(car);
			}
			transaction.commit();
		}

		taskLoadCar = registerTaskLoadCar();
		taskInsertCar = registerTaskInsertCar();
		taskUpdateCar = registerTaskUpdateCar();
		taskLoadCars = registerTaskLoadCarList();
	}

	@Override
	protected void doTearDown() throws Exception {
		if (dataBaseManager != null) {
			try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
				//A chaque fin de test on arréte la base.
				final SqlConnection connection = dataBaseManager.getConnectionProvider().obtainConnection();
				execCallableStatement(connection, "shutdown;");
				transaction.commit();
			}
		}
	}

	private void execCallableStatement(final SqlConnection connection, final String sql) throws SQLException {
		final SqlCallableStatement callableStatement = dataBaseManager.createCallableStatement(connection, sql);
		callableStatement.init();
		callableStatement.executeUpdate();
	}

	private static TaskDefinition registerTaskInsertCar() {
		final Domain doCar = Home.getDefinitionSpace().resolve("DO_DT_CAR_DTO", Domain.class);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder("TK_INSERT_CAR")
				.withEngine(TaskEngineProc.class)
				.withRequest("insert into CAR (ID, FAM_ID,MAKE, MODEL, DESCRIPTION, YEAR, KILO, PRICE, MOTOR_TYPE) values "
						//syntaxe HsqlDb pour sequence.nextval
						+ "(NEXT VALUE FOR SEQ_CAR, #DTO_CAR.FAM_ID#, #DTO_CAR.MAKE#, #DTO_CAR.MODEL#, #DTO_CAR.DESCRIPTION#, #DTO_CAR.YEAR#, #DTO_CAR.KILO#, #DTO_CAR.PRICE#, #DTO_CAR.MOTOR_TYPE#)")
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.withAttribute("DTO_CAR", doCar, true, true)
				.build();

		Home.getDefinitionSpace().put(taskDefinition, TaskDefinition.class);
		return taskDefinition;
	}

	private static TaskDefinition registerTaskUpdateCar() {
		final Domain doCar = Home.getDefinitionSpace().resolve("DO_DT_CAR_DTO", Domain.class);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder("TK_UPDATE_CAR")
				.withEngine(TaskEngineProc.class)
				.withRequest("update CAR set "
						+ "FAM_ID = #DTO_CAR.FAM_ID#, "
						+ "MAKE = #DTO_CAR.MAKE#, "
						+ "MODEL = #DTO_CAR.MODEL#, "
						+ "DESCRIPTION = #DTO_CAR.DESCRIPTION#, "
						+ "YEAR = #DTO_CAR.YEAR#, "
						+ "KILO = #DTO_CAR.KILO#, "
						+ "PRICE = #DTO_CAR.PRICE#, "
						+ "MOTOR_TYPE = #DTO_CAR.MOTOR_TYPE#) "
						+ "where CAR_ID = #DTO_CAR.ID#" + "")
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.withAttribute("DTO_CAR", doCar, true, true)
				.build();

		Home.getDefinitionSpace().put(taskDefinition, TaskDefinition.class);
		return taskDefinition;
	}

	private static TaskDefinition registerTaskLoadCar() {
		final Domain doId = Home.getDefinitionSpace().resolve("DO_IDENTIFIANT", Domain.class);
		final Domain doCar = Home.getDefinitionSpace().resolve("DO_DT_CAR_DTO", Domain.class);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder("TK_LOAD_CAR_BY_ID")
				.withEngine(TaskEngineSelect.class)
				.withRequest("select * from CAR where ID = #ID#")
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.withAttribute("ID", doId, true, true)
				.withAttribute("DTO_CAR_OUT", doCar, true, false)
				.build();

		Home.getDefinitionSpace().put(taskDefinition, TaskDefinition.class);
		return taskDefinition;
	}

	private static TaskDefinition registerTaskLoadCarList() {
		final Domain doCarList = Home.getDefinitionSpace().resolve("DO_DT_CAR_DTC", Domain.class);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder("TK_LOAD_ALL_CARS")
				.withEngine(TaskEngineSelect.class)
				.withRequest("select * from CAR")
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.withAttribute("DTC_CAR_OUT", doCarList, true, false)
				.build();

		Home.getDefinitionSpace().put(taskDefinition, TaskDefinition.class);
		return taskDefinition;
	}

	protected final void nativeInsertCar(final Car car) {
		Assertion.checkArgument(car.getId() == null, "L'id n'est pas null {0}", car.getId());
		//-----
		final Task task = new TaskBuilder(taskInsertCar)
				.withValue("DTO_CAR", car)
				.build();
		final TaskResult taskResult = taskManager.execute(task);
		nop(taskResult);
	}

	protected final void nativeUpdateCar(final Car car) {
		Assertion.checkArgument(car.getId() != null, "L'id est null");
		//-----
		final Task task = new TaskBuilder(taskUpdateCar)
				.withValue("DTO_CAR", car)
				.build();
		final TaskResult taskResult = taskManager.execute(task);
		nop(taskResult);

	}

	protected final Car nativeLoadCar(final long carId) {
		final Task task = new TaskBuilder(taskLoadCar)
				.withValue("CAR_ID", carId)
				.build();
		final TaskResult taskResult = taskManager.execute(task);
		return taskResult.getValue("DTO_CAR_OUT");
	}

	protected final DtList<Car> nativeLoadCarList() {
		final Task task = new TaskBuilder(taskLoadCars)
				.build();
		final TaskResult taskResult = taskManager.execute(task);
		return taskResult.getValue("DTC_CAR_OUT");
	}

	protected final List<Map<String, String>> execPreparedStatement(final SqlConnection connection, final String sql) throws SQLException {

		final PreparedStatement preparedStatement = connection.getJdbcConnection().prepareStatement(sql);
		final List<Map<String, String>> result = new ArrayList<>();

		final ResultSet resultSet = preparedStatement.executeQuery();
		final ResultSetMetaData rsmd = resultSet.getMetaData();
		final int columnCount = rsmd.getColumnCount();
		String sep = "";
		for (int i = 0; i < columnCount; i++) {
			System.out.print(sep);
			System.out.print(rsmd.getColumnName(i + 1));
			sep = " | ";
		}
		System.out.print("\n");
		while (resultSet.next()) {
			sep = "";
			for (int i = 0; i < columnCount; i++) {
				System.out.print(sep);
				System.out.print(resultSet.getString(i + 1));
				sep = " | ";
			}
			System.out.print("\n");
		}
		return result;
	}

	//		//BIGINT > LONG
	//		statement.execute("SET PROPERTY \"sql.enforce_strict_size\" TRUE; " + "create table famille(ID BIGINT, LIBELLE varchar(255), PRIMARY KEY (ID))");
	//	// TODO private
	//	protected void createHsqlTablesFileInfo() throws SQLException {
	//		statement.execute("drop table KX_FILE_INFO if exists");
	//		statement.execute("drop sequence SEQ_KX_FILE_INFO if exists");
	//		statement.execute("create sequence SEQ_KX_FILE_INFO start with 10001 increment by 1");
	//		//BIGINT > LONG
	//		statement.execute("SET PROPERTY \"sql.enforce_strict_size\" TRUE; " + "create table KX_FILE_INFO(FIL_ID BIGINT, " + "FILE_NAME varchar(50), " + "MIME_TYPE varchar(50), "
	//				+ "LENGTH BIGINT, " + "LAST_MODIFIED date, " + "FILE_DATA LONGVARBINARY, " + "PRIMARY KEY (FIL_ID))");
	//
	//	}

	/**
	 * On vérifie que la liste est vide.
	 */
	@Test
	public void testGetFamille() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final DtList<Famille> dtc = persistenceManager.getBroker().getList(dtDefinitionFamille, null, null);
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
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			DtList<Famille> dtc = persistenceManager.getBroker().getList(dtDefinitionFamille, null, null);
			Assert.assertEquals(0, dtc.size());
			//-----
			final Famille famille = new Famille();
			famille.setLibelle("encore un");
			persistenceManager.getBroker().save(famille);
			// on attend un objet avec un ID non null ?
			Assert.assertNotNull(famille.getFamId());
			//-----
			dtc = persistenceManager.getBroker().getList(dtDefinitionFamille, null, null);
			Assert.assertEquals(1, dtc.size());
			transaction.commit();
		}
	}

	/**
	 * on vérifier l'exception levée si une contrainte bdd n'est pas respecté.
	 */
	@Test(expected = Exception.class)
	public void testCreateFamilleFail() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
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
			persistenceManager.getBroker().save(famille);
			Assert.fail();
		}
	}

	@Test
	public void testCreateFile() throws Exception {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final KFile kFile;
			//1.Création du fichier depuis un fichier texte du FS

			kFile = TestUtil.createKFile(fileManager, "data/lautreamont.txt", AbstractPersistenceManagerTest.class);
			//2. Sauvegarde en BDD
			final FileInfo fileInfo = new FileInfoStd(kFile);
			persistenceManager.getBroker().save(fileInfo);

			//3.relecture du fichier
			final FileInfo readFileInfo = persistenceManager.getBroker().getFileInfo(fileInfo.getURI());

			//4. comparaison du fichier créé et du fichier lu.

			final String source;
			try (final OutputStream sourceOS = new java.io.ByteArrayOutputStream()) {
				FileUtil.copy(kFile.createInputStream(), sourceOS);
				source = sourceOS.toString();
			}

			final String read;
			try (final OutputStream readOS = new java.io.ByteArrayOutputStream()) {
				FileUtil.copy(readFileInfo.getKFile().createInputStream(), readOS);
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

	private static String secureSubString(final String read, final int index, final String searchString) {
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
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on crée une famille
			final Famille famille = new Famille();
			famille.setLibelle("Ma famille");
			persistenceManager.getBroker().save(famille);

			//on récupère la liste des voitures
			final DtDefinition dtDefinitionCar = DtObjectUtil.findDtDefinition(Car.class);
			final DtList<Car> cars = persistenceManager.getBroker().getList(dtDefinitionCar, null, null);
			Assert.assertNotNull(cars);
			Assert.assertFalse("La liste des cars est vide", cars.isEmpty());

			//on associe la liste de voiture à la famille en NN
			final List<URI<? extends DtObject>> carUriList = new ArrayList<>();
			for (final Car car : cars) {
				carUriList.add(new URI<Car>(dtDefinitionCar, car.getId()));
			}
			persistenceManager.getBrokerNN().updateNN(famille.getVoituresLocationDtListURI(), carUriList);

			//On garde le résultat de l'association NN
			final DtList<Car> firstResult = famille.getVoituresLocationList();
			Assert.assertEquals("Test tailles du nombre de voiture dans une NN", cars.size(), firstResult.size());

			//On met à jour l'association en retirant le premier élément
			carUriList.remove(0);
			persistenceManager.getBrokerNN().updateNN(famille.getVoituresLocationDtListURI(), carUriList);

			//on garde le résultat en lazy : il doit avoir le meme nombre de voiture qu'au début
			final DtList<Car> lazyResult = famille.getVoituresLocationList();
			Assert.assertEquals("Test tailles du nombre de voiture pour une NN", firstResult.size(), lazyResult.size());

			//on recharge la famille et on recharge la liste issus de l'association NN : il doit avoir une voiture de moins qu'au début
			final DtDefinition dtFamille = DtObjectUtil.findDtDefinition(Famille.class);
			final Famille famille2 = persistenceManager.getBroker().get(new URI<Famille>(dtFamille, famille.getFamId()));
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
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on crée une famille
			final Famille famille = new Famille();
			famille.setLibelle("Ma famille");
			persistenceManager.getBroker().save(famille);

			//on récupère la liste des voitures
			final DtDefinition dtDefinitionCar = DtObjectUtil.findDtDefinition(Car.class);
			final DtList<Car> cars = persistenceManager.getBroker().getList(dtDefinitionCar, null, null);
			Assert.assertNotNull(cars);
			Assert.assertFalse("La liste des cars est vide", cars.isEmpty());

			//on associe la liste de voiture à la famille en 1N
			for (final Car car : cars) {
				car.setFamId(famille.getFamId());
				persistenceManager.getBroker().save(car);
			}

			//On garde le résultat de l'association 1N
			final DtList<Car> firstResult = famille.getVoituresFamilleList();

			//On met à jour l'association en retirant le premier élément
			final Car firstCar = cars.get(0);
			firstCar.setFamId(null);
			persistenceManager.getBroker().save(firstCar);

			//on garde le résultat en lazy : il doit avoir le meme nombre de voiture qu'au début
			final DtList<Car> lazyResult = famille.getVoituresFamilleList();
			Assert.assertEquals("Test tailles du nombre de voiture pour une 1-N", firstResult.size(), lazyResult.size());

			//on recharge la famille et on recharge la liste issus de l'association 1N : il doit avoir une voiture de moins qu'au début
			final DtDefinition dtFamille = DtObjectUtil.findDtDefinition(Famille.class);
			final Famille famille2 = persistenceManager.getBroker().get(new URI<Famille>(dtFamille, famille.getFamId()));
			final DtList<Car> secondResult = famille2.getVoituresFamilleList();
			Assert.assertEquals("Test tailles du nombre de voiture pour une 1-N", firstResult.size() - 1, secondResult.size());
			transaction.commit();
		}
	}

	@Test
	public void testTxCrudSelectRollback() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			final DtDefinition dtDefinitionCar = DtObjectUtil.findDtDefinition(Car.class);
			final DtList<Car> cars = persistenceManager.getBroker().getList(dtDefinitionCar, null, null);
			Assert.assertNotNull(cars);
			Assert.assertEquals("Test tailles du nombre de voiture pour une N", initialDbCarSize, cars.size());
		}
	}

	@Test
	public void testTxNativeSelectRollback() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			final DtList<Car> cars = nativeLoadCarList();
			Assert.assertNotNull(cars);
			Assert.assertEquals("Test tailles du nombre de voiture pour une N", initialDbCarSize, cars.size());
		}
	}

	@Test
	public void testTxCrudInsertCrudSelectRollback() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar(null);
			persistenceManager.getBroker().save(car);

			//on récupère la liste des voitures
			final DtDefinition dtDefinitionCar = DtObjectUtil.findDtDefinition(Car.class);
			final DtList<Car> cars = persistenceManager.getBroker().getList(dtDefinitionCar, null, null);
			Assert.assertNotNull(cars);
			Assert.assertEquals("Test tailles du nombre de voiture pour une N", initialDbCarSize + 1, cars.size());
		}
	}

	@Test
	public void testTxNativeInsertCrudSelectRollback() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar(null);
			nativeInsertCar(car);

			//on récupère la liste des voitures
			final DtDefinition dtDefinitionCar = DtObjectUtil.findDtDefinition(Car.class);
			final DtList<Car> cars = persistenceManager.getBroker().getList(dtDefinitionCar, null, null);
			Assert.assertNotNull(cars);
			Assert.assertEquals("Test tailles du nombre de voiture pour une N", initialDbCarSize + 1, cars.size());
		}
	}

	@Test
	public void testTxCrudInsertNativeSelectRollback() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar(null);
			persistenceManager.getBroker().save(car);

			//on récupère la liste des voitures
			final DtList<Car> cars = nativeLoadCarList();
			Assert.assertNotNull(cars);
			Assert.assertEquals("Test tailles du nombre de voiture pour une N", initialDbCarSize + 1, cars.size());
		}
	}

	@Test
	public void testTxNativeInsertNativeSelectRollback() {
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar(null);
			nativeInsertCar(car);

			//on récupère la liste des voitures
			final DtList<Car> cars = nativeLoadCarList();
			Assert.assertNotNull(cars);
			Assert.assertEquals("Test tailles du nombre de voiture pour une N", initialDbCarSize + 1, cars.size());
		}
	}

	@Test
	public void testTxCrudInsertRollbackCrudSelectRollback() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar(null);
			persistenceManager.getBroker().save(car);
		}
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			final DtDefinition dtDefinitionCar = DtObjectUtil.findDtDefinition(Car.class);
			final DtList<Car> cars = persistenceManager.getBroker().getList(dtDefinitionCar, null, null);
			Assert.assertNotNull(cars);
			Assert.assertEquals("Test tailles du nombre de voiture pour une N", initialDbCarSize, cars.size());
		}
	}

	@Test
	public void testTxNativeInsertRollbackCrudSelectRollback() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar(null);
			nativeInsertCar(car);
		}
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			final DtDefinition dtDefinitionCar = DtObjectUtil.findDtDefinition(Car.class);
			final DtList<Car> cars = persistenceManager.getBroker().getList(dtDefinitionCar, null, null);
			Assert.assertNotNull(cars);
			Assert.assertEquals("Test tailles du nombre de voiture pour une N", initialDbCarSize, cars.size());
		}
	}

	@Test
	public void testTxCrudInsertRollbackNativeSelectRollback() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar(null);
			persistenceManager.getBroker().save(car);
		}
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			final DtList<Car> cars = nativeLoadCarList();
			Assert.assertNotNull(cars);
			Assert.assertEquals("Test tailles du nombre de voiture pour une N", initialDbCarSize, cars.size());
		}
	}

	@Test
	public void testTxNativeInsertRollbackNativeSelectRollback() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar(null);
			nativeInsertCar(car);
		}
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			final DtList<Car> cars = nativeLoadCarList();
			Assert.assertNotNull(cars);
			Assert.assertEquals("Test tailles du nombre de voiture pour une N", initialDbCarSize, cars.size());
		}
	}

	@Test
	public void testTxNativeInsertCrudInsertCommit() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar(null);
			final Car car2 = createNewCar(null);
			nativeInsertCar(car2);
			persistenceManager.getBroker().save(car);
			transaction.commit();
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testTxCrudInsertTwoCommit() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar(null);
			persistenceManager.getBroker().save(car);
			transaction.commit();
			transaction.commit();
		}
	}

	@Test
	public void testTxCrudInsertCommitCrudSelectRollback() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar(null);
			persistenceManager.getBroker().save(car);
			transaction.commit();
		}
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			final DtDefinition dtDefinitionCar = DtObjectUtil.findDtDefinition(Car.class);
			final DtList<Car> cars = persistenceManager.getBroker().getList(dtDefinitionCar, null, null);
			Assert.assertNotNull(cars);
			Assert.assertEquals("Test tailles du nombre de voiture pour une N", initialDbCarSize + 1, cars.size());
		}
	}

	@Test
	public void testTxNativeInsertCommitCrudSelectRollback() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar(null);
			nativeInsertCar(car);
			transaction.commit();
		}
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			final DtDefinition dtDefinitionCar = DtObjectUtil.findDtDefinition(Car.class);
			final DtList<Car> cars = persistenceManager.getBroker().getList(dtDefinitionCar, null, null);
			Assert.assertNotNull(cars);
			Assert.assertEquals("Test tailles du nombre de voiture pour une N", initialDbCarSize + 1, cars.size());
		}
	}

	@Test
	public void testTxCrudInsertCommitNativeSelectRollback() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar(null);
			persistenceManager.getBroker().save(car);
			transaction.commit();
		}
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			final DtList<Car> cars = nativeLoadCarList();
			Assert.assertNotNull(cars);
			Assert.assertEquals("Test tailles du nombre de voiture pour une N", initialDbCarSize + 1, cars.size());
		}
	}

	@Test
	public void testTxNativeInsertCommitNativeSelectRollback() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Car car = createNewCar(null);
			nativeInsertCar(car);
			transaction.commit();
		}
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			//on récupère la liste des voitures
			final DtList<Car> cars = nativeLoadCarList();
			Assert.assertNotNull(cars);
			Assert.assertEquals("Test tailles du nombre de voiture pour une N", initialDbCarSize + 1, cars.size());
		}
	}

	@Test(expected = NullPointerException.class)
	public void testCrudInsertNoTx() {
		final Car car = createNewCar(null);
		persistenceManager.getBroker().save(car);
	}

	@Test(expected = NullPointerException.class)
	public void testNativeInsertNoTx() {
		final Car car = createNewCar(null);
		nativeInsertCar(car);
	}

	@Test(expected = NullPointerException.class)
	public void testCrudSelectNoTx() {
		final DtDefinition dtDefinitionCar = DtObjectUtil.findDtDefinition(Car.class);
		final DtList<Car> cars = persistenceManager.getBroker().getList(dtDefinitionCar, null, null);
		Assert.assertNotNull(cars);
		Assert.assertEquals("Test tailles du nombre de voiture pour une N", initialDbCarSize, cars.size());
	}

	@Test(expected = NullPointerException.class)
	public void testNativeSelectNoTx() {
		final DtList<Car> cars = nativeLoadCarList();
		Assert.assertNotNull(cars);
		Assert.assertEquals("Test tailles du nombre de voiture pour une N", initialDbCarSize, cars.size());
	}

	@Test
	public void testPerfCrudInsertCrudSelectRollback() {
		final long start = System.currentTimeMillis();
		int execCount = 0;
		while (System.currentTimeMillis() - start < 5000) {
			testTxCrudInsertCrudSelectRollback();
			execCount++;
		}
		final long time = System.currentTimeMillis() - start;
		System.out.println(execCount + " exec en 5s. moy=" + time * 1000 / execCount / 1000d + "ms");
	}

	@Test
	public void testPerfNativeInsertNativeSelectRollback() {
		final long start = System.currentTimeMillis();
		int execCount = 0;
		while (System.currentTimeMillis() - start < 5000) {
			testTxNativeInsertNativeSelectRollback();
			execCount++;
		}
		final long time = System.currentTimeMillis() - start;
		System.out.println(execCount + " exec en 5s. moy=" + time * 1000 / execCount / 1000d + "ms");
	}

	private static Car createNewCar(final Long id) {
		final Car car = new Car();
		car.setId(id);
		car.setPrice(5600);
		car.setMake("Peugeot");
		car.setModel("407");
		car.setYear(2014);
		car.setKilo(20000);
		car.setMotorType("essence".toLowerCase());
		car.setDescription("Vds 407 de test, 2014, 20000 kms, rouge, TBEG");
		return car;
	}
	//
	//	public void createSequence Datas() throws Exception {
	//		final KConnection connection = dataBaseManager.getConnectionProviderPlugin().obtainConnection();
	//		try {
	//			execCallableStatement(connection, "create sequence SEQ_FAMILLE start with 10001 increment by 1");
	//			execCallableStatement(connection, "insert into famille values (SEQ_FAMILLE.nextval, 'Aizoaceae')");
	//			execCallableStatement(connection, "insert into famille values (SEQ_FAMILLE.nextval, 'Balsaminaceae')");
	//		} finally {
	//			connection.commit();
	//		}
	//	}
}
