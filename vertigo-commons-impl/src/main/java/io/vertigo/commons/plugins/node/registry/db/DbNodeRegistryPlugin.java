package io.vertigo.commons.plugins.node.registry.db;

import java.beans.PropertyVetoException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import io.vertigo.commons.impl.node.NodeRegistryPlugin;
import io.vertigo.commons.node.Node;
import io.vertigo.core.definition.DefinitionReference;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.JsonExclude;
import io.vertigo.lang.WrappedException;

/**
 * Db implementation for multi node management
 * @author mlaroche
 *
 */
public final class DbNodeRegistryPlugin implements NodeRegistryPlugin {

	private final ComboPooledDataSource pooledDataSource = new ComboPooledDataSource();
	private final Gson gson;

	@Inject
	public DbNodeRegistryPlugin(
			@Named("driverClassName") final String driverClassName,
			@Named("jdbcUrl") final String jdbcUrl) {
		Assertion.checkArgNotEmpty(driverClassName);
		Assertion.checkArgNotEmpty(jdbcUrl);
		// ---
		gson = createGson();
		// ---
		//we configure the connection pool
		try {
			//loads the jdbc driver
			pooledDataSource.setDriverClass(driverClassName);
		} catch (final PropertyVetoException e) {
			throw WrappedException.wrap(e, "Can't defined JdbcDriver {0}", driverClassName);
		}
		pooledDataSource.setJdbcUrl(jdbcUrl);

		// we work with only one connection to the db
		pooledDataSource.setInitialPoolSize(1);
		pooledDataSource.setMaxPoolSize(1);
		pooledDataSource.setMinPoolSize(1);

		final String request = "CREATE TABLE IF NOT EXISTS V_NODE(NODE_ID VARCHAR(255) PRIMARY KEY, JSON TEXT)";
		executeCallableSql(request);
	}

	@Override
	public void register(final Node node) {
		Assertion.checkNotNull(node);
		//---
		final String request = "insert into V_NODE(NODE_ID,JSON) values (?,?)";
		executeCallableSql(request, node.getId(), gson.toJson(node));

	}

	@Override
	public void unregister(final Node node) {
		Assertion.checkNotNull(node);
		// ---
		final String request = "delete from V_NODE where NODE_ID = ?";
		executeCallableSql(request, node.getId());
	}

	@Override
	public List<Node> getTopology() {
		final String request = "select NODE_ID, JSON from V_NODE";
		return retrieveNodes(request);

	}

	@Override
	public Optional<Node> find(final String nodeId) {
		Assertion.checkArgNotEmpty(nodeId);
		// ---
		final String request = "select NODE_ID, JSON from V_NODE where NODE_ID = ?";
		final List<Node> result = retrieveNodes(request, nodeId);
		Assertion.checkState(result.size() <= 1, "Loaded two many rows when retrieving node with id : '{0}'", nodeId);

		if (result.size() == 1) {
			return Optional.of(result.get(0));

		}
		return Optional.empty();

	}

	@Override
	public void updateStatus(final Node node) {
		Assertion.checkNotNull(node);
		// ---
		final String request = "update V_NODE set JSON = ? where NODE_ID = ?";
		executeCallableSql(request, gson.toJson(node), node.getId());
	}

	private void executeCallableSql(final String sql, final String... params) {
		try (final Connection connection = obtainConnection()) {
			try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				for (int i = 0; i < params.length; i++) {
					preparedStatement.setObject(i + 1, params[i]);
				}
				preparedStatement.executeUpdate();
			}
			connection.commit();
		} catch (final SQLException e) {
			throw WrappedException.wrap(e);
		}
	}

	private List<Node> retrieveNodes(final String sql, final String... params) {
		try (final Connection connection = obtainConnection()) {
			try (final PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
				for (int i = 0; i < params.length; i++) {
					preparedStatement.setObject(i + 1, params[i]);
				}
				try (final ResultSet result = preparedStatement.executeQuery()) {
					final List<Node> nodes = new ArrayList<>();
					while (result.next()) {
						final String json = result.getString(2);
						nodes.add(gson.fromJson(json, Node.class));
					}
					return nodes;
				}
			}
		} catch (final SQLException e) {
			throw WrappedException.wrap(e);
		}
	}

	private Connection obtainConnection() {
		try {
			return pooledDataSource.getConnection();
		} catch (final SQLException e) {
			throw WrappedException.wrap(e, "Can't open connection");
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
