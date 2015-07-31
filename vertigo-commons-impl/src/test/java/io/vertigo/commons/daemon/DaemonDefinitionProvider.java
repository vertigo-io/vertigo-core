package io.vertigo.commons.daemon;

import io.vertigo.commons.daemon.DaemonManagerTest.SimpleDaemon;
import io.vertigo.core.config.DefinitionProvider;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.util.ListBuilder;

import java.util.Iterator;

public final class DaemonDefinitionProvider implements DefinitionProvider {

	@Override
	public Iterator<Definition> iterator() {
		return new ListBuilder<Definition>()
				.add(new DaemonDefinition("DMN_SIMPLE", SimpleDaemon.class, 2))
				.build()
				.iterator();
	}
}
