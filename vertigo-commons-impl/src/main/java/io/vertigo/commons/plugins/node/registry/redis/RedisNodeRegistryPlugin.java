package io.vertigo.commons.plugins.node.registry.redis;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import io.vertigo.commons.impl.connectors.redis.RedisConnector;
import io.vertigo.commons.node.Node;
import io.vertigo.commons.node.NodeRegistryPlugin;
import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.JsonExclude;
import io.vertigo.lang.WrappedException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Memory implementation for a single node app.
 * @author mlaroche
 *
 */
public final class RedisNodeRegistryPlugin implements NodeRegistryPlugin {

	private static final String VERTIGO_NODE = "vertigo:node:";
	private static final String VERTIGO_NODES = "vertigo:nodes";
	private final RedisConnector redisConnector;
	private final Gson gson;

	@Inject
	public RedisNodeRegistryPlugin(final RedisConnector redisConnector) {
		Assertion.checkNotNull(redisConnector);
		//---
		this.redisConnector = redisConnector;
		gson = createGson();
	}

	@Override
	public void register(final Node node) {
		try (final Jedis jedis = redisConnector.getResource()) {
			final Boolean isIdUsed = jedis.sismember(VERTIGO_NODES, node.getId());
			Assertion.checkState(!isIdUsed, "A node id must be unique : Id '{0}' is already used ", node.getId());
			// ---
			try (final Transaction tx = jedis.multi()) {
				tx.hset(VERTIGO_NODE + node.getId(), "json", gson.toJson(node));
				tx.sadd(VERTIGO_NODES, node.getId());
				tx.exec();
			} catch (final IOException e) {
				throw WrappedException.wrap(e);
			}
		}
	}

	@Override
	public void unregister(final Node node) {
		try (final Jedis jedis = redisConnector.getResource()) {
			try (final Transaction tx = jedis.multi()) {
				tx.del(VERTIGO_NODE + node.getId());
				tx.srem(VERTIGO_NODES, node.getId());
				tx.exec();
			} catch (final IOException e) {
				throw WrappedException.wrap(e);
			}
		}
	}

	@Override
	public Optional<Node> find(final String nodeId) {
		try (final Jedis jedis = redisConnector.getResource()) {
			final String result = jedis.hget(VERTIGO_NODE + nodeId, "json");
			if (result != null) {
				return Optional.of(gson.fromJson(result, Node.class));
			}
			return Optional.empty();
		}
	}

	@Override
	public void updateStatus(final Node node) {
		try (final Jedis jedis = redisConnector.getResource()) {
			jedis.hset(VERTIGO_NODE + node.getId(), "json", gson.toJson(node));
		}
	}

	@Override
	public List<Node> getTopology() {
		try (final Jedis jedis = redisConnector.getResource()) {
			return jedis.smembers(VERTIGO_NODES)
					.stream()
					.map(nodeId -> jedis.hget(VERTIGO_NODE + nodeId, "json"))
					.map(nodeJson -> gson.fromJson(nodeJson, Node.class))
					.collect(Collectors.toList());

		}

	}

	private static Gson createGson() {
		return new GsonBuilder()
				.setPrettyPrinting()
				.registerTypeAdapter(DefinitionReference.class, new DefinitionReferenceJsonSerializer())
				.registerTypeAdapter(Optional.class, new OptionJsonSerializer())
				.addSerializationExclusionStrategy(new JsonExclusionStrategy())
				.create();
	}

	private static final class DefinitionReferenceJsonSerializer implements JsonSerializer<DefinitionReference> {
		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final DefinitionReference src, final Type typeOfSrc, final JsonSerializationContext context) {
			return context.serialize(src.get().getName());
		}
	}

	private static final class OptionJsonSerializer implements JsonSerializer<Optional> {
		/** {@inheritDoc} */
		@Override
		public JsonElement serialize(final Optional src, final Type typeOfSrc, final JsonSerializationContext context) {
			if (src.isPresent()) {
				return context.serialize(src.get());
			}
			return null; //rien
		}
	}

	private static final class JsonExclusionStrategy implements ExclusionStrategy {
		/** {@inheritDoc} */
		@Override
		public boolean shouldSkipField(final FieldAttributes arg0) {
			return arg0.getAnnotation(JsonExclude.class) != null;
		}

		@Override
		public boolean shouldSkipClass(final Class<?> arg0) {
			return false;
		}
	}

}
