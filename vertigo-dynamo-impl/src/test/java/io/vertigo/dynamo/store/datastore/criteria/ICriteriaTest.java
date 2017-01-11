package io.vertigo.dynamo.store.datastore.criteria;

import static io.vertigo.dynamo.store.data.domain.car.Car.CarFields.MAKE;
import static io.vertigo.dynamo.store.data.domain.car.Car.CarFields.MOTOR_TYPE;
import static io.vertigo.dynamo.store.data.domain.car.Car.CarFields.YEAR;

import org.junit.jupiter.api.Test;

import io.vertigo.dynamo.store.criteria.Criteria;
import io.vertigo.dynamo.store.criteria.Criterions;
import io.vertigo.dynamo.store.data.domain.car.Car;

public interface ICriteriaTest {
	void assertCriteria(final long expected, final Criteria<Car> criteriaBool);

	@Test
	default void testIsEqualTo() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot");
		assertCriteria(4, criteriaBool);
	}

	@Test
	default void testAnd() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isEqualTo(MOTOR_TYPE, "diesel"));
		assertCriteria(3, criteriaBool);
	}

	@Test
	default void testOr() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.or(Criterions.isEqualTo(MAKE, "Volkswagen"));
		assertCriteria(6, criteriaBool);
	}

	@Test
	default void testAndOr() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isGreaterThan(YEAR, 2000))
				.or(Criterions.isEqualTo(MAKE, "Volkswagen"));
		assertCriteria((4 - 2) + 2, criteriaBool);
	}

	@Test
	default void testOrAnd() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.or(Criterions.isGreaterThan(YEAR, 2008))
				.and(Criterions.isEqualTo(MAKE, "Volkswagen"));
		assertCriteria(4 + (2 - 1), criteriaBool);
	}

	@Test
	default void testAndOrAnd() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isGreaterThan(YEAR, 2000))
				.or(
						Criterions.isGreaterThan(YEAR, 2008)
								.and(Criterions.isEqualTo(MAKE, "Volkswagen")));
		assertCriteria(3, criteriaBool);
	}

	@Test
	default void testOrAndOr() {
		final Criteria<Car> criteriaBool = Criterions.isLessThan(YEAR, 1999)
				.or(Criterions.isGreaterThan(YEAR, 2001)
						.and(Criterions.isEqualTo(MAKE, "Peugeot")));
		assertCriteria(2, criteriaBool);
	}

	@Test
	default void testIsNotEqualTo() {
		final Criteria<Car> criteriaBool = Criterions.isNotEqualTo(MAKE, "Peugeot");
		assertCriteria(5, criteriaBool);
	}

	@Test
	default void testIsNotNull() {
		final Criteria<Car> criteriaBool = Criterions.isNotNull(MAKE);
		assertCriteria(9, criteriaBool);
	}

	@Test
	default void testIsGreaterThan() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isGreaterThan(YEAR, 2001));
		assertCriteria(1, criteriaBool);
	}

	@Test
	default void testIsGreaterThanOrEqualTo() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isGreaterThanOrEqualTo(YEAR, 2001));
		assertCriteria(2, criteriaBool);
	}

	@Test
	default void testIsLessThan() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isLessThan(YEAR, 2001));
		assertCriteria(2, criteriaBool);
	}

	@Test
	default void testIsLessThanOrEqualTo() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isLessThanOrEqualTo(YEAR, 2001));
		assertCriteria(3, criteriaBool);
	}

	@Test
	default void testIsBetween() {
		final Criteria<Car> criteriaBool = Criterions.isEqualTo(MAKE, "Peugeot")
				.and(Criterions.isBetween(YEAR, 1999, 2001));
		assertCriteria(2, criteriaBool);
	}

	@Test
	default void testStartsWith() {
		final Criteria<Car> criteriaBool = Criterions.startsWith(MAKE, "P");
		assertCriteria(4, criteriaBool);
	}

	@Test
	default void testInString() {
		final Criteria<Car> criteriaBool = Criterions.in(MAKE, "Peugeot", "Volkswagen");
		assertCriteria(4 + 2, criteriaBool);
	}

	@Test
	default void testInNumnber() {
		final Criteria<Car> criteriaBool = Criterions.in(YEAR, 2002, 2006);
		assertCriteria(1 + 2, criteriaBool);
	}

}
