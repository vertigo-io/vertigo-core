package io.vertigo.commons.cache.redis;

import io.vertigo.commons.cache.AbstractCacheManagerTest;

/**
 * RedisCache Manager test class.
 * Uses RedisConnector from {@link io.vertigo.commons.impl.connectors.redis.RedisConnector}
 *
 * @author pchretien, dszniten
 */
public class RedisCacheManagerTest extends AbstractCacheManagerTest {
	// Unit tests use abstract class methods

	/**
	 * Max nbRows to 500.
	 */
	public RedisCacheManagerTest() {
		super(500);
	}
}
