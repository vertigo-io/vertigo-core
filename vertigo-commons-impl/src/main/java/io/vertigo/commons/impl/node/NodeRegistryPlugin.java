package io.vertigo.commons.impl.node;

import java.util.List;
import java.util.Optional;

import io.vertigo.commons.node.Node;
import io.vertigo.lang.Plugin;

/**
 * Plugin for storing and querying the node topology of an App.
 * @author mlaroche
 *
 */
public interface NodeRegistryPlugin extends Plugin {

	void register(Node node);

	void unregister(Node node);

	List<Node> getTopology();

	Optional<Node> find(String nodeId);

	void updateStatus(Node node);

}
