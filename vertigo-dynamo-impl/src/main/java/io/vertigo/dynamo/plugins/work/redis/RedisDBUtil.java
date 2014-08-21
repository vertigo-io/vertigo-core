package io.vertigo.dynamo.plugins.work.redis;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.codec.CodecManagerImpl;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.kernel.util.DateUtil;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

public final class RedisDBUtil {
	private static final CodecManager codecManager = new CodecManagerImpl();

	static <WR, W> void writeWorkItem(final Jedis jedis, final WorkItem<WR, W> workItem) {
		//out.println("creating work [" + workId + "] : " + work.getClass().getSimpleName());

		final Map<String, String> datas = new HashMap<>();
		datas.put("work64", encode(workItem.getWork()));
		datas.put("provider64", encode(workItem.getWorkEngineProvider().getName()));
		datas.put("date", DateUtil.newDate().toString());

		final Transaction tx = jedis.multi();

		tx.hmset("work:" + workItem.getId(), datas);

		//tx.expire("work:" + workId, 70);
		//On publie la demande de travaux
		tx.lpush("works:todo", workItem.getId());

		tx.exec();
	}

	static <W, WR> WorkItem<WR, W> readWorkItem(final Jedis jedis, final String workId) {
		//		datas.put("work64", RedisUtil.encode(work));
		//		datas.put("provider64", RedisUtil.encode(workEngineProvider.getName()));
		final Transaction tx = jedis.multi();

		final Response<String> swork = tx.hget("work:" + workId, "work64");
		final Response<String> sname = tx.hget("work:" + workId, "provider64");
		tx.exec();

		final W work = (W) decode(swork.get());
		final String name = (String) decode(sname.get());
		final WorkEngineProvider<WR, W> workEngineProvider = new WorkEngineProvider<>(name);
		return new WorkItem<>(work, workEngineProvider);
	}

	static <WR> WR readSuccess(final Jedis jedis, final String workId) {
		return (WR) decode(jedis.hget("work:" + workId, "result"));
	}

	static Throwable readFailure(final Jedis jedis, final String workId) {
		return (Throwable) decode(jedis.hget("work:" + workId, "error"));
	}

	static <WR> void writeSuccess(final Jedis jedis, final String workId, final WR result) {
		final Map<String, String> datas = new HashMap<>();
		datas.put("result", encode(result));
		datas.put("status", "ok");
		exec(jedis, workId, datas);
	}

	static void writeFailure(final Jedis jedis, final String workId, final Throwable t) {
		final Map<String, String> datas = new HashMap<>();
		datas.put("error", encode(t));
		datas.put("status", "ko");
		exec(jedis, workId, datas);
	}

	private static void exec(final Jedis jedis, final String workId, final Map<String, String> datas) {
		final Transaction tx = jedis.multi();
		tx.hmset("work:" + workId, datas);
		tx.lrem("works:in progress", 0, workId);
		tx.lpush("works:done", workId);
		tx.exec();
	}

	private static String encode(final Object toEncode) {
		return codecManager.getBase64Codec().encode(codecManager.getSerializationCodec().encode((Serializable) toEncode));
	}

	private static Object decode(final String encoded) {
		return codecManager.getSerializationCodec().decode(codecManager.getBase64Codec().decode(encoded));
	}
}
