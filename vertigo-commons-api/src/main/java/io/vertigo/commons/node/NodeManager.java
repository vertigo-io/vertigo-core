package io.vertigo.commons.node;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.vertigo.commons.health.HealthMeasure;
import io.vertigo.core.component.Manager;

/**
 * Node Manager.
 * @author mlaroche
 *
 */
public interface NodeManager extends Manager {

	/**
	 * Find a node by an id
	 * @param nodeId the id to look for
	 * @return the optional found Node
	 */
	Optional<Node> find(String nodeId);

	/**
	 * Find node with a given skill
	 * @param skills the skills to look for
	 * @return the Node matching the skills
	 */
	List<Node> locateSkills(String... skills);

	/**
	 * Get the whole topology of an app
	 * @return the topology
	 */
	List<Node> getTopology();

	/**
	 * Get the current node
	 * @return the current node
	 */
	Node getCurrentNode();

	/**
	 * List the dead nodes of the app
	 * @return the dead nodes
	 */
	List<Node> getDeadNodes();

	/**
	 * A consolidated view of the cluster (id+status)
	 * @return the overall status
	 */
	Map<String, List<HealthMeasure>> getStatus();

	/**
	 * A consolidated view of the stats of the app
	 * @return the overall stats
	 */
	Map<String, Object> getStats();

	/**
	 * A consolidated view of the config of the app
	 * @return the overall config
	 */
	Map<String, String> getConfig();

}
