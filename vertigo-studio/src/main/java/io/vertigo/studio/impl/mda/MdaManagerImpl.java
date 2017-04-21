/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.studio.impl.mda;

import java.io.File;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.vertigo.lang.Assertion;
import io.vertigo.studio.mda.MdaManager;
import io.vertigo.studio.mda.MdaResult;
import io.vertigo.studio.mda.MdaResultBuilder;
import io.vertigo.studio.plugins.mda.FileGeneratorConfig;

/**
 * Implements MDA.
 *
 * @author pchretien, dchallas
 */
public final class MdaManagerImpl implements MdaManager {
	private final List<GeneratorPlugin> generatorPlugins;
	private final FileGeneratorConfig fileGeneratorConfig;

	/**
	 * Constructor.
	 * @param generatorPlugins
	 * @param targetGenDir Répertoire des fichiers TOUJOURS générés
	 * @param projectPackageName Racine du projet.
	 * @param encoding Encoding des fichiers générés.
	 */
	@Inject
	public MdaManagerImpl(
			final List<GeneratorPlugin> generatorPlugins,
			@Named("targetGenDir") final String targetGenDir,
			@Named("projectPackageName") final String projectPackageName,
			@Named("encoding") final String encoding) {
		Assertion.checkNotNull(generatorPlugins);
		Assertion.checkArgNotEmpty(targetGenDir);
		Assertion.checkArgNotEmpty(projectPackageName);
		Assertion.checkArgNotEmpty(encoding);
		//-----
		this.generatorPlugins = java.util.Collections.unmodifiableList(generatorPlugins);
		fileGeneratorConfig = new FileGeneratorConfig(targetGenDir, projectPackageName, encoding);
	}

	/** {@inheritDoc} */
	@Override
	public MdaResult generate() {
		//Création d'un objet listant les résultats
		final MdaResultBuilder mdaResultBuilder = MdaResult.builder();
		//Génèration des objets issus de la modélisation
		for (final GeneratorPlugin generatorPlugin : generatorPlugins) {
			generatorPlugin.generate(fileGeneratorConfig, mdaResultBuilder);
		}
		return mdaResultBuilder.build();
	}

	@Override
	public MdaResult clean() {
		final File directory = new File(fileGeneratorConfig.getTargetGenDir());
		Assertion.checkArgument(directory.exists(), "targetGenDir must exist");
		Assertion.checkArgument(directory.isDirectory(), "targetGenDir must be a directory");
		//---
		// We want to final clean the directory
		final MdaResultBuilder mdaResultBuilder = MdaResult.builder();
		deleteFiles(directory, mdaResultBuilder);
		return mdaResultBuilder.build();
	}

	private static boolean deleteDirectory(final File directory, final MdaResultBuilder mdaResultBuilder) {
		deleteFiles(directory, mdaResultBuilder);
		return (directory.delete());
	}

	private static void deleteFiles(final File directory, final MdaResultBuilder mdaResultBuilder) {
		for (final File file : directory.listFiles()) {
			if (file.isDirectory()) {
				deleteDirectory(file, mdaResultBuilder);
			} else {
				file.delete(); // we don't care about real deletion of the file
				mdaResultBuilder.incFileDeleted();
			}
		}
	}
}
