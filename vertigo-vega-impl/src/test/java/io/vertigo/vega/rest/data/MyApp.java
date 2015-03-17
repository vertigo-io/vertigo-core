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
package io.vertigo.vega.rest.data;

import io.vertigo.commons.cache.CacheManager;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.cache.CacheManagerImpl;
import io.vertigo.commons.impl.codec.CodecManagerImpl;
import io.vertigo.commons.impl.locale.LocaleManagerImpl;
import io.vertigo.commons.impl.resource.ResourceManagerImpl;
import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.commons.plugins.cache.memory.MemoryCachePlugin;
import io.vertigo.commons.plugins.resource.java.ClassPathResourceResolverPlugin;
import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.export.ExportManager;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.impl.collections.CollectionsManagerImpl;
import io.vertigo.dynamo.impl.environment.EnvironmentManagerImpl;
import io.vertigo.dynamo.impl.export.ExportManagerImpl;
import io.vertigo.dynamo.impl.file.FileManagerImpl;
import io.vertigo.dynamo.impl.kvdatastore.KVDataStoreManagerImpl;
import io.vertigo.dynamo.impl.persistence.PersistenceManagerImpl;
import io.vertigo.dynamo.impl.task.TaskManagerImpl;
import io.vertigo.dynamo.kvdatastore.KVDataStoreManager;
import io.vertigo.dynamo.persistence.PersistenceManager;
import io.vertigo.dynamo.plugins.environment.loaders.java.AnnotationLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.KprLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainDynamicRegistryPlugin;
import io.vertigo.dynamo.plugins.export.pdf.PDFExporterPlugin;
import io.vertigo.dynamo.plugins.kvdatastore.delayedmemory.DelayedMemoryKVDataStorePlugin;
import io.vertigo.dynamo.plugins.persistence.datastore.postgresql.PostgreSqlDataStorePlugin;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.persona.impl.security.VSecurityManagerImpl;
import io.vertigo.persona.plugins.security.loaders.SecurityResourceLoaderPlugin;
import io.vertigo.persona.security.VSecurityManager;
import io.vertigo.vega.impl.rest.RestManagerImpl;
import io.vertigo.vega.impl.rest.catalog.CatalogRestServices;
import io.vertigo.vega.impl.rest.catalog.SwaggerRestServices;
import io.vertigo.vega.impl.token.TokenManagerImpl;
import io.vertigo.vega.plugins.rest.handler.AccessTokenRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.CorsAllowerRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.ExceptionRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.JsonConverterRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.PaginatorAndSortRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.RateLimitingRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.RestfulServiceRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.SecurityRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.SessionInvalidateRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.SessionRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.ValidatorRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.instrospector.annotations.AnnotationsEndPointIntrospectorPlugin;
import io.vertigo.vega.plugins.rest.routesregister.sparkjava.SparkJavaRoutesRegisterPlugin;
import io.vertigo.vega.rest.RestManager;
import io.vertigo.vega.rest.RestfulService;
import io.vertigo.vega.rest.WsRestHandler.DtDefinitions;
import io.vertigo.vega.rest.data.domain.ContactDao;
import io.vertigo.vega.rest.data.user.TestUserSession;
import io.vertigo.vega.rest.data.ws.WsCommonRestServices;
import io.vertigo.vega.rest.data.ws.WsContactsRestServices;
import io.vertigo.vega.rest.data.ws.WsFileDownload;
import io.vertigo.vega.rest.data.ws.WsRestServices;
import io.vertigo.vega.rest.engine.GoogleJsonEngine;
import io.vertigo.vega.rest.engine.JsonEngine;
import io.vertigo.vega.token.TokenManager;
import io.vertigoimpl.engines.rest.cmd.ComponentCmdRestServices;

