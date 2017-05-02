/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.criteria.sql;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.criteria.AbstractCriteriaTest;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.store.data.domain.car.Car;
import io.vertigo.dynamo.store.data.domain.car.CarDataBase;
import io.vertigo.dynamo.store.datastore.SqlUtil;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionWritable;
import io.vertigo.util.ListBuilder;

/**
 *
 */
@RunWith(JUnitPlatform.class)
public final class SqlCriteriaTest extends AbstractCriteriaTest {
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

	@Override
	protected void doSetUp() throws Exception {
		dtDefinitionCar = DtObjectUtil.findDtDefinition(Car.class);
		//allCarsUri = new DtListURIForCriteria<>(dtDefinitionCar, null, null);

		initMainStore();
	}

	@Override
	protected void doTearDown() throws Exception {
		shutDown("TK_SHUT_DOWN", Optional.empty());
	}

	protected void shutDown(final String taskName, final Optional<String> optDataSpace) {
		SqlUtil.execRequests(
				transactionManager,
				taskManager,
				Collections.singletonList("shutdown;"),
				taskName,
				optDataSpace);
	}

	private void initMainStore() {
		//A chaque test on recrée la table famille
		SqlUtil.execRequests(
				transactionManager,
				taskManager,
				getCreateMainStoreRequests(),
				"TK_INIT_MAIN",
				Optional.empty());

		final CarDataBase carDataBase = new CarDataBase();
		carDataBase.loadDatas();
		try (VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			for (final Car car : carDataBase.getAllCars()) {
				car.setId(null);
				storeManager.getDataStore().create(car);
			}
			transaction.commit();
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
	public void assertCriteria(final long expected, final Criteria<Car> criteria) {
		try (VTransactionWritable tx = transactionManager.createCurrentTransaction()) {
			final long count = storeManager.getDataStore().find(dtDefinitionCar, criteria).size();
			Assert.assertEquals(expected, count);
		}
	}

}
