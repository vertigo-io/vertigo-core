package io.vertigo.dynamo.plugins.work.redis;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.codec.CodecManagerImpl;
import io.vertigo.core.lang.Activeable;
import io.vertigo.core.lang.Option;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.node.Node;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.DateUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Transaction;

/**
 * @author pchretien
 */
public final class RedisDB implements Activeable {
	private static final int timeout = 2000;
	private final JedisPool jedisPool;

	public RedisDB(final String redisHost, final int port, final Option<String> password) {
		Assertion.checkArgNotEmpty(redisHost);
		Assertion.checkNotNull(password);
		//---------------------------------------------------------------------
		final JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
		//jedisPoolConfig.setMaxActive(10);
		if (password.isDefined()) {
			jedisPool = new JedisPool(jedisPoolConfig, redisHost, port, timeout, password.get());
		} else {
			jedisPool = new JedisPool(jedisPoolConfig, redisHost, port, timeout);
		}

		//test
		try (Jedis jedis = jedisPool.getResource()) {
			jedis.ping();
		}
	}

	public void start() {
		//
	}

	public void stop() {
		//see doc :https://github.com/xetorthio/jedis/wiki/Getting-started
		jedisPool.destroy();
	}

	public void reset() {
		try (final Jedis jedis = jedisPool.getResource()) {
			jedis.flushAll();
		}
	}

	private static final CodecManager codecManager = new CodecManagerImpl();

	public void putStart(final String workId) {
		//Todo
	}

	public <WR, W> void putWorkItem(final WorkItem<WR, W> workItem) {
		Assertion.checkNotNull(workItem);
		//---------------------------------------------------------------------
		try (Jedis jedis = jedisPool.getResource()) {
			//out.println("creating work [" + workId + "] : " + work.getClass().getSimpleName());

			final Map<String, String> datas = new HashMap<>();
			datas.put("work64", encode(workItem.getWork()));
			datas.put("provider64", encode(workItem.getWorkType()));
			datas.put("x-date", DateUtil.newDate().toString());

			final Transaction tx = jedis.multi();

			tx.hmset("work:" + workItem.getId(), datas);

			//tx.expire("work:" + workId, 70);
			//On publie la demande de travaux
			tx.lpush("works:todo:" + workItem.getWorkType(), workItem.getId());

			tx.exec();
		}
	}

	public <WR, W> WorkItem<WR, W> pollWorkItem(final String workType, final int timeoutInSeconds) {
		Assertion.checkNotNull(workType);
		//---------------------------------------------------------------------
		try (Jedis jedis = jedisPool.getResource()) {
			final String workId = jedis.brpoplpush("works:todo:" + workType, "works:in progress", timeoutInSeconds);
			if (workId == null) {
				return null;
			}
			final Map<String, String> hash = jedis.hgetAll("work:" + workId);
			final W work = (W) decode(hash.get("work64"));
			final String name = (String) decode(hash.get("provider64"));
			final WorkEngineProvider<WR, W> workEngineProvider = new WorkEngineProvider<>(name);
			return new WorkItem<>(workId, work, workEngineProvider);
		}
	}

	public <WR> void putResult(final String workId, final WR result, final Throwable error) {
		Assertion.checkArgNotEmpty(workId);
		Assertion.checkArgument(result == null ^ error == null, "result xor error is null");
		//---------------------------------------------------------------------
		final Map<String, String> datas = new HashMap<>();
		try (Jedis jedis = jedisPool.getResource()) {
			if (error == null) {
				datas.put("result", encode(result));
				datas.put("status", "ok");
			} else {
				datas.put("error", encode(error));
				datas.put("status", "ko");
			}
			final Transaction tx = jedis.multi();
			tx.hmset("work:" + workId, datas);
			tx.lrem("works:in progress", 0, workId);
			tx.lpush("works:done", workId);
			tx.exec();
		}
	}

	public <WR> RedisResult<WR> pollResult(final int waitTimeSeconds) {
		try (final Jedis jedis = jedisPool.getResource()) {
			final String workId = jedis.brpoplpush("works:done", "works:completed", waitTimeSeconds);
			if (workId == null) {
				return null;
			}
			final Map<String, String> hash = jedis.hgetAll("work:" + workId);
			//final boolean succeeded = "ok".equals(hash.get("status"));
			final WR value = (WR) decode(hash.get("result"));
			final Throwable error = (Throwable) decode(jedis.hget("work:" + workId, "error"));
			//et on détruit le work (ou bien on l'archive ???
			jedis.del("work:" + workId);
			return new RedisResult<>(workId, value, error);
		}
	}

	public void registerNode(final Node node) {
		Assertion.checkNotNull(node);
		//---------------------------------------------------------------------
		try (Jedis jedis = jedisPool.getResource()) {
			jedis.lpush("nodes", node.getUID());
			final Map<String, String> hash = new HashMap<>();
			hash.put("id", node.getUID());
			hash.put("active", node.isActive()?"true":"false");
			jedis.hmset("node:" + node.getUID(), hash);
		}
	}

	public List<Node> getNodes() {
		try (Jedis jedis = jedisPool.getResource()) {
			final List<Node> nodes = new ArrayList<>();

			final List<String> nodeIds= jedis.lrange("nodes", -1, -1);
			for (final String nodeId : nodeIds) {
				final Map<String, String> hash = jedis.hgetAll(nodeId);
				nodes.add(new Node(hash.get("id"), Boolean.valueOf(hash.get("active"))));
			}
			return nodes;
		}
	}

	private static String encode(final Object toEncode) {
		return codecManager.getBase64Codec().encode(codecManager.getSerializationCodec().encode((Serializable) toEncode));
	}

	private static Object decode(final String encoded) {
		return codecManager.getSerializationCodec().decode(codecManager.getBase64Codec().decode(encoded));
	}


	/**
	 * @author pchretien
	 */
	public static final class RedisResult<WR> {
		public final String workId;
		public final Throwable error;
		public final WR result;

		private RedisResult(final String workId, final WR result, final Throwable error) {
			Assertion.checkArgNotEmpty(workId);
			Assertion.checkArgument(result == null ^ error == null, "result xor error is null");
			//---------------------------------------------------------------------
			this.workId = workId;
			this.error = error;
			this.result = result;
		}
	}

}
