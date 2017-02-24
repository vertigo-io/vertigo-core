package io.vertigo.persona.security;

import java.util.Collections;
import java.util.List;

import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.persona.security.metamodel.Permission;
import io.vertigo.persona.security.metamodel.Role;
import io.vertigo.util.ListBuilder;

public final class TestSecurityDefinitionProvider extends SimpleDefinitionProvider {

	@Override
	public List<Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return new ListBuilder<Definition>()
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
