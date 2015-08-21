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
package io.vertigo.dynamo.plugins.environment.registries.file;

import io.vertigo.core.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.dynamo.plugins.environment.KspProperty;
import io.vertigo.dynamo.plugins.environment.registries.AbstractDynamicRegistryPlugin;
import io.vertigo.lang.Option;

/**
 * @author pchretien
 */
public final class FileDynamicRegistryPlugin extends AbstractDynamicRegistryPlugin {

	/**
	 * Constructeur.
	 */
	public FileDynamicRegistryPlugin() {
		super(FileGrammar.GRAMMAR);
	}

	/** {@inheritDoc} */
	@Override
	public Option<Definition> createDefinition(final DynamicDefinition xdefinition) {
		if (FileGrammar.FILE_INFO_DEFINITION_ENTITY.equals(xdefinition.getEntity())) {
			//Seuls les taches sont gérées.
			final Definition definition = createFileDefinition(xdefinition);
			return Option.some(definition);
		}
		return Option.none();
	}

	private static FileInfoDefinition createFileDefinition(final DynamicDefinition xFileDefinition) {
		final String fileDefinitionName = xFileDefinition.getName();
		final String root = getPropertyValueAsString(xFileDefinition, KspProperty.ROOT);

		return new FileInfoDefinition(fileDefinitionName, root);
	}

}
