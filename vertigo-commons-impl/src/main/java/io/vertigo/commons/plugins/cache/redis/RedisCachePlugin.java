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
	 * @param codecManager CodecManager
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
	public void addCache(String context, CacheConfig cacheConfig) {
		Assertion.checkNotNull(cacheConfig);
		Assertion.checkArgNotEmpty(context);
		// Jedis uses "int" values for the TTL parameter. This assertion checks if the given CacheConfig TTL value is compatible.
		Assertion.checkArgument(cacheConfig.getTimeToLiveSeconds() == (int) cacheConfig.getTimeToLiveSeconds(), "Cannot use a TTL value greater than an int");
		//----
		cacheConfigsPerContext.put(context, cacheConfig);
		storeConfigInRedis(context, cacheConfig);

	}

	/** {@inheritDoc} */
	@Override
	public void put(String context, Serializable key, Object value) {
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
			jedis.setex(redisKey, (int) (getCacheConfig(context).getTimeToLiveSeconds()), redisValue);
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object get(String context, Serializable key) {

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
	public boolean remove(String context, Serializable key) {
		final String redisKey = buildRedisKey(context, key); //Assertions on context and key done inside this private method
		try (final Jedis jedis = redisConnector.getResource()) {
			return jedis.del(redisKey) > 0;
		}
	}

	/** {@inheritDoc} */
	@Override
	public void clear(String context) {

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

	private void storeConfigInRedis(String context, CacheConfig cacheConfig) {
		//---
		final Map<String, String> configMap = new MapBuilder<String, String>()
				.put("cacheType", cacheConfig.getCacheType())
				.put("MaxElementsInMemory", Integer.toString(cacheConfig.getMaxElementsInMemory()))
				.put("TTI", Long.toString(cacheConfig.getTimeToIdleSeconds()))
				.put("TTL", Long.toString(cacheConfig.getTimeToLiveSeconds()))
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
	private static String buildRedisKey(String context, Serializable key) {
		Assertion.checkArgNotEmpty(context);
		Assertion.checkNotNull(key);
		//---
		return VERTIGO_CACHE + ":" + context + ":" + keyToString(key);
	}

	/*
	 * Builds a string to represent the key to be set in Redis
	 * redisKey = "vertigo:cache:" + context + key
	 */
	private static String buildPatternFromContext(String context) {
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
			return "s-" + ((String) key).trim();
		} else if (key instanceof Integer) {
			return "i-" + key;
		} else if (key instanceof Long) {
			return "l-" + key;
		}
		throw new IllegalArgumentException(key.toString() + " is not supported as a key type");
	}

	/*
	 * Converts a String to a serializable key
	 */
	private static Serializable stringToSerializableKey(final String strValue) {
		Assertion.checkArgNotEmpty(strValue, "Cannot convert an empty string to a key");
		//---
		if (strValue.startsWith("s-")) {
			return strValue.substring(2);
		} else if (strValue.startsWith("i-")) {
			return Integer.valueOf(strValue.substring(2));
		} else if (strValue.startsWith("l-")) {
			return Long.valueOf(strValue.substring(2));
		}
		throw new IllegalArgumentException(strValue + " Cannot be converted to a Serializable key");
	}

}
