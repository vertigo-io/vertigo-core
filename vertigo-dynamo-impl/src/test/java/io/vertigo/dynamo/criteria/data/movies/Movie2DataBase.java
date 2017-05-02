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
package io.vertigo.dynamo.criteria.data.movies;

import java.util.ArrayList;
import java.util.List;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.util.VCollectors;

/**
 * Base de données des voitures.
 *
 *
 * @author pchretien
 */
public final class Movie2DataBase {
	private final List<Movie2> movies = new ArrayList<>();
	//	private long size = 0;

	public void loadDatas() {
		add(1933, "king kong");
		add(1951, "pandora");
		add(1959, "vertigo");
		add(1979, "alien");
		add(1980, "shining");
		add(1984, "amadeus");
		add(1984, "1984");
		add(1984, "terminator");
		add(1985, "porco rosso");
		add(2000, "gladiator");
		add(2014, "interstellar");
		add(2014, "mommy");
		add(null, "ordet");
	}

	private void add(final Integer year, final String title) {
		final Movie2 movie = new Movie2();
		movie.setYear(year);
		movie.setTitle(title);
		movies.add(movie);
		//		size++;
	}

	//	public long size() {
	//		return size;
	//	}

	public final DtList<Movie2> getAllMovies() {
		return movies
				.stream()
				.collect(VCollectors.toDtList(Movie2.class));
	}
	//
	//	public List<Movie2> getCarsByMaker(final String make) {
	//		final List<Movie2> byMakeCars = new ArrayList<>();
	//		for (final Movie2 car : movies) {
	//			if (car.getMake().toLowerCase(Locale.FRENCH).equals(make)) {
	//				byMakeCars.add(car);
	//			}
	//		}
	//		return byMakeCars;
	//	}
	//
	//	public long getCarsBefore(final int year) {
	//		long count = 0;
	//		for (final Movie2 car : movies) {
	//			if (car.getYear() <= year) {
	//				count++;
	//			}
	//		}
	//		return count;
	//	}
	//
	//	public long containsDescription(final String word) {
	//		long count = 0;
	//		for (final Movie2 car : movies) {
	//			if (car.getDescription().toLowerCase(Locale.FRENCH).contains(word)) {
	//				count++;
	//			}
	//		}
	//		return count;
	//	}
}
