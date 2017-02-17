package io.vertigo.persona.security;

import java.util.Collections;
import java.util.List;

import io.vertigo.app.config.DefinitionProvider;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.persona.security.metamodel.Permission;
import io.vertigo.persona.security.metamodel.Role;
import io.vertigo.util.ListBuilder;

public final class SecurityDefinitionProvider implements DefinitionProvider {

	@Override
	public List<Definition> get() {
		return new ListBuilder()
				.add(createRole("R_ADMIN"))
				.add(createRole("R_USER"))
				.add(createRole("R_MANAGER"))
				.add(createRole("R_SECRETARY"))
				.build();
	}

	private static Role createRole(final String name) {
		final String description = name;
		return new Role(name, description, Collections.<Permission> emptyList());
	}
}
