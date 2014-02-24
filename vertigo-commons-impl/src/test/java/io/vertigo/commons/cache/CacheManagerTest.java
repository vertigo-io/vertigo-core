package io.vertigo.commons.cache;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Modifiable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * @author dchallas
 * @version $Id: CacheManagerTest.java,v 1.5 2014/01/20 17:51:47 pchretien Exp $
 */
public final class CacheManagerTest extends AbstractTestCaseJU4 {
	private static final String KEY = "ma cl�";
	private static final String CONTEXT = CacheManagerInitializer.CONTEXT;

	@Inject
	private CacheManager cacheManager;

	/**
	 * 
	 * @throws Exception manager null
	 */
	@Test
	public void testNotNull() throws Exception {
		Assertion.checkNotNull(cacheManager);
	}

	/**
	 * Test sur un element.
	 */
	@Test
	public void testPut1() {
		final Serializable value = new Element();

		cacheManager.put(CONTEXT, KEY, value);
		final Serializable retrieve = cacheManager.get(CONTEXT, KEY);
		//On v�rifie qu'il s'agit du m�me objet. 
		Assert.assertEquals(value, retrieve);
	}

	/**
	 * Supprime un element.
	 */
	@Test
	public void testRemove1() {
		testPut1();
		// v�rification de suppression d'un element
		Assert.assertTrue(cacheManager.remove(CONTEXT, KEY));
		Assert.assertNull(cacheManager.get(CONTEXT, KEY));
	}

	/**
	 * vide compl�tement le cache.
	 */
	@Test
	public void testClear() {
		testPut1();
		cacheManager.clear(CONTEXT);
		Assert.assertNull(cacheManager.get(CONTEXT, KEY));
	}

	/**
	 * vide compl�tement tous les caches.
	 */
	@Test
	public void testClearAll() {
		testPut1();
		cacheManager.clearAll();
		Assert.assertNull(cacheManager.get(CONTEXT, KEY));
	}

	/**
	 * Test sur 10 000 elements.
	 */
	@Test
	public void testPut10k() {
		final int nbRow = 10000;
		for (int i = 0; i < nbRow; i++) {
			final String key = "ma cl�[" + i + "]";
			final Serializable value = new Element();
			cacheManager.put(CONTEXT, key, value);
		}
		//System.out.println("Hit Ratio : " + cacheManager.getDescription().getMainSummaryInfo().getStringValue());

		for (int i = 5000; i < 5500; i++) {
			final String key = "ma cl�[" + i + "]";
			Assert.assertNotNull(cacheManager.get(CONTEXT, key));
		}
		//System.out.println("Hit Ratio : " + cacheManager.getDescription().getMainSummaryInfo().getStringValue());

		for (int i = 0; i < nbRow; i++) {
			final String key = "ma cl�[" + i + "]";
			Assert.assertNotNull(cacheManager.get(CONTEXT, key));
		}
		//	assertEquals(ManagerState.OK, cacheManager.getDescription().getMainSummaryInfo().getValueState());
		//System.out.println("Hit Ratio : " + cacheManager.getDescription().getMainSummaryInfo().getStringValue());

		for (int i = 0; i < nbRow; i++) {
			final String key = "ma cl�[" + i + "]";
			Assert.assertNotNull(cacheManager.get(CONTEXT, key));
		}
		//	assertEquals(ManagerState.OK, cacheManager.getDescription().getMainSummaryInfo().getValueState());
		//System.out.println("Hit Ratio : " + cacheManager.getDescription().getMainSummaryInfo().getStringValue());
		cacheManager.clear(CONTEXT);
	}

	/**
	 * Test sur 10 000 elements.
	 */
	@Test
	public void testPut10kUnmodifiable() {
		final int nbRow = 10000;
		for (int i = 0; i < nbRow; i++) {
			final String key = "ma cl�[" + i + "]";
			final Serializable value = new ElementUnmodifiable();
			cacheManager.put(CONTEXT, key, value);
		}
		//System.out.println("Hit Ratio : " + cacheManager.getDescription().getMainSummaryInfo().getStringValue());

		for (int i = 5000; i < 5500; i++) {
			final String key = "ma cl�[" + i + "]";
			Assert.assertNotNull(cacheManager.get(CONTEXT, key));
		}
		//System.out.println("Hit Ratio : " + cacheManager.getDescription().getMainSummaryInfo().getStringValue());

		for (int i = 0; i < nbRow; i++) {
			final String key = "ma cl�[" + i + "]";
			Assert.assertNotNull(cacheManager.get(CONTEXT, key));
		}
		//	assertEquals(ManagerState.OK, cacheManager.getDescription().getMainSummaryInfo().getValueState());
		//System.out.println("Hit Ratio : " + cacheManager.getDescription().getMainSummaryInfo().getStringValue());

		for (int i = 0; i < nbRow; i++) {
			final String key = "ma cl�[" + i + "]";
			Assert.assertNotNull(cacheManager.get(CONTEXT, key));
		}
		//assertEquals(ManagerState.OK, cacheManager.getDescription().getMainSummaryInfo().getValueState());
		//System.out.println("Hit Ratio : " + cacheManager.getDescription().getMainSummaryInfo().getStringValue());
		cacheManager.clear(CONTEXT);
	}

