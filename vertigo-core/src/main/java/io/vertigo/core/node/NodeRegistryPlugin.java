package io.vertigo.core.node;

import java.util.List;
import java.util.Optional;

import io.vertigo.app.App;
import io.vertigo.lang.Plugin;

/**
 * Plugin for storing and querying the node topology of an App.
 * @author mlaroche
 *
 */
public interface NodeRegistryPlugin extends Plugin {

	void register(Node node);

	List<Node> getTopology();

	Optional<Node> find(String nodeName);

	List<Node> locateSkills(String... skills);

	void updateStatus(App node);

}
