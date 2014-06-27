package io.vertigo.dynamo.impl.node;

import io.vertigo.dynamo.node.Node;
import io.vertigo.kernel.component.Plugin;

import java.util.List;

/**
 * NodePlugin
 * @author pchretien
 */
public interface NodePlugin extends Plugin {
	/**
	 * @return Liste des noeuds
	 */
	List<Node> getNodes();
}
