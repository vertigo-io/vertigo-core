package io.vertigo.dynamo.plugins.work.redis;

import io.vertigo.dynamo.impl.node.NodePlugin;
import io.vertigo.dynamo.impl.work.worker.local.LocalWorker;
import io.vertigo.dynamo.node.Node;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * NodePlugin 
 * Ce plugin permet d'ex�cuter des travaux en mode distribu�.
 * REDIS est utilis� comme plateforme d'�changes.
 * 
 * @author pchretien
 * $Id: RedisNodePlugin.java,v 1.9 2014/06/26 12:30:08 npiedeloup Exp $
 */
public final class RedisNodePlugin implements NodePlugin, Activeable {
	private final JedisPool jedisPool;
	private final LocalWorker localWorker = new LocalWorker(/*workersCount*/5);
	private final Thread dispatcherThread;
	private final String nodeId;

	@Inject
	public RedisNodePlugin(final @Named("nodeId") String nodeId, final @Named("host") String redisHost) {
		Assertion.checkArgNotEmpty(nodeId);
		Assertion.checkArgNotEmpty(redisHost);
		//---------------------------------------------------------------------
		this.nodeId = nodeId;
		jedisPool = RedisUtil.createJedisPool(redisHost, 6379);
		dispatcherThread = new RedisDispatcherThread(nodeId, jedisPool, localWorker);
		//System.out.println("RedisNodePlugin");
	}

	/** {@inheritDoc} */
	public void start() {
		//System.out.println("start node");

		//On enregistre le node
		register(new Node(nodeId, true));

		localWorker.start();
		dispatcherThread.start();
	}

	/** {@inheritDoc} */
	public void stop() {
		dispatcherThread.interrupt();
		try {
			dispatcherThread.join();
		} catch (final InterruptedException e) {
			//On ne fait rien
		}
		localWorker.stop();
		register(new Node(nodeId, false));
	}

	//------------------------------------
	//------------------------------------
	//------------------------------------
	//------------------------------------

	private final Gson gson = createGson();

	private static Gson createGson() {
		return new GsonBuilder()//
				//.setPrettyPrinting()//
				.create();
	}

	/** {@inheritDoc} */
	public List<Node> getNodes() {
		final List<Node> nodes = new ArrayList<>();
		Jedis jedis = jedisPool.getResource();
		try {
			final List<String> nodeKeys = jedis.lrange("nodes", -1, -1);
			for (final String nodeKey : nodeKeys) {
				final String json = jedis.hget(nodeKey, "json");
				nodes.add(toNode(json));
			}
		} catch (final JedisException e) {
			jedisPool.returnBrokenResource(jedis);
			jedis = null;
		} finally {
			jedisPool.returnResource(jedis);
		}
		return nodes;
	}

	private Node toNode(final String json) {
		return gson.fromJson(json, Node.class);
	}

	private String toJson(final Node node) {
		return gson.toJson(node);
	}

	private void register(final Node node) {
		Assertion.checkNotNull(node);
		//---------------------------------------------------------------------
		Jedis jedis = jedisPool.getResource();
		try {
			jedis.lpush("nodes", node.getUID());
			jedis.hset("node:" + node.getUID(), "json", toJson(node));
		} catch (final JedisException e) {
			jedisPool.returnBrokenResource(jedis);
			jedis = null;
		} finally {
			jedisPool.returnResource(jedis);
		}
	}
}
