package io.vertigo.dynamo.domain.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.domain.data.domain.Artist;
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
		DtList<Artist> emptyDtList = new DtList<>(Artist.class);
		DtList<Artist> listCollected = emptyDtList.stream().collect(VCollectors.toDtList(Artist.class));

		assertNotNull(listCollected);
		assertTrue(listCollected.isEmpty());
		assertEquals(0, listCollected.size());
	}

	private static Artist createArtist(long id, String name) {
		Artist m = new Artist();
		m.setId(id);
		m.setName(name);
		return m;
	}

	/**
	 * Test du VCollectors.toDtList sur une liste non vide sans filtrage
	 */
	@Test
	public void testCollectDtList() {
		DtList<Artist> dtList = new DtList<>(Artist.class);
		Artist m1 = createArtist(1, "David Bowie");
		Artist m2 = createArtist(2, "Joe Strummer");

		dtList.add(m1);
		dtList.add(m2);

		// @formatter:off
		DtList<Artist> listCollected = dtList.stream()
											.sorted( (mov1, mov2) -> mov1.getId().compareTo(mov2.getId()))
											.collect(VCollectors.toDtList(Artist.class));
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
		DtList<Artist> dtList = new DtList<>(Artist.class);
		Artist m1 = createArtist(1, "Louis Armstrong");
		Artist m2 = createArtist(2, "Duke Ellington");
		Artist m3 = createArtist(3, "Jimmy Hendricks");

		dtList.add(m1);
		dtList.add(m2);
		dtList.add(m3);

		// @formatter:off
		DtList<Artist> listCollected = dtList.stream()
											.filter( m -> m.getId() % 2 == 0)
											.collect(VCollectors.toDtList(Artist.class));
		// @formatter:on
		assertNotNull(listCollected);
		assertTrue(listCollected.isEmpty() == false);
		assertEquals(1, listCollected.size());
		assertEquals(listCollected.get(0), m2);
		assertEquals(3, dtList.size());
	}

}
