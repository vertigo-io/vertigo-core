package io.vertigo.commons.impl.node;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.app.App;
import io.vertigo.app.Home;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.commons.node.Node;
import io.vertigo.commons.node.NodeManager;
import io.vertigo.commons.plugins.node.registry.single.SingleNodeRegistryPlugin;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;

/**
 * Implementation of the NodeManager.
 * @author mlaroche
 *
 */
public final class NodeManagerImpl implements NodeManager, Activeable {

	private final NodeRegistryPlugin nodeRegistryPlugin;
	private final Map<String, NodeInfosPlugin> nodeInfosPluginMap = new HashMap<>();
	private final int heartBeatSeconds;

	@Inject
	public NodeManagerImpl(
			@Named("heartBeatSeconds") final Optional<Integer> heartBeatSecondsOpt,
			final DaemonManager daemonManager,
			final Optional<NodeRegistryPlugin> nodeRegistryPluginOpt,
			final List<NodeInfosPlugin> nodeInfosPlugins) {
		Assertion.checkNotNull(heartBeatSecondsOpt);
		Assertion.checkNotNull(daemonManager);
		Assertion.checkNotNull(nodeRegistryPluginOpt);
		// ---
		heartBeatSeconds = heartBeatSecondsOpt.orElse(5);
		nodeRegistryPlugin = nodeRegistryPluginOpt.orElse(new SingleNodeRegistryPlugin());
		nodeInfosPlugins
				.forEach(plugin -> {
					Assertion.checkState(!nodeInfosPluginMap.containsKey(plugin.getProtocol()), "A plugin for the protocol {0} is already registered", plugin.getProtocol());
					//---
					nodeInfosPluginMap.put(plugin.getProtocol(), plugin);
				});

		// register a daemon
		daemonManager.registerDaemon("DMN_UPDATE_NODE_STATUS", () -> () -> nodeRegistryPlugin.updateStatus(toAppNode(Home.getApp())), heartBeatSeconds);
	}

	@Override
	public void start() {
		nodeRegistryPlugin.register(toAppNode(Home.getApp()));
	}

	@Override
	public void stop() {
		nodeRegistryPlugin.unregister(toAppNode(Home.getApp()));

	}

	@Override
	public Optional<Node> find(final String nodeId) {
		return nodeRegistryPlugin.find(nodeId);
	}

	@Override
	public List<Node> locateSkills(final String... skills) {
		return getTopology()
				.stream()
				.filter(node -> node.getSkills().containsAll(Arrays.asList(skills)))
				.collect(Collectors.toList());
	}

	@Override
	public List<Node> getTopology() {
		return nodeRegistryPlugin.getTopology();
	}

	@Override
	public Node getCurrentNode() {
		final String currentNodeId = Home.getApp().getConfig().getNodeConfig().getNodeId();
		return find(currentNodeId)
				.orElseThrow(() -> new VSystemException("Current node with '{0}' cannot be found in the registry", currentNodeId));
	}

	@Override
	public List<Node> getDeadNodes() {
		return getTopology()
				.stream()
				// we wait two heartbeat to decide that a node is dead
				.filter(node -> node.getLastTouch().plus(2 * heartBeatSeconds, ChronoUnit.SECONDS).isBefore(Instant.now()))
				.collect(Collectors.toList());
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
						Node::getId,
						app -> functionToApply.apply(app)));

	}

	private NodeInfosPlugin getInfosPlugin(final Node app) {
		Assertion.checkState(nodeInfosPluginMap.containsKey(app.getProtocol()), "No status plugin found for the protocol {0} when reach attempt on {1} ", app.getProtocol(), app.getEndPoint());
		//---
		return nodeInfosPluginMap.get(app.getProtocol());
	}

	private static Node toAppNode(final App app) {
		return new Node(
				app.getConfig().getNodeConfig().getNodeId(),
				app.getConfig().getNodeConfig().getAppName(),
				NodeStatus.UP.name(),
				Instant.now(),
				app.getStart(),
				app.getConfig().getNodeConfig().getEndPoint(),
				getSkills(app));
	}

	private static List<String> getSkills(final App app) {
		return app.getConfig().getModuleConfigs().stream()
				.map(ModuleConfig::getName)
				.collect(Collectors.toList());
	}

	public enum NodeStatus {
		UP, DOWN;

	}

}
