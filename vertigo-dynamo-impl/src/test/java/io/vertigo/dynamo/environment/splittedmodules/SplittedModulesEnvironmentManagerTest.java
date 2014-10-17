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
package io.vertigo.dynamo.environment.splittedmodules;

import io.vertigo.commons.impl.resource.ResourceManagerImpl;
import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.commons.plugins.resource.java.ClassPathResourceResolverPlugin;
import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.core.Home;
import io.vertigo.core.config.ComponentSpaceConfig;
import io.vertigo.core.config.ComponentSpaceConfigBuilder;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.environment.EnvironmentManager;
import io.vertigo.dynamo.impl.environment.EnvironmentManagerImpl;
import io.vertigo.dynamo.plugins.environment.loaders.java.AnnotationLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.KprLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainDynamicRegistryPlugin;
import io.vertigo.dynamock.domain.DtDefinitions;
import io.vertigoimpl.commons.locale.LocaleManagerImpl;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test de l'implémentation standard.
 * 
 * @author npiedeloup
 */
public final class SplittedModulesEnvironmentManagerTest {

	//
	//<module name="test-2"><!--  this moduleReference -->
	//	<resource type ="classes" path="io.vertigo.dynamock.domain.DtDefinitions"/>
	//</module>

	@Test
	public void testFirstModule() {
		// @formatter:off
		final ComponentSpaceConfig componentSpaceConfig = prepareDefaultComponentSpaceConfigBuilder()
			.beginModule("test-1")
				.withResource("kpr", "io/vertigo/dynamock/execution.kpr")
			.endModule()
			.build();
		// @formatter:on

		Home.start(componentSpaceConfig);
		try {
			final Domain doString = Home.getDefinitionSpace().resolve("DO_STRING", Domain.class);
			Assert.assertNotNull(doString);
		} finally {
			Home.stop();
		}
	}

	@Test
	public void testMergedModules() {
		// @formatter:off
		final ComponentSpaceConfig componentSpaceConfig = prepareDefaultComponentSpaceConfigBuilder()
			.beginModule("test-1-2")
				.withResource("kpr", "io/vertigo/dynamock/execution.kpr")
				.withResource("classes", DtDefinitions.class.getCanonicalName())
			.endModule()
			.build();
		// @formatter:on

		Home.start(componentSpaceConfig);
		try {
			final Domain doString = Home.getDefinitionSpace().resolve("DO_STRING", Domain.class);
			Assert.assertNotNull(doString);
			final DtDefinition dtFamille = Home.getDefinitionSpace().resolve("DT_FAMILLE", DtDefinition.class);
			Assert.assertNotNull(dtFamille);
		} finally {
			Home.stop();
		}
	}

	@Test
	public void testSplittedModules() {
		// @formatter:off
		final ComponentSpaceConfig componentSpaceConfig = prepareDefaultComponentSpaceConfigBuilder()
			.beginModule("test-1")
				.withResource("kpr", "io/vertigo/dynamock/execution.kpr")
			.endModule()
			.beginModule("test-2")
				.withResource("classes", DtDefinitions.class.getCanonicalName())
			.endModule()
			.build();
		// @formatter:on

		Home.start(componentSpaceConfig);
		try {
			final Domain doString = Home.getDefinitionSpace().resolve("DO_STRING", Domain.class);
			Assert.assertNotNull(doString);
			final DtDefinition dtFamille = Home.getDefinitionSpace().resolve("DT_FAMILLE", DtDefinition.class);
			Assert.assertNotNull(dtFamille);
		} finally {
			Home.stop();
		}
	}

	private ComponentSpaceConfigBuilder prepareDefaultComponentSpaceConfigBuilder() {
		// @formatter:off
		final ComponentSpaceConfigBuilder componentSpaceConfigBuilder = new ComponentSpaceConfigBuilder()
		.withParam("log4j.configurationFileName", "/log4j.xml")
		.withSilence(false)
		.beginModule("vertigo")
			.beginComponent(LocaleManager.class, LocaleManagerImpl.class)
				.withParam("locales", "locales")
			.endComponent()
			.beginComponent(ResourceManager.class, ResourceManagerImpl.class)
				.beginPlugin(ClassPathResourceResolverPlugin.class).endPlugin()
			.endComponent()
			.beginComponent(EnvironmentManager.class, EnvironmentManagerImpl.class)
				.beginPlugin(KprLoaderPlugin.class).endPlugin()
				.beginPlugin(AnnotationLoaderPlugin.class).endPlugin()
				.beginPlugin(DomainDynamicRegistryPlugin.class).endPlugin()
			.endComponent()
		.endModule();
		// @formatter:on
		return componentSpaceConfigBuilder;
	}
}
