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
import io.vertigo.studio.plugins.mda.FileConfig;
import io.vertigo.util.MapBuilder;

import java.util.Collection;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Generation des objets relatifs au module Securite.
 *
 * @author pchretien
 */
public final class SecurityGeneratorPlugin extends AbstractGeneratorPlugin {

	private final String targetSubDir;

	/**
	 * Constructeur.
	 * @param targetSubDir Repertoire de generation des fichiers de ce plugin
	 */
	@Inject
	public SecurityGeneratorPlugin(
			@Named("targetSubDir") final String targetSubDir) {
		//-----
		this.targetSubDir = targetSubDir;
	}

	/** {@inheritDoc} */
	@Override
	public void generate(final FileConfig securityConfiguration, final ResultBuilder resultBuilder) {
		Assertion.checkNotNull(securityConfiguration);
		Assertion.checkNotNull(resultBuilder);
		//-----
		generateRole(targetSubDir, securityConfiguration, resultBuilder);
	}

	private static Collection<Role> getRoles() {
		return Home.getApp().getDefinitionSpace().getAll(Role.class);
	}

	private static void generateRole(final String targetSubDir, final FileConfig securityConfig, final ResultBuilder resultBuilder) {
		final Collection<Role> roles = getRoles();
		if (!roles.isEmpty()) {
			//On ne genere aucun fichier si aucun rele.
			//				final Roles2java roles2Java = new Roles2java(packageName, roleList, parameters);

			final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
					.put("roles", roles)
					.put("classSimpleName", "Role")
					.put("packageName", securityConfig.getProjectPackageName() + ".security")
					.build();

			createFileGenerator(securityConfig, mapRoot, "Role", targetSubDir, securityConfig.getProjectPackageName() + ".security", ".java", "security/role.ftl")
					.generateFile(resultBuilder);
		}
	}
}
