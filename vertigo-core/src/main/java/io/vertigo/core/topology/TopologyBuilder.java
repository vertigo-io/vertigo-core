package io.vertigo.core.topology;

import io.vertigo.core.lang.Builder;

import java.util.ArrayList;
import java.util.List;

public final class TopologyBuilder implements Builder<Topology>{
	private final List<Node> nodes = new ArrayList<>();

	/** {@inheritDoc} */
	public Topology build() {
		return new Topology(nodes);
	}

}
