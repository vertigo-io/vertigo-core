package io.vertigo.core.node;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.app.App;
import io.vertigo.app.Home;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.core.plugins.node.registry.SingleNodeRegistryPlugin;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;

/**
 * Implementation of the NodeManager.
 * @author mlaroche
 *
 */
public final class NodeManagerImpl implements NodeManager, Activeable {

	private final NodeRegistryPlugin nodeRegistryPlugin;
	private final Map<String, NodeInfosPlugin> nodeInfosPluginMap = new HashMap<>();

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	@Inject
	public NodeManagerImpl(
			final Optional<NodeRegistryPlugin> nodeRegistryPluginOpt,
			final List<NodeInfosPlugin> nodeInfosPlugins) {
		Assertion.checkNotNull(nodeRegistryPluginOpt);
		// ---
		nodeRegistryPlugin = nodeRegistryPluginOpt.orElse(new SingleNodeRegistryPlugin());
		nodeInfosPlugins
				.forEach(plugin -> {
					Assertion.checkState(!nodeInfosPluginMap.containsKey(plugin.getProtocol()), "A plugin for the protocol {0} is already registered", plugin.getProtocol());
					//---
					nodeInfosPluginMap.put(plugin.getProtocol(), plugin);
				});
	}

	@Override
	public void start() {
		register(Home.getApp());
		executor.scheduleAtFixedRate(() -> nodeRegistryPlugin.updateStatus(Home.getApp()), 0, 5, TimeUnit.SECONDS);

	}

	private void register(final App app) {
		Assertion.checkNotNull(app);
		//---
		nodeRegistryPlugin.register(toAppNode(app));
	}

	@Override
	public void stop() {
		executor.shutdownNow();

	}

	@Override
	public Optional<Node> find(final String nodeName) {
		return nodeRegistryPlugin.find(nodeName);
	}

	@Override
	public List<Node> locateSkills(final String... skills) {
		return nodeRegistryPlugin.locateSkills(skills);
	}

	@Override
	public Map<String, String> getStatus() {
		return aggregateResults(app -> getInfosPlugin(app).getStatus(app));
	}

	@Override
	public Map<String, Object> getStats() {
		return aggregateResults(app -> getInfosPlugin(app).getStats(app));
	}

	@Override
	public Map<String, String> getConfig() {
		return aggregateResults(app -> getInfosPlugin(app).getConfig(app));
	}

	private <R> Map<String, R> aggregateResults(final Function<Node, R> functionToApply) {
		return nodeRegistryPlugin
				.getTopology()
				.stream()
				.collect(Collectors.toMap(
						Node::getName,
						app -> functionToApply.apply(app)));

	}

	private NodeInfosPlugin getInfosPlugin(final Node app) {
		Assertion.checkState(nodeInfosPluginMap.containsKey(app.getProtocol()), "No status plugin found for the protocol {0} when reach attempt on {1} ", app.getProtocol(), app.getEndPoint());
		//---
		return nodeInfosPluginMap.get(app.getProtocol());
	}

	private static Node toAppNode(final App app) {
		return new Node(
				UUID.randomUUID().toString(),
				app.getConfig().getAppName(),
				"OK",
				Instant.now(),
				Instant.now(),
				"http://localhost:8088",
				getSkills(app));
	}

	private static List<String> getSkills(final App app) {
		return app.getConfig().getModuleConfigs().stream()
				.map(ModuleConfig::getName)
				.collect(Collectors.toList());
	}

}
