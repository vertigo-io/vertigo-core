/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.definition.loader;

import java.util.List;
import java.util.stream.Stream;

import io.vertigo.app.Home;
import io.vertigo.app.config.DefinitionProvider;
import io.vertigo.app.config.DefinitionProviderConfig;
import io.vertigo.app.config.DefinitionSupplier;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.core.component.di.injector.Injector;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.lang.Assertion;

/**

 * Environnement permettant de charger le Modèle.
 * Le Modèle peut être chargé de multiples façon :
 * - par lecture d'un fichier oom (poweramc),
 * - par lecture des annotations java présentes sur les beans,
 * - par lecture de fichiers ksp regoupés dans un projet kpr,
 * - ....
 *  Ces modes de chargement sont extensibles.
 *
 * @author pchretien
 */
public final class DefinitionLoader {

	public static void injectDefinitions(final DefinitionSpace definitionSpace, final List<ModuleConfig> moduleConfigs) {
		Assertion.checkNotNull(moduleConfigs);
		//-----
		moduleConfigs
				.stream()
				.peek(Assertion::checkNotNull)
				.flatMap(moduleConfig -> provide(definitionSpace, moduleConfig.getDefinitionProviderConfigs()))
				.forEach(supplier -> definitionSpace.registerDefinition(supplier.get(definitionSpace))); //Here all definitions are registered into the definitionSpace
	}

	private static Stream<DefinitionSupplier> provide(final DefinitionSpace definitionSpace, final List<DefinitionProviderConfig> definitionProviderConfigs) {
		return definitionProviderConfigs
				.stream()
				.map(DefinitionLoader::createDefinitionProvider)
				.flatMap(definitionProvider -> definitionProvider.get(definitionSpace).stream());
	}

	private static DefinitionProvider createDefinitionProvider(final DefinitionProviderConfig definitionProviderConfig) {
		final DefinitionProvider instance = Injector.newInstance(definitionProviderConfig.getDefinitionProviderClass(), Home.getApp().getComponentSpace());// attention c'est pourri
		definitionProviderConfig.getDefinitionResourceConfigs().forEach(config -> instance.addDefinitionResourceConfig(config));
		return instance;

	}
}
