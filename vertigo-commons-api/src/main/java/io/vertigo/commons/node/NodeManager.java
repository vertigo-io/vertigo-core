package io.vertigo.commons.node;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.vertigo.lang.Manager;

/**
 * Node Manager.
 * @author mlaroche
 *
 */
public interface NodeManager extends Manager {

	Optional<Node> find(String nodeId);

	List<Node> locateSkills(String... skills);

	List<Node> getTopology();

	Map<String, String> getStatus();

	Map<String, Object> getStats();

	Map<String, String> getConfig();

}
