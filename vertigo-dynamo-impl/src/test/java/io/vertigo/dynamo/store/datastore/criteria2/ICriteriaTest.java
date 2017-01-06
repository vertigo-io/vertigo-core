package io.vertigo.dynamo.store.datastore.criteria2;

import static io.vertigo.dynamo.store.data.domain.car.Car.CarFields.MAKE;
import static io.vertigo.dynamo.store.data.domain.car.Car.CarFields.MOTOR_TYPE;
import static io.vertigo.dynamo.store.data.domain.car.Car.CarFields.YEAR;

import org.junit.jupiter.api.Test;

import io.vertigo.dynamo.store.criteria2.Criteria2;
import io.vertigo.dynamo.store.criteria2.Criterions;
import io.vertigo.dynamo.store.data.domain.car.Car;

public interface ICriteriaTest {
	void assertCriteria(final long expected, final Criteria2<Car> criteriaBool);

	@Test
	default void testIsEqualTo() {
		final Criteria2<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot");
		assertCriteria(4, criteriaBool);
	}

	@Test
	default void testAnd() {
		final Criteria2<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isEqualTo(MOTOR_TYPE, "diesel"));
		assertCriteria(3, criteriaBool);
	}

	@Test
	default void testOr() {
		final Criteria2<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.or(Criterions.isEqualTo(MAKE, "Volkswagen"));
		assertCriteria(6, criteriaBool);
	}

	@Test
	default void testAndOr() {
		final Criteria2<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isGreaterThan(YEAR, 2000))
				.or(Criterions.isEqualTo(MAKE, "Volkswagen"));
		assertCriteria((4 - 2) + 2, criteriaBool);
	}

	@Test
	default void testOrAnd() {
		final Criteria2<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.or(Criterions.isGreaterThan(YEAR, 2008))
				.and(Criterions.isEqualTo(MAKE, "Volkswagen"));
		assertCriteria(4 + (2 - 1), criteriaBool);
	}

	@Test
	default void testAndOrAnd() {
		final Criteria2<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isGreaterThan(YEAR, 2000))
				.or(
						Criterions.isGreaterThan(YEAR, 2008)
								.and(Criterions.isEqualTo(MAKE, "Volkswagen")));
		assertCriteria(3, criteriaBool);
	}

	@Test
	default void testOrAndOr() {
		final Criteria2<Car> criteriaBool = Criterions.isLessThan(YEAR, 1999)
				.or(Criterions.isGreaterThan(YEAR, 2001)
						.and(Criterions.isEqualTo(MAKE, "Peugeot")));
		assertCriteria(2, criteriaBool);
	}

	@Test
	default void testIsNotEqualTo() {
		final Criteria2<Car> criteriaBool = Criterions.isNotEqualTo(MAKE, "Peugeot");
		assertCriteria(5, criteriaBool);
	}

	@Test
	default void testIsNotNull() {
		final Criteria2<Car> criteriaBool = Criterions.isNotNull(MAKE);
		assertCriteria(9, criteriaBool);
	}

	@Test
	default void testIsGreaterThan() {
		final Criteria2<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isGreaterThan(YEAR, 2001));
		assertCriteria(1, criteriaBool);
	}

	@Test
	default void testIsGreaterThanOrEqualTo() {
		final Criteria2<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isGreaterThanOrEqualTo(YEAR, 2001));
		assertCriteria(2, criteriaBool);
	}

	@Test
	default void testIsLessThan() {
		final Criteria2<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isLessThan(YEAR, 2001));
		assertCriteria(2, criteriaBool);
	}

	@Test
	default void testIsLessThanOrEqualTo() {
		final Criteria2<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isLessThanOrEqualTo(YEAR, 2001));
		assertCriteria(3, criteriaBool);
	}

	@Test
	default void testIsBetween() {
		final Criteria2<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isBetween(YEAR, 1999, 2001));
		assertCriteria(2, criteriaBool);
	}

	@Test
	default void testStartsWith() {
		final Criteria2<Car> criteriaBool = Criterions.startsWith(MAKE, "P");
		assertCriteria(4, criteriaBool);
	}

}
