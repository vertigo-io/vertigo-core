package io.vertigo.dynamo.plugins.work.redis;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.codec.CodecManagerImpl;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.node.Node;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.kernel.util.DateUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author pchretien
 * $Id: RedisDispatcherThread.java,v 1.8 2014/02/03 17:28:45 pchretien Exp $
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
			//final String ping = jedis.ping();
			jedis.ping();
			//System.out.println(" ping=" + ping);
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

	private static final Gson gson = createGson();

	private static Gson createGson() {
		return new GsonBuilder()//
				//.setPrettyPrinting()//
				.create();
	}

	private static final CodecManager codecManager = new CodecManagerImpl();

	<WR, W> void writeWorkItem(final WorkItem<WR, W> workItem) {
		try (Jedis jedis = jedisPool.getResource()) {
			//out.println("creating work [" + workId + "] : " + work.getClass().getSimpleName());

			final Map<String, String> datas = new HashMap<>();
			datas.put("id", workItem.getId());
			datas.put("work64", encode(workItem.getWork()));
			datas.put("provider64", encode(workItem.getWorkEngineProvider().getName()));
			datas.put("x-date", DateUtil.newDate().toString());

			final Transaction tx = jedis.multi();

			tx.hmset("work:" + workItem.getId(), datas);

			//tx.expire("work:" + workId, 70);
			//On publie la demande de travaux
			tx.lpush("works:todo", workItem.getId());

			tx.exec();
		}
	}

	private static <W, WR> WorkItem<WR, W> readWorkItem(final Jedis jedis, final String workId) {
		//		datas.put("work64", RedisUtil.encode(work));
		//		datas.put("provider64", RedisUtil.encode(workEngineProvider.getName()));
		final Transaction tx = jedis.multi();
		final Response<String> sid = tx.hget("work:" + workId, "id");
		final Response<String> swork = tx.hget("work:" + workId, "work64");
		final Response<String> sname = tx.hget("work:" + workId, "provider64");
		tx.exec();

		final String id = sid.get();
		final W work = (W) decode(swork.get());
		final String name = (String) decode(sname.get());
		final WorkEngineProvider<WR, W> workEngineProvider = new WorkEngineProvider<>(name);
		return new WorkItem<>(id, work, workEngineProvider);
	}

	<WR, W> WorkItem<WR, W> nextWorkItemTodo(final int timeoutInSeconds) {
		try (Jedis jedis = jedisPool.getResource()) {
			final String workId = jedis.brpoplpush("works:todo", "works:in progress", timeoutInSeconds);
			if (workId != null) {
				return readWorkItem(jedis, workId);
			}
			return null;
		}
	}

	private <WR> WR readSuccess(final Jedis jedis, final String workId) {
		return (WR) decode(jedis.hget("work:" + workId, "result"));
	}

	private Throwable readFailure(final Jedis jedis, final String workId) {
		return (Throwable) decode(jedis.hget("work:" + workId, "error"));
	}

	<WR> void writeResult(final String workId, final boolean succeeded, final WR result, final Throwable error) {
		if (succeeded) {
			Assertion.checkArgument(result != null, "when succeeded,  a result is required");
			Assertion.checkArgument(error == null, "when succeeded, an error is not accepted");
		} else {
			Assertion.checkArgument(error != null, "when failed, an error is required");
			Assertion.checkArgument(result == null, "when failed, a result is not accepted");
		}
		//---------------------------------------------------------------------
		final Map<String, String> datas = new HashMap<>();
		if (succeeded) {
			datas.put("result", encode(result));
			datas.put("status", "ok");
		} else {
			datas.put("error", encode(error));
			datas.put("status", "ko");
		}
		try (Jedis jedis = jedisPool.getResource()) {
			final Transaction tx = jedis.multi();
			tx.hmset("work:" + workId, datas);
			tx.lrem("works:in progress", 0, workId);
			tx.lpush("works:done", workId);
			tx.exec();
		}

	}

	//	<WR> void writeResult(final String workId, final boolean succeeded, final WR result, final Throwable t) {
	//		Assertion.checkArgument(succeeded && result != null, "a result is required");
	//		Assertion.checkArgument(succeeded && t == null, "an error is not accepted when operation has succeeded");
	//		Assertion.checkArgument(!succeeded && t != null, "an error is required");
	//		Assertion.checkArgument(!succeeded && result == null, "a result  is not accepted when operation has failed");
	//		//---------------------------------------------------------------------
	//		try (Jedis jedis = jedisPool.getResource()) {
	//			final Map<String, String> datas = new HashMap<>();
	//			datas.put("result", encode(result));
	//			if (succeeded) {
	//				datas.put("result", encode(result));
	//				datas.put("status", "ok");
	//			} else {
	//				datas.put("error", encode(t));
	//				datas.put("status", "ok");
	//			}
	//			exec(jedis, workId, datas);
	//		}

	private static String encode(final Object toEncode) {
		return codecManager.getBase64Codec().encode(codecManager.getSerializationCodec().encode((Serializable) toEncode));
	}

	private static Object decode(final String encoded) {
		return codecManager.getSerializationCodec().decode(codecManager.getBase64Codec().decode(encoded));
	}

	<WR> WResult<WR> nextResult(final int waitTimeSeconds) {
		try (Jedis jedis = jedisPool.getResource()) {
			final String workId = jedis.brpoplpush("works:done", "works:completed", waitTimeSeconds);
			final WResult<WR> result;
			if (workId == null) {
				result = null;
			} else {
				if ("ok".equals(jedis.hget("work:" + workId, "status"))) {
					result = new WResult<>(workId, this.<WR> readSuccess(jedis, workId), null);
				} else {
					final Throwable t = readFailure(jedis, workId);
					result = new WResult<>(workId, null, t);
				}
				//et on détruit le work (ou bien on l'archive ???
				jedis.del("work:" + workId);
			}
			return result;
		}
	}

	void registerNode(final Node node) {
		Assertion.checkNotNull(node);
		//---------------------------------------------------------------------
		try (Jedis jedis = jedisPool.getResource()) {
			jedis.lpush("nodes", node.getUID());
			jedis.hset("node:" + node.getUID(), "json", toJson(node));
		}
	}

	List<Node> getNodes() {
		try (Jedis jedis = jedisPool.getResource()) {
			final List<Node> nodes = new ArrayList<>();

			final List<String> nodeKeys = jedis.lrange("nodes", -1, -1);
			for (final String nodeKey : nodeKeys) {
				final String json = jedis.hget(nodeKey, "json");
				nodes.add(toNode(json));
			}
			return nodes;
		}
	}

	private static Node toNode(final String json) {
		return gson.fromJson(json, Node.class);
	}

	private static String toJson(final Node node) {
		return gson.toJson(node);
	}
}