	/**
	 * Test l'acc�s concurrent au cache.
	 */
	@Test
	public void testConcurrentAccess() {
		final Set<Thread> threadSet = new HashSet<>();
		final long baseTime = System.currentTimeMillis();
		final long deathTime = baseTime + 30 * 1000;
		final int nbRow = 1000;
		final int nbReader = 100;
		final int nbWriter = 10;

		for (int i = 0; i < nbReader; i++) {
			final CacheReader ope = new CacheReader(cacheManager, deathTime, nbRow);
			threadSet.add(new Thread(ope, "Reader_" + i));
		}
		for (int i = 0; i < nbWriter; i++) {
			final CacheWriter ope = new CacheWriter(cacheManager, deathTime, nbRow);
			threadSet.add(new Thread(ope, "Writer_" + i));
		}

		for (final Thread th : threadSet) {
			th.start();
		}
		System.out.println("Start : " + nbReader + " readers et " + nbWriter + " writers, utilisation de " + nbRow + " cl�s, pendant " + (deathTime - baseTime) / 1000 + "s ");

		//on attend la fin
		for (final Thread th : threadSet) {
			try {
				th.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		cacheManager.clear(CONTEXT);
	}

	private class CacheReader implements Runnable {
		private final CacheManager lCacheManager;
		private final long deathTime;
		private final long nbRow;

		public CacheReader(final CacheManager cacheManager, final long deathTime, final long nbRow) {
			lCacheManager = cacheManager;
			this.deathTime = deathTime;
			this.nbRow = nbRow;
		}

		public void run() {
			while (System.currentTimeMillis() < deathTime) {
				final String key = "ma cl�[" + Math.round(Math.random() * nbRow) + "]";
				lCacheManager.get(CONTEXT, key); //on test juste le get
				try {
					Thread.sleep(10); //on rend juste la main
				} catch (final InterruptedException e) {
					//rien					
				}
			}
		}
	}

	private class CacheWriter implements Runnable {
		private final CacheManager lCacheManager;
		private final long deathTime;
		private final long nbRow;

		public CacheWriter(final CacheManager cacheManager, final long deathTime, final long nbRow) {
			lCacheManager = cacheManager;
			this.deathTime = deathTime;
			this.nbRow = nbRow;
		}

		public void run() {
			while (System.currentTimeMillis() < deathTime) {
				final String key = "ma cl�[" + Math.round(Math.random() * nbRow) + "]";
				final Serializable value = new ElementUnmodifiable();
				lCacheManager.put(CONTEXT, key, value);
				try {
					Thread.sleep(10); //on rend juste la main
				} catch (final InterruptedException e) {
					//rien					
				}
			}
		}
	}

	//	/**
	//	 * Test que quand on ferme 
	//	  * @throws KSystemException   si erreur r�cuperation cache
	//	 */
	//	public void testClose() throws KSystemException {
	//		manager.put(CONTEXT, String.valueOf(1), String.valueOf(1));
	//		assertNotNull(manager.get(CONTEXT, String.valueOf(1)));
	//
	//		try {
	//			manager.close();
	//			assertNotNull(manager.get(CONTEXT, String.valueOf(1)));
	//			fail("Cache toujours actif");
	//		} catch (final IllegalStateException e) {
	//			assertEquals("The CacheManager is not alive.", e.getMessage());
	//		}
	//	}

	//-------------------------------------------------------------------------

	static class Element implements Serializable {
		private static final long serialVersionUID = 1L;
		private static long count;
		private final long i;

		Element() {
			i = count++;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj instanceof Element) {
				return ((Element) obj).i == i;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return String.valueOf(i).hashCode();
		}

	}

	private static final class ElementUnmodifiable extends Element implements Modifiable {
		private static final long serialVersionUID = -2196291926056658304L;

		ElementUnmodifiable() {
			super();
		}

		public void makeUnmodifiable() {
			//rien
		}

		public boolean isModifiable() {
			return false;
		}
	}
}
