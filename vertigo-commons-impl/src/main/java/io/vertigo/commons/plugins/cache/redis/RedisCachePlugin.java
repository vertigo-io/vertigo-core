package io.vertigo.commons.plugins.cache.redis;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.vertigo.commons.cache.CacheConfig;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.cache.CachePlugin;
import io.vertigo.core.connectors.redis.RedisConnector;
import io.vertigo.lang.Assertion;
import io.vertigo.util.MapBuilder;
import redis.clients.jedis.Jedis;

/**
 * RedisCache plugin
 *
 * @author pchretien, dszniten
 */
public class RedisCachePlugin implements CachePlugin {

	private final CodecManager codecManager;
	private final RedisConnector redisConnector;
	private final Map<String, CacheConfig> cacheConfigsPerContext = new HashMap<>();

	private static final String VERTIGO_CACHE = "vertigo:cache";
	private static final String VERTIGO_CACHE_CONFIG = "vertigo:cacheconfig";
	private static final String DELETE_KEYS_ON_PATTERN_SCRIPT = "local keys = redis.call('keys', '%s') for i,k in ipairs(keys) do local res = redis.call('del', k) end";

	/**
	 * Constructor.
	 * @param codecManager  the codecManager
	 * @param redisConnector the redis connector
	 */
	@Inject
	public RedisCachePlugin(
			final CodecManager codecManager,
			final RedisConnector redisConnector) {
		Assertion.checkNotNull(codecManager);
		Assertion.checkNotNull(redisConnector);
		//-----
		this.codecManager = codecManager;
		this.redisConnector = redisConnector;
	}

	/** {@inheritDoc} */
	@Override
	public void addCache(final String context, final CacheConfig cacheConfig) {
		Assertion.checkNotNull(cacheConfig);
		Assertion.checkArgNotEmpty(context);
		//----
		cacheConfigsPerContext.put(context, cacheConfig);
		storeConfigInRedis(context, cacheConfig);

	}

	/** {@inheritDoc} */
	@Override
	public void put(final String context, final Serializable key, final Object value) {
		Assertion.checkNotNull(value, "CachePlugin can't cache null value. (context: {0}, key:{1})", context, key);
		Assertion.checkState(!(value instanceof byte[]), "CachePlugin can't cache byte[] values");
		Assertion.checkArgument(value instanceof Serializable,
				"Object to cache isn't Serializable. Make it unmodifiable or add it in noSerialization's plugin parameter. (context: {0}, key:{1}, class:{2})",
				context, key, value.getClass().getSimpleName());
		//---
		final String redisKey = buildRedisKey(context, key);

		// redisValue = Base64 encoding of the serialized value
		final byte[] serializedObject = codecManager.getCompressedSerializationCodec().encode((Serializable) value);
		final String redisValue = codecManager.getBase64Codec().encode(serializedObject);

		try (final Jedis jedis = redisConnector.getResource()) {
			jedis.setex(redisKey, (getCacheConfig(context).getTimeToLiveSeconds()), redisValue);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object get(final String context, final Serializable key) {
		final String redisKey = buildRedisKey(context, key); //Assertions on context and key done inside this private method
		final String redisValue;

		try (final Jedis jedis = redisConnector.getResource()) {
			redisValue = jedis.get(redisKey);
		}

		if (redisValue == null) {
			return null;
		}
		final byte[] serializedObject = codecManager.getBase64Codec().decode(redisValue);
		return codecManager.getCompressedSerializationCodec().decode(serializedObject);
	}

	/** {@inheritDoc} */
	@Override
	public boolean remove(final String context, final Serializable key) {
		final String redisKey = buildRedisKey(context, key); //Assertions on context and key done inside this private method
		try (final Jedis jedis = redisConnector.getResource()) {
			return jedis.del(redisKey) > 0;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void clear(final String context) {
		final String pattern = buildPatternFromContext(context);
		try (final Jedis jedis = redisConnector.getResource()) {
			jedis.eval(String.format(DELETE_KEYS_ON_PATTERN_SCRIPT, pattern));
		}
	}

	/** {@inheritDoc} */
	@Override
	public void clearAll() {
		try (final Jedis jedis = redisConnector.getResource()) {
			jedis.eval(String.format(DELETE_KEYS_ON_PATTERN_SCRIPT, VERTIGO_CACHE + ":*"));
		}

	}

	private void storeConfigInRedis(final String context, final CacheConfig cacheConfig) {
		//---
		final Map<String, String> configMap = new MapBuilder<String, String>()
				.put("cacheType", cacheConfig.getCacheType())
				.put("MaxElementsInMemory", Integer.toString(cacheConfig.getMaxElementsInMemory()))
				.put("TTI", String.valueOf(cacheConfig.getTimeToIdleSeconds()))
				.put("TTL", String.valueOf(cacheConfig.getTimeToLiveSeconds()))
				.put("shouldSerialize", Boolean.toString(cacheConfig.shouldSerializeElements()))
				.build();

		try (final Jedis jedis = redisConnector.getResource()) {
			jedis.hmset(buildRedisKey(VERTIGO_CACHE_CONFIG, context), configMap);
		}
	}

	private synchronized CacheConfig getCacheConfig(final String context) {
		final CacheConfig cacheConfig = cacheConfigsPerContext.get(context);
		Assertion.checkNotNull(cacheConfig, "Cache {0} are not yet registered.", context);
		return cacheConfig;
	}

	/*
	 * Builds a string to represent the key to be set in Redis
	 * redisKey = "vertigo:cache:" + context + key
	 */
	private static String buildRedisKey(final String context, final Serializable key) {
		Assertion.checkArgNotEmpty(context);
		Assertion.checkNotNull(key);
		//---
		return VERTIGO_CACHE + ":" + context + ":" + keyToString(key);
	}

	/*
	 * Builds a string to represent the key to be set in Redis
	 * redisKey = "vertigo:cache:" + context + key
	 */
	private static String buildPatternFromContext(final String context) {
		Assertion.checkArgNotEmpty(context);
		//---
		return VERTIGO_CACHE + ":" + context + ":*";
	}

	/*
	 * Converts a key into a String.
	 * An empty key is considered as null.
	 */
	private static String keyToString(final Serializable key) {
		Assertion.checkNotNull(key);
		//---
		if (key instanceof String) {
			Assertion.checkArgNotEmpty((String) key, "a key cannot be an empty string");
			//--
			return ((String) key).trim();
		} else if (key instanceof Integer) {
			return key.toString();
		} else if (key instanceof Long) {
			return key.toString();
		}
		throw new IllegalArgumentException(key.toString() + " is not supported as a key type");
	}
}
