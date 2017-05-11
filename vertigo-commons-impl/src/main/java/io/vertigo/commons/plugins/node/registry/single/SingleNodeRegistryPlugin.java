package io.vertigo.commons.plugins.node.registry.single;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.vertigo.app.Home;
import io.vertigo.commons.impl.node.NodeRegistryPlugin;
import io.vertigo.commons.node.Node;
import io.vertigo.lang.Assertion;

/**
 * Memory implementation for a single node app.
 * @author mlaroche
 *
 */
public final class SingleNodeRegistryPlugin implements NodeRegistryPlugin {

	private Node localNode;

	@Override
	public synchronized void register(final Node node) {
		Assertion.checkState(localNode == null, "SingleNode has already been registered");
		// ---
		Assertion.checkNotNull(node);
		// ---
		localNode = node;

	}

	@Override
	public synchronized void unregister(final Node node) {
		localNode = null;
	}

	@Override
	public Optional<Node> find(final String nodeId) {
		if (Home.getApp().getConfig().getNodeConfig().getNodeId().equals(nodeId)) {
			return Optional.of(localNode);
		}
		return Optional.empty();
	}

	@Override
	public synchronized void updateStatus(final Node node) {
		localNode = node;

	}

	@Override
	public List<Node> getTopology() {
		if (localNode != null) {
			return Collections.singletonList(localNode);
		}
		return Collections.emptyList();
	}

}
