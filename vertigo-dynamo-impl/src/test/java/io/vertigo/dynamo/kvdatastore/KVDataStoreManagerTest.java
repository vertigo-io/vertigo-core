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
package io.vertigo.dynamo.kvdatastore;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionWritable;
import io.vertigo.dynamock.domain.car.Car;
import io.vertigo.kernel.lang.Option;

import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author pchretien
 */
public class KVDataStoreManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private KVDataStoreManager kvDataStoreManager;
	@Inject
	private KTransactionManager transactionManager;

	@Test
	public void testFindPutFind() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction();) {
			Option<Car> search;
			search = kvDataStoreManager.getDataStore().find("1", Car.class);
			Assert.assertTrue(search.isEmpty());
			Car car = new Car();
			car.setModel("205 gti");

			kvDataStoreManager.getDataStore().put("1", car);
			search = kvDataStoreManager.getDataStore().find("1", Car.class);
			Assert.assertTrue(search.isDefined());
			Assert.assertEquals("205 gti", search.get().getModel());

		}
	}

	private static void addCar(List<Car> cars, String model) {
		Car car = new Car();
		car.setModel(model);
		cars.add(car);
	}

	@Test
	public void testFindAllPutFindAll() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			List<Car> cars;
			cars = kvDataStoreManager.getDataStore().findAll(0, null, Car.class);
			Assert.assertTrue(cars.isEmpty());

			addCar(cars, "205 gti");
			addCar(cars, "R4");
			addCar(cars, "R5");
			addCar(cars, "2CV");

			//			for (
			//			kvDataStoreManager.getDataStore().put("1", car);
			//			search = kvDataStoreManager.getDataStore().find("1", Car.class);
			//			Assert.assertTrue(search.isDefined());
			//			Assert.assertEquals("205 gti", search.get().getModel());

		}
	}

	@Test
	public void test2() {
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			Option<Flower> search;
			search = kvDataStoreManager.getDataStore().find("1", Flower.class);
			Assert.assertTrue(search.isEmpty());
			Flower flower = new Flower();
			flower.setName("Hortensia");
			flower.setPrice(10d);
			kvDataStoreManager.getDataStore().put("H1", flower);
			search = kvDataStoreManager.getDataStore().find("H1", Flower.class);
			Assert.assertTrue(search.isDefined());
			Assert.assertEquals("Hortensia", search.get().getName());

			Assert.assertEquals(10d, search.get().getPrice(), 0); //"Price must be excatly 10", 
		}
	}
}
