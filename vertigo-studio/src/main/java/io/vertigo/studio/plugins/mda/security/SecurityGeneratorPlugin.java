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
package io.vertigo.studio.plugins.mda.security;

import io.vertigo.core.Home;
import io.vertigo.lang.Assertion;
import io.vertigo.persona.security.metamodel.Role;
import io.vertigo.studio.mda.ResultBuilder;
import io.vertigo.studio.plugins.mda.AbstractGeneratorPlugin;
import io.vertigo.util.MapBuilder;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Generation des objets relatifs au module Securite.
 *
 * @author pchretien
 */
public final class SecurityGeneratorPlugin extends AbstractGeneratorPlugin<SecurityConfiguration> {
	/** {@inheritDoc}  */
	@Override
	public SecurityConfiguration createConfiguration(final Properties properties) {
		return new SecurityConfiguration(properties);
	}

	private static Collection<Role> getRoles() {
		// return Home.getNameSpace().getDefinitions(Role.class);
		return Home.getDefinitionSpace().getAll(Role.class);
	}

	/** {@inheritDoc} */
	@Override
	public void generate(final SecurityConfiguration securityConfiguration, final ResultBuilder resultBuilder) {
		Assertion.checkNotNull(securityConfiguration);
		Assertion.checkNotNull(resultBuilder);
		//---------------------------------------------------------------------
		generateRole(securityConfiguration, resultBuilder);
	}

	private static void generateRole(final SecurityConfiguration securityConfiguration, final ResultBuilder resultBuilder) {
		final Collection<Role> roles = getRoles();
		if (!roles.isEmpty()) {
			//On ne genere aucun fichier si aucun rele.
			//				final Roles2java roles2Java = new Roles2java(packageName, roleList, parameters);

			final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
					.put("roles", roles)
					.put("classSimpleName", "Role")
					.put("packageName", securityConfiguration.getSecurityPackage())
					.build();

			createFileGenerator(securityConfiguration, mapRoot, "Role", securityConfiguration.getSecurityPackage(), ".java", "role.ftl")
					.generateFile(resultBuilder);
		}
	}
}
