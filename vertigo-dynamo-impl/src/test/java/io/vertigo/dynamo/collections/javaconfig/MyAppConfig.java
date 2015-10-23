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
package io.vertigo.dynamo.collections.javaconfig;

import io.vertigo.commons.impl.CommonsFeatures;
import io.vertigo.commons.plugins.cache.memory.MemoryCachePlugin;
import io.vertigo.commons.plugins.resource.java.ClassPathResourceResolverPlugin;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.dynamo.impl.DynamoFeatures;
import io.vertigo.dynamo.plugins.collections.lucene.LuceneIndexPlugin;
import io.vertigo.dynamo.plugins.environment.loaders.java.AnnotationLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.KprLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainDynamicRegistryPlugin;
import io.vertigo.dynamo.plugins.store.datastore.postgresql.PostgreSqlDataStorePlugin;

public final class MyAppConfig {

	public static AppConfig config() {
		// @formatter:off
		return new AppConfigBuilder()
			.beginBootModule("fr")
				.addPlugin( ClassPathResourceResolverPlugin.class)
				.addPlugin(AnnotationLoaderPlugin.class)
				.addPlugin(KprLoaderPlugin.class)
				.addPlugin(DomainDynamicRegistryPlugin.class)
			.endModule()
			.beginBoot()
				.silently()
			.endBoot()
			.beginModule(CommonsFeatures.class).withCache(MemoryCachePlugin.class).endModule()
			.beginModule(DynamoFeatures.class)
				.withStore()
				.getModuleConfigBuilder()
				.beginPlugin(PostgreSqlDataStorePlugin.class)
					.addParam("sequencePrefix","SEQ_")
				.endPlugin()
				.addPlugin(LuceneIndexPlugin.class)
			.endModule()
			
			.beginModule("myApp")
				.addDefinitionResource("classes", io.vertigo.dynamock.domain.DtDefinitions.class.getName())
				.addDefinitionResource("kpr", "io/vertigo/dynamock/execution.kpr")
			.endModule()
		.build();
		// @formatter:on
	}
}
