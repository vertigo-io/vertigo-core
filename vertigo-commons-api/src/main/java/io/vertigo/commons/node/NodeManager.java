package io.vertigo.commons.node;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.vertigo.core.component.Manager;

/**
 * Node Manager.
 * @author mlaroche
 *
 */
public interface NodeManager extends Manager {

	Optional<Node> find(String nodeId);

	List<Node> locateSkills(String... skills);

	List<Node> getTopology();

	Node getCurrentNode();

	List<Node> getDeadNodes();

	Map<String, String> getStatus();

	Map<String, Object> getStats();

	Map<String, String> getConfig();

}
