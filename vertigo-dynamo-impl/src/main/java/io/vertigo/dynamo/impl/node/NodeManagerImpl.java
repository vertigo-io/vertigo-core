package io.vertigo.dynamo.impl.node;

import io.vertigo.dynamo.node.Node;
import io.vertigo.dynamo.node.NodeManager;
import io.vertigo.kernel.component.Plugin;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Impl�mentation de NodeManager, pour l'execution de travaux par des Workers distant.
 * 
 * 
 * @author npiedeloup, pchretien
 * @version $Id: NodeManagerImpl.java,v 1.5 2014/02/27 10:33:55 pchretien Exp $
 */

public final class NodeManagerImpl implements NodeManager {
	@Inject
	private List<NodePlugin> nodePlugins;

	/** {@inheritDoc} */
	public List<Node> getNodes() {
		final List<Node> nodes = new ArrayList<>();
		for (final Plugin plugin : nodePlugins) {
			nodes.addAll(NodePlugin.class.cast(plugin).getNodes());
		}
		return nodes;
	}
}
