/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.app.Home;
import io.vertigo.lang.Assertion;
import io.vertigo.persona.security.metamodel.Permission;
import io.vertigo.persona.security.metamodel.Role;
import io.vertigo.studio.impl.mda.GeneratorPlugin;
import io.vertigo.studio.mda.MdaResultBuilder;
import io.vertigo.studio.plugins.mda.FileGenerator;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;
import io.vertigo.util.MapBuilder;

/**
 * Generation des objets relatifs au module Securite.
 *
 * @author pchretien
 */
public final class SecurityGeneratorPlugin implements GeneratorPlugin {

	private final String targetSubDir;

	/**
	 * Constructeur.
	 * @param targetSubDir Repertoire de generation des fichiers de ce plugin
	 */
	@Inject
	public SecurityGeneratorPlugin(@Named("targetSubDir") final String targetSubDir) {
		//-----
		this.targetSubDir = targetSubDir;
	}

	/** {@inheritDoc} */
	@Override
	public void generate(
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder) {
		Assertion.checkNotNull(fileGeneratorConfig);
		Assertion.checkNotNull(mdaResultBuilder);
		//-----
		generateRole(targetSubDir, fileGeneratorConfig, mdaResultBuilder);
		// generate enum associated with operation defined in permission
		generateOperation(targetSubDir, fileGeneratorConfig, mdaResultBuilder);
	}

	private static Collection<Role> getRoles() {
		return Home.getApp().getDefinitionSpace().getAll(Role.class);
	}

	private static Collection<Permission> getPermissions() {
		return Home.getApp().getDefinitionSpace().getAll(Permission.class);
	}

	private static void generateRole(final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		generateDictionnary("Roles", targetSubDir, fileGeneratorConfig, mdaResultBuilder, getRoles());
	}

	private static void generateOperation(final String targetSubDir, final FileGeneratorConfig fileGeneratorConfig, final MdaResultBuilder mdaResultBuilder) {
		final Set<String> operations = new HashSet<>();
		for (final Permission permission : getPermissions()) {
			operations.add(permission.getOperation());
		}
		generateDictionnary("Operations", targetSubDir, fileGeneratorConfig, mdaResultBuilder, operations);
	}

	private static void generateDictionnary(
			final String objectName,
			final String targetSubDir,
			final FileGeneratorConfig fileGeneratorConfig,
			final MdaResultBuilder mdaResultBuilder,
			final Collection<?> values) {
		Assertion.checkArgNotEmpty(objectName);
		Assertion.checkArgument(Character.isUpperCase(objectName.charAt(0)) && !objectName.contains("_"), "Object name ({0}) should be in camelcase and starts with UpperCase", objectName);
		Assertion.checkArgument(objectName.charAt(objectName.length() - 1) == 's', "Object name ({0}) should ends with 's'", objectName);
		//----
		if (!values.isEmpty()) {
			final String lowerCaseObjectName = objectName.toLowerCase(Locale.ROOT);
			final String lowerCaseObjectNameSingle = lowerCaseObjectName.substring(0, objectName.length() - 1);
			final Map<String, Object> model = new MapBuilder<String, Object>()
					.put(lowerCaseObjectName, values)
					.put("classSimpleName", objectName)
					.put("packageName", fileGeneratorConfig.getProjectPackageName() + ".security")
					.build();

			FileGenerator.builder(fileGeneratorConfig)
					.withModel(model)
					.withFileName(objectName + ".java")
					.withGenSubDir(targetSubDir)
					.withPackageName(fileGeneratorConfig.getProjectPackageName() + ".security")
					.withTemplateName("security/template/" + lowerCaseObjectNameSingle + ".ftl")
					.build()
					.generateFile(mdaResultBuilder);

		}
	}
}
