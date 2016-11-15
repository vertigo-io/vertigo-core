package io.vertigo.dynamo.store.datastore.criteria2;

import static io.vertigo.dynamo.store.data.domain.car.Car.CarFields.MAKE;
import static io.vertigo.dynamo.store.data.domain.car.Car.CarFields.MOTOR_TYPE;
import static io.vertigo.dynamo.store.data.domain.car.Car.CarFields.YEAR;

import org.junit.jupiter.api.Test;

import io.vertigo.dynamo.store.criteria2.CriteriaBool;
import io.vertigo.dynamo.store.data.domain.car.Car;

public interface ICriteriaTest {
	void assertCriteria(final long expected, final CriteriaBool<Car> criteriaBool);

	@Test
	default void testIsEqualTo() {
		final CriteriaBool<Car> criteriaBool = MAKE.isEqualTo("Peugeot");
		assertCriteria(4, criteriaBool);
	}

	@Test
	default void testAnd() {
		final CriteriaBool<Car> criteriaBool = MAKE.isEqualTo("Peugeot")
				.and(MOTOR_TYPE.isEqualTo("diesel"));
		assertCriteria(3, criteriaBool);
	}

	@Test
	default void testOr() {
		final CriteriaBool<Car> criteriaBool = MAKE.isEqualTo("Peugeot")
				.or(MAKE.isEqualTo("Volkswagen"));
		assertCriteria(6, criteriaBool);
	}

	@Test
	default void testAndOr() {
		final CriteriaBool<Car> criteriaBool = MAKE.isEqualTo("Peugeot")
				.and(YEAR.isGreaterThan(2000))
				.or(MAKE.isEqualTo("Volkswagen"));
		assertCriteria((4 - 2) + 2, criteriaBool);
	}

	@Test
	default void testOrAnd() {
		final CriteriaBool<Car> criteriaBool = MAKE.isEqualTo("Peugeot")
				.or(YEAR.isGreaterThan(2008))
				.and(MAKE.isEqualTo("Volkswagen"));
		assertCriteria(4 + (2 - 1), criteriaBool);
	}

	@Test
	default void testAndOrAnd() {
		final CriteriaBool<Car> criteriaBool = MAKE.isEqualTo("Peugeot")
				.and(YEAR.isGreaterThan(2000))
				.or(
						YEAR.isGreaterThan(2008)
								.and(MAKE.isEqualTo("Volkswagen")));
		assertCriteria(3, criteriaBool);
	}

	@Test
	default void testOrAndOr() {
		final CriteriaBool<Car> criteriaBool = YEAR.isLessThan(1999)
				.or(YEAR.isGreaterThan(2001)
						.and(MAKE.isEqualTo("Peugeot")));
		assertCriteria(2, criteriaBool);
	}

	@Test
	default void testIsNotEqualTo() {
		final CriteriaBool<Car> criteriaBool = MAKE.isNotEqualTo("Peugeot");
		assertCriteria(5, criteriaBool);
	}

	@Test
	default void testIsNotNull() {
		final CriteriaBool<Car> criteriaBool = MAKE.isNotNull();
		assertCriteria(9, criteriaBool);
	}

	@Test
	default void testIsGreaterThan() {
		final CriteriaBool<Car> criteriaBool = MAKE.isEqualTo("Peugeot")
				.and(YEAR.isGreaterThan(2001));
		assertCriteria(1, criteriaBool);
	}

	@Test
	default void testIsGreaterThanOrEqualTo() {
		final CriteriaBool<Car> criteriaBool = MAKE.isEqualTo("Peugeot")
				.and(YEAR.isGreaterThanOrEqualTo(2001));
		assertCriteria(2, criteriaBool);
	}

	@Test
	default void testIsLessThan() {
		final CriteriaBool<Car> criteriaBool = MAKE.isEqualTo("Peugeot")
				.and(YEAR.isLessThan(2001));
		assertCriteria(2, criteriaBool);
	}

	@Test
	default void testIsLessThanOrEqualTo() {
		final CriteriaBool<Car> criteriaBool = MAKE.isEqualTo("Peugeot")
				.and(YEAR.isLessThanOrEqualTo(2001));
		assertCriteria(3, criteriaBool);
	}

	@Test
	default void testIsBetween() {
		final CriteriaBool<Car> criteriaBool = MAKE.isEqualTo("Peugeot")
				.and(YEAR.isBetween(1999, 2001));
		assertCriteria(2, criteriaBool);
	}

	@Test
	default void testStartsWith() {
		final CriteriaBool<Car> criteriaBool = MAKE.startsWith("P");
		assertCriteria(4, criteriaBool);
	}

}
