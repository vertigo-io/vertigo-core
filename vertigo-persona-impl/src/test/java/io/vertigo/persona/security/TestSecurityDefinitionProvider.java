package io.vertigo.persona.security;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import io.vertigo.app.config.DefinitionProvider;
import io.vertigo.app.config.DefinitionSupplier;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.persona.security.metamodel.Permission;
import io.vertigo.persona.security.metamodel.Role;
import io.vertigo.util.ListBuilder;

public final class TestSecurityDefinitionProvider implements DefinitionProvider {

	@Override
	public List<DefinitionSupplier> get(final DefinitionSpace definitionSpace) {
		return new ListBuilder<Definition>()
				.add(createRole("R_ADMIN"))
				.add(createRole("R_USER"))
				.add(createRole("R_MANAGER"))
				.add(createRole("R_SECRETARY"))
				.build()

				.stream()
				.map(definition -> (DefinitionSupplier) dS -> definition)
				.collect(Collectors.toList());
	}

	private static Role createRole(final String name) {
		final String description = name;
		return new Role(name, description, Collections.<Permission> emptyList());
	}
}
