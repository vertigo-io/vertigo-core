package io.vertigo.core.plugins.node.registry.single;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.vertigo.app.Home;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.NodeRegistryPlugin;
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
	public Optional<Node> find(final String nodeName) {
		if (Home.getApp().getConfig().getAppName().equals(nodeName)) {
			return Optional.of(localNode);
		}
		return Optional.empty();
	}

	@Override
	public List<Node> locateSkills(final String... skills) {
		if (localNode != null) {
			final boolean hasSkills = localNode.getSkills().containsAll(Arrays.asList(skills));
			return hasSkills ? Collections.singletonList(localNode) : Collections.emptyList();
		}
		return Collections.emptyList();
	}

	@Override
	public void updateStatus(final Node node) {
		// Nothing

	}

	@Override
	public List<Node> getTopology() {
		if (localNode != null) {
			return Collections.singletonList(localNode);
		}
		return Collections.emptyList();
	}

}
