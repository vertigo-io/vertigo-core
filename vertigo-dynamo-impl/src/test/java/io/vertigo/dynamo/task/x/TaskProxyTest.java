/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.task.x;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.commons.transaction.VTransactionManager;
import io.vertigo.commons.transaction.VTransactionWritable;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.store.StoreManager;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.data.domain.SuperHero;
import io.vertigo.util.AbstractTestCaseJU4;

/**
 *
 * @author npiedeloup
 */
public final class TaskProxyTest extends AbstractTestCaseJU4 {
	@Inject
	private TaskManager taskManager;
	@Inject
	private StoreManager storeManager;
	@Inject
	private VTransactionManager transactionManager;
	@Inject
	private SuperHeroDao superHeroDao;

	private SuperHeroDataBase superHeroDataBase;

	@Override
	protected void doSetUp() throws Exception {
		superHeroDataBase = new SuperHeroDataBase(transactionManager, taskManager);
		superHeroDataBase.createDataBase();
		superHeroDataBase.populateSuperHero(storeManager, 33);
	}

	/**
	 * Test where in avec 2200 Id a exclure.
	 */
	@Test
	public void testCount() {
		superHeroDataBase.populateSuperHero(storeManager, 100);
		//---
		try (final VTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final int count = superHeroDao.count();
			Assert.assertEquals(100 + 33, count);
		}
	}

	@Test
	public void testCountByName() {
		final int count = superHeroDao.count("SuperHero ( 10)");
		Assert.assertEquals(1, count);
	}

	@Test
	public void testSelectByName() {
		DtList<SuperHero> list;
		list = superHeroDao.findAll(Optional.of("SuperHero ( 10)"));
		Assert.assertEquals(1, list.size());

		list = superHeroDao.findAll(Optional.empty());
		Assert.assertEquals(33, list.size());

		list = superHeroDao.findAll(Optional.of("nada"));
		Assert.assertEquals(0, list.size());
	}

	@Test
	public void testSelectDistinctNames() {
		final List<String> names = superHeroDao.names();
		Assert.assertEquals(33, names.size());
	}

	@Test
	public void testUpdateNames() {
		final String oldName = "SuperHero ( 10)";
		final String newName = "superMan";

		DtList<SuperHero> list;
		list = superHeroDao.findAll(Optional.of(oldName));
		Assert.assertEquals(1, list.size());

		list = superHeroDao.findAll(Optional.of(newName));
		Assert.assertEquals(0, list.size());

		superHeroDao.update(oldName, newName);
		final List<String> names = superHeroDao.names();
		Assert.assertEquals(33, names.size());

		list = superHeroDao.findAll(Optional.of(oldName));
		Assert.assertEquals(0, list.size());

		list = superHeroDao.findAll(Optional.of(newName));
		Assert.assertEquals(1, list.size());
	}
}
