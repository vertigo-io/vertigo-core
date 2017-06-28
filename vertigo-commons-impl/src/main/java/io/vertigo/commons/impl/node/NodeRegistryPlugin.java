package io.vertigo.commons.impl.node;

import java.util.List;
import java.util.Optional;

import io.vertigo.commons.node.Node;
import io.vertigo.core.component.Plugin;

/**
 * Plugin for storing and querying the node topology of an App.
 * @author mlaroche
 *
 */
public interface NodeRegistryPlugin extends Plugin {

	/**
	 * Register a node
	 * @param node the node to register
	 */
	void register(Node node);

	/**
	 * Unregister a node
	 * @param node the node to unregister
	 */
	void unregister(Node node);

	/**
	 * Get the whole topology of the app
	 * @return the list of node of the app
	 */
	List<Node> getTopology();

	/**
	 * Find a node in the topology with the given id
	 * @param nodeId the id to look for
	 * @return an optional Node
	 */
	Optional<Node> find(String nodeId);

	/**
	 * Update the status of a node
	 * @param node the node to update
	 */
	void updateStatus(Node node);

}
