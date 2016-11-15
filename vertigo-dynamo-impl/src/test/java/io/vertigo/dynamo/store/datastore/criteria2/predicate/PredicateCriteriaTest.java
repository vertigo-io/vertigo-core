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
package io.vertigo.dynamo.store.datastore.criteria2.predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.store.criteria2.Criteria2;
import io.vertigo.dynamo.store.criteria2.CriteriaBool;
import io.vertigo.dynamo.store.data.domain.car.Car;
import io.vertigo.dynamo.store.data.domain.car.CarDataBase;
import io.vertigo.dynamo.store.datastore.criteria2.ICriteriaTest;

/**
 *
 */
@RunWith(JUnitPlatform.class)
public final class PredicateCriteriaTest extends AbstractTestCaseJU4 implements ICriteriaTest {
	private final CarDataBase carDataBase = new CarDataBase();

	@BeforeEach
	public void before() {
		carDataBase.loadDatas();
	}

	private static Predicate<Car> predicate(final CriteriaBool<Car> criteriaBool) {
		return new Criteria2(criteriaBool).toPredicate();
	}

	@Override
	public void assertCriteria(final long expected, final CriteriaBool<Car> criteriaBool) {
		final long count = carDataBase.getAllCars()
				.stream()
				.filter(predicate(criteriaBool))
				.count();
		assertEquals(expected, count);
	}

	@Test
	public void test() {
		final long count = carDataBase.getAllCars()
				.stream()
				.filter(car -> true)
				.count();
		assertEquals(9, count);
	}
}
