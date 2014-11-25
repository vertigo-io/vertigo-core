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
package io.vertigo.studio.plugins.mda.file;

import io.vertigo.core.Home;
import io.vertigo.dynamo.file.metamodel.FileInfoDefinition;
import io.vertigo.lang.Assertion;
import io.vertigo.studio.mda.Result;
import io.vertigo.studio.plugins.mda.AbstractGeneratorPlugin;
import io.vertigo.util.MapBuilder;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Génération des objets relatifs au module File.
 *
 * @author npiedeloup
 */
public final class FileInfoGeneratorPlugin extends AbstractGeneratorPlugin<FileInfoConfiguration> {
	/** {@inheritDoc} */
	@Override
	public FileInfoConfiguration createConfiguration(final Properties properties) {
		return new FileInfoConfiguration(properties);
	}

	/** {@inheritDoc} */
	@Override
	public void generate(final FileInfoConfiguration fileInfoConfiguration, final Result result) {
		Assertion.checkNotNull(fileInfoConfiguration);
		Assertion.checkNotNull(result);
		// ---------------------------------------------------------------------
		/* Générations des FI. */
		generateFileInfos(fileInfoConfiguration, result);
	}

	private static void generateFileInfos(final FileInfoConfiguration fileInfoConfiguration, final Result result) {
		final Collection<FileInfoDefinition> fileInfoDefinitions = Home.getDefinitionSpace().getAll(FileInfoDefinition.class);
		for (final FileInfoDefinition fileInfoDefinition : fileInfoDefinitions) {
			generateFileInfo(fileInfoConfiguration, result, fileInfoDefinition);
		}
	}

	private static void generateFileInfo(final FileInfoConfiguration fileInfoConfiguration, final Result result, final FileInfoDefinition fileInfoDefinition) {
		final TemplateFileInfoDefinition definition = new TemplateFileInfoDefinition(fileInfoDefinition);

		final Map<String, Object> mapRoot = new MapBuilder<String, Object>()
				.put("fiDefinition", definition)
				.put("packageName", fileInfoConfiguration.getFilePackage())
				.build();

		createFileGenerator(fileInfoConfiguration, mapRoot, definition.getClassSimpleName(), fileInfoConfiguration.getFilePackage(), ".java", "fileInfo.ftl")
				.generateFile(result, true);
	}
}