public final class MyApp {
	public static AppConfig config() {
		// @formatter:off
		return new AppConfigBuilder()
			.withSilence(true)
			.beginModule("commons")
				.beginComponent(LocaleManager.class, LocaleManagerImpl.class)
					.withParam("locales", "fr")
				.endComponent()
				.beginComponent(ResourceManager.class, ResourceManagerImpl.class)
					.beginPlugin( ClassPathResourceResolverPlugin.class).endPlugin()
				.endComponent()
				.beginComponent(VSecurityManager.class, VSecurityManagerImpl.class)
					.withParam("userSessionClassName", TestUserSession.class.getName())
					.beginPlugin(SecurityResourceLoaderPlugin.class).endPlugin()
				.endComponent()
			.endModule()
			.beginModule("dynamo").withNoAPI()
				.beginComponent(CodecManager.class, CodecManagerImpl.class).endComponent()
				.beginComponent(CollectionsManager.class, CollectionsManagerImpl.class).endComponent()
				.beginComponent(FileManager.class, FileManagerImpl.class).endComponent()
				.beginComponent(KVDataStoreManager.class, KVDataStoreManagerImpl.class)
					.beginPlugin(DelayedMemoryKVDataStorePlugin.class)
						.withParam("dataStoreName", "UiSecurityStore")
						.withParam("timeToLiveSeconds", "120")
					.endPlugin()
				.endComponent()
				.beginComponent(PersistenceManager.class, PersistenceManagerImpl.class)
					.beginPlugin(PostgreSqlDataStorePlugin.class)
						.withParam("sequencePrefix","SEQ_")
					.endPlugin()
				.endComponent()
				.beginComponent(CacheManager.class, CacheManagerImpl.class)
					.beginPlugin( MemoryCachePlugin.class).endPlugin()
				.endComponent()
				.beginComponent(TaskManager.class, TaskManagerImpl.class).endComponent()
				.beginComponent(ExportManager.class, ExportManagerImpl.class)
					.beginPlugin(PDFExporterPlugin.class).endPlugin()
				.endComponent()
				.beginComponent(EnvironmentManagerImpl.class)
					.beginPlugin(AnnotationLoaderPlugin.class).endPlugin()
					.beginPlugin(KprLoaderPlugin.class).endPlugin()
					.beginPlugin(DomainDynamicRegistryPlugin.class).endPlugin()
				.endComponent()
			.endModule()
			.beginModule("dao").withNoAPI().withInheritance(Object.class)
				.beginComponent(ContactDao.class).endComponent()
			.endModule()
			.beginModule("restServices").withNoAPI().withInheritance(RestfulService.class)
				.beginComponent(ComponentCmdRestServices.class).endComponent()
				.beginComponent(WsCommonRestServices.class).endComponent()
				.beginComponent(WsContactsRestServices.class).endComponent()
				.beginComponent(WsRestServices.class).endComponent()
				.beginComponent(WsFileDownload.class).endComponent()

			.endModule()
			.beginModule("restCore").withNoAPI().withInheritance(Object.class)
				.beginComponent(JsonEngine.class, GoogleJsonEngine.class).endComponent()
				.beginComponent(SwaggerRestServices.class).endComponent()
				.beginComponent(CatalogRestServices.class).endComponent()
				.beginComponent(TokenManager.class, TokenManagerImpl.class)
					.withParam("dataStoreName", "UiSecurityStore")
				.endComponent()
				.beginComponent(RestManager.class, RestManagerImpl.class)
					.beginPlugin(AnnotationsEndPointIntrospectorPlugin.class).endPlugin()
					.beginPlugin(SparkJavaRoutesRegisterPlugin.class).endPlugin()
					//-- Handlers plugins
					.beginPlugin(ExceptionRestHandlerPlugin.class).endPlugin()
					.beginPlugin(CorsAllowerRestHandlerPlugin.class).endPlugin()
					.beginPlugin(SessionInvalidateRestHandlerPlugin.class).endPlugin()
					.beginPlugin(SessionRestHandlerPlugin.class).endPlugin()
					.beginPlugin(RateLimitingRestHandlerPlugin.class).endPlugin()
					.beginPlugin(SecurityRestHandlerPlugin.class).endPlugin()
					.beginPlugin(AccessTokenRestHandlerPlugin.class).endPlugin()
					.beginPlugin(JsonConverterRestHandlerPlugin.class).endPlugin()
					.beginPlugin(PaginatorAndSortRestHandlerPlugin.class).endPlugin()
					.beginPlugin(ValidatorRestHandlerPlugin.class).endPlugin()
					.beginPlugin(RestfulServiceRestHandlerPlugin.class).endPlugin()
				.endComponent()
			.endModule()
			.beginModule("myApp")
				.withResource("classes", DtDefinitions.class.getName())
				.withResource("kpr", "io/vertigo/vega/rest/data/execution.kpr")
			.endModule()
		.build();
		// @formatter:on
	}
}
