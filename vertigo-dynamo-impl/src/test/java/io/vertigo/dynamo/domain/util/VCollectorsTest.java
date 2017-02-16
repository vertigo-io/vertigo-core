package io.vertigo.dynamo.domain.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.database.data.domain.Movie;
import io.vertigo.dynamo.domain.model.DtList;

/**
 * 
 * @author xdurand
 *
 */
public class VCollectorsTest extends AbstractTestCaseJU4 {

	/**
	 * Test du VCollectors.toDtList sur une liste vide
	 */
	@Test
	public void testCollectDtListEmpty() {
		DtList<Movie> emptyDtList = new DtList<>(Movie.class);
		DtList<Movie> listCollected = emptyDtList.stream().collect(VCollectors.toDtList(Movie.class));

		assertNotNull(listCollected);
		assertTrue(listCollected.isEmpty());
		assertEquals(0, listCollected.size());
	}

	private static Movie createMovie(long id, String title) {
		Movie m = new Movie();
		m.setId(id);
		m.setTitle(title);
		return m;
	}

	/**
	 * Test du VCollectors.toDtList sur une liste non vide sans filtrage
	 */
	@Test
	public void testCollectDtList() {
		DtList<Movie> dtList = new DtList<>(Movie.class);
		Movie m1 = createMovie(1, "Title 1");
		Movie m2 = createMovie(2, "Title 2");

		dtList.add(m1);
		dtList.add(m2);

		// @formatter:off
		DtList<Movie> listCollected = dtList.stream()
											.sorted( (mov1, mov2) -> mov1.getId().compareTo(mov2.getId()))
											.collect(VCollectors.toDtList(Movie.class));
		// @formatter:on

		assertNotNull(listCollected);
		assertTrue(listCollected.isEmpty() == false);
		assertEquals(2, listCollected.size());
		assertEquals(listCollected.get(0), m1);
		assertEquals(listCollected.get(1), m2);
		assertEquals(2, dtList.size());
	}

	/**
	 * Test du VCollectors.toDtList sur une liste non vide avec filtrage
	 */
	@Test
	public void testFilterCollectDtList() {
		DtList<Movie> dtList = new DtList<>(Movie.class);
		Movie m1 = createMovie(1, "Title 1");
		Movie m2 = createMovie(2, "Title 2");
		Movie m3 = createMovie(3, "Title 3");

		dtList.add(m1);
		dtList.add(m2);
		dtList.add(m3);

		// @formatter:off
		DtList<Movie> listCollected = dtList.stream()
											.filter( m -> m.getId() % 2 == 0)
											.collect(VCollectors.toDtList(Movie.class));
		// @formatter:on
		assertNotNull(listCollected);
		assertTrue(listCollected.isEmpty() == false);
		assertEquals(1, listCollected.size());
		assertEquals(listCollected.get(0), m2);
		assertEquals(3, dtList.size());
	}

}
