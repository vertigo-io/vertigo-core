/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.impl.node;

import io.vertigo.dynamo.node.Node;
import io.vertigo.dynamo.node.NodeManager;
import io.vertigo.kernel.component.Plugin;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

/**
 * Implémentation de NodeManager, pour l'execution de travaux par des Workers distant.
 * 
 * 
 * @author npiedeloup, pchretien
 * @version $Id: NodeManagerImpl.java,v 1.5 2014/02/27 10:33:55 pchretien Exp $
 */

public final class NodeManagerImpl implements NodeManager {
	private final List<Node> nodes;

	@Inject
	public NodeManagerImpl(final List<NodePlugin> nodePlugins) {
		Assertion.checkNotNull(nodePlugins);
		//---------------------------------------------------------------------
		final List<Node> tmpNodes = new ArrayList<>();
		for (final Plugin plugin : nodePlugins) {
			tmpNodes.addAll(NodePlugin.class.cast(plugin).getNodes());
		}
		this.nodes = Collections.unmodifiableList(tmpNodes);
	}

	/** {@inheritDoc} */
	public List<Node> getNodes() {
		return nodes;
	}
}
