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
package io.vertigo.dynamo.store.datastore.criteria;

import static io.vertigo.dynamo.store.data.domain.car.Car.CarFields.MAKE;
import static io.vertigo.dynamo.store.data.domain.car.Car.CarFields.MOTOR_TYPE;
import static io.vertigo.dynamo.store.data.domain.car.Car.CarFields.YEAR;

import org.junit.jupiter.api.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.store.criteria.Criteria;
import io.vertigo.dynamo.store.criteria.Criterions;
import io.vertigo.dynamo.store.data.domain.car.Car;

public abstract class ICriteriaTest extends AbstractTestCaseJU4 {
	abstract public void assertCriteria(final long expected, final Criteria<Car> criteriaBool);

	@Test
	public final void testIsEqualTo() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot");
		assertCriteria(4, criteriaBool);
	}

	@Test
	public final void testAnd() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isEqualTo(MOTOR_TYPE, "diesel"));
		assertCriteria(3, criteriaBool);
	}

	@Test
	public final void testOr() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.or(Criterions.isEqualTo(MAKE, "Volkswagen"));
		assertCriteria(6, criteriaBool);
	}

	@Test
	public final void testAndOr() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isGreaterThan(YEAR, 2000))
				.or(Criterions.isEqualTo(MAKE, "Volkswagen"));
		assertCriteria((4 - 2) + 2, criteriaBool);
	}

	@Test
	public final void testOrAnd() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.or(Criterions.isGreaterThan(YEAR, 2008))
				.and(Criterions.isEqualTo(MAKE, "Volkswagen"));
		assertCriteria(4 + (2 - 1), criteriaBool);
	}

	@Test
	public final void testAndOrAnd() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isGreaterThan(YEAR, 2000))
				.or(
						Criterions.isGreaterThan(YEAR, 2008)
								.and(Criterions.isEqualTo(MAKE, "Volkswagen")));
		assertCriteria(3, criteriaBool);
	}

	@Test
	public final void testOrAndOr() {
		final Criteria<Car> criteriaBool = Criterions.isLessThan(YEAR, 1999)
				.or(Criterions.isGreaterThan(YEAR, 2001)
						.and(Criterions.isEqualTo(MAKE, "Peugeot")));
		assertCriteria(2, criteriaBool);
	}

	@Test
	public final void testIsNotEqualTo() {
		final Criteria<Car> criteriaBool = Criterions.isNotEqualTo(MAKE, "Peugeot");
		assertCriteria(5, criteriaBool);
	}

	@Test
	public final void testIsNull() {
		final Criteria<Car> criteriaBool = Criterions.isNull(MAKE);
		assertCriteria(1, criteriaBool);
	}

	@Test
	public final void testIsNotNull() {
		final Criteria<Car> criteriaBool = Criterions.isNotNull(MAKE);
		assertCriteria(8, criteriaBool);
	}

	@Test
	public final void testIsGreaterThan() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isGreaterThan(YEAR, 2001));
		assertCriteria(1, criteriaBool);
	}

	@Test
	public final void testIsGreaterThanOrEqualTo() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isGreaterThanOrEqualTo(YEAR, 2001));
		assertCriteria(2, criteriaBool);
	}

	@Test
	public final void testIsLessThan() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isLessThan(YEAR, 2001));
		assertCriteria(2, criteriaBool);
	}

	@Test
	public final void testIsLessThanOrEqualTo() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isLessThanOrEqualTo(YEAR, 2001));
		assertCriteria(3, criteriaBool);
	}

	@Test
	public final void testIsBetween() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isBetween(YEAR, 1999, 2001));
		assertCriteria(2, criteriaBool);
	}

	@Test
	public final void testStartsWith() {
		final Criteria<Car> criteriaBool = Criterions.startsWith(MAKE, "P");
		assertCriteria(4, criteriaBool);
	}

	@Test
	public final void testInString() {
		final Criteria<Car> criteriaBool = Criterions.in(MAKE, "Peugeot", "Volkswagen");
		assertCriteria(4 + 2, criteriaBool);
	}

	@Test
	public final void testInNumnber() {
		final Criteria<Car> criteriaBool = Criterions.in(YEAR, 2002, 2006);
		assertCriteria(1 + 2, criteriaBool);
	}

}
