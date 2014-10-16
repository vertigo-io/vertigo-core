package io.vertigo.core.topology;

import io.vertigo.core.lang.Assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * A system is compposed with a set of node.
 * @author pchretien
 */
public final class Topology {
	private final List<Node> nodes ;

	Topology(final List<Node> nodes){
		Assertion.checkNotNull(nodes);
		//---------------------------------------------------------------------
		this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
	}

	public List<Node> getNodes() {
		return nodes;
	}
}
