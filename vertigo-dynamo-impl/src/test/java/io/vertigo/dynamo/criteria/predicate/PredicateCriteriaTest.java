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
package io.vertigo.dynamo.criteria.predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.dynamo.criteria.AbstractCriteriaTest;
import io.vertigo.dynamo.criteria.Criteria;
import io.vertigo.dynamo.criteria.data.movies.Movie2;
import io.vertigo.dynamo.criteria.data.movies.Movie2DataBase;

/**
 *
 */
@RunWith(JUnitPlatform.class)
public final class PredicateCriteriaTest extends AbstractCriteriaTest {
	private final Movie2DataBase movie2DataBase = new Movie2DataBase();

	private static Predicate<Movie2> predicate(final Criteria<Movie2> criteria) {
		return criteria.toPredicate();
	}

	@Override
	public void assertCriteria(final long expected, final Criteria<Movie2> criteria) {
		final long count = movie2DataBase.getAllMovies()
				.stream()
				.filter(predicate(criteria))
				.count();
		assertEquals(expected, count);
	}

	@Test
	public void test() {
		final long count = movie2DataBase.getAllMovies()
				.stream()
				.filter(movie -> true)
				.count();
		assertEquals(13, count);
	}
}
