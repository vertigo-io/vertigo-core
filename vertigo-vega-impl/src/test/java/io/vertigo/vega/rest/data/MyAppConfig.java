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

import io.vertigo.commons.impl.CommonsFeatures;
import io.vertigo.commons.plugins.cache.memory.MemoryCachePlugin;
import io.vertigo.commons.plugins.resource.java.ClassPathResourceResolverPlugin;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.environment.EnvironmentManager;
import io.vertigo.core.impl.environment.EnvironmentManagerImpl;
import io.vertigo.core.impl.locale.LocaleManagerImpl;
import io.vertigo.core.impl.resource.ResourceManagerImpl;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.dynamo.collections.CollectionsManager;
import io.vertigo.dynamo.export.ExportManager;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.impl.collections.CollectionsManagerImpl;
import io.vertigo.dynamo.impl.export.ExportManagerImpl;
import io.vertigo.dynamo.impl.file.FileManagerImpl;
import io.vertigo.dynamo.impl.store.StoreManagerImpl;
import io.vertigo.dynamo.impl.task.TaskManagerImpl;
import io.vertigo.dynamo.plugins.environment.loaders.java.AnnotationLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.KprLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainDynamicRegistryPlugin;
import io.vertigo.dynamo.plugins.export.pdf.PDFExporterPlugin;
import io.vertigo.dynamo.plugins.kvdatastore.delayedmemory.DelayedMemoryKVDataStorePlugin;
import io.vertigo.dynamo.plugins.store.datastore.postgresql.PostgreSqlDataStorePlugin;
import io.vertigo.dynamo.store.StoreManager;
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
import io.vertigo.vega.plugins.rest.handler.ServerSideStateRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.SessionInvalidateRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.SessionRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.handler.ValidatorRestHandlerPlugin;
import io.vertigo.vega.plugins.rest.instrospector.annotations.AnnotationsEndPointIntrospectorPlugin;
import io.vertigo.vega.plugins.rest.webserver.sparkjava.SparkJavaEmbeddedWebServerPlugin;
import io.vertigo.vega.plugins.rest.webserver.sparkjava.SparkJavaServletFilterWebServerPlugin;
import io.vertigo.vega.rest.RestManager;
import io.vertigo.vega.rest.RestfulService;
import io.vertigo.vega.rest.data.domain.Contact;
import io.vertigo.vega.rest.data.domain.ContactCriteria;
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

import java.util.Arrays;
import java.util.Iterator;

public final class MyAppConfig {
	public static final int WS_PORT = 8088;

	public static final class DtDefinitions implements Iterable<Class<?>> {
		@Override
		public Iterator<Class<?>> iterator() {
			return Arrays.asList(new Class<?>[] {
					Contact.class, ContactCriteria.class
			}).iterator();
		}
	}

	public static AppConfig config() {
		// @formatter:off
		return new AppConfigBuilder()
			.beginBootModule()
				.beginComponent(LocaleManager.class, LocaleManagerImpl.class)
					.addParam("locales", "fr")
				.endComponent()
				.addComponent(ResourceManager.class, ResourceManagerImpl.class)
					.addPlugin( ClassPathResourceResolverPlugin.class)
				.addComponent(EnvironmentManager.class, EnvironmentManagerImpl.class)
					.addPlugin(SecurityResourceLoaderPlugin.class)
					.addPlugin(AnnotationLoaderPlugin.class)
					.addPlugin(KprLoaderPlugin.class)
					.addPlugin(DomainDynamicRegistryPlugin.class)
			.endModule()
			.beginBoot()
				.silently()
			.endBoot()
			.beginModule("persona")
				.beginComponent(VSecurityManager.class, VSecurityManagerImpl.class)
					.addParam("userSessionClassName", TestUserSession.class.getName())
				.endComponent()
			.endModule()
			.beginModule(CommonsFeatures.class)
				.withCache(MemoryCachePlugin.class)
			.endModule()

			.beginModule("dynamo").withNoAPI()
				.addComponent(CollectionsManager.class, CollectionsManagerImpl.class)
				.addComponent(FileManager.class, FileManagerImpl.class)
				.addComponent(StoreManager.class, StoreManagerImpl.class)
					.beginPlugin(PostgreSqlDataStorePlugin.class)
						.addParam("sequencePrefix","SEQ_")
					.endPlugin()
					.beginPlugin(DelayedMemoryKVDataStorePlugin.class)
						.addParam("dataStoreName", "UiSecurityStore")
						.addParam("timeToLiveSeconds", "120")
					.endPlugin()

				.addComponent(TaskManager.class, TaskManagerImpl.class)
			.endModule()
			.beginModule("dynamo2").withNoAPI().withInheritance(Object.class)
			.addComponent(ExportManager.class, ExportManagerImpl.class)
					.addPlugin(PDFExporterPlugin.class)
			.endModule()
			.beginModule("dao").withNoAPI().withInheritance(Object.class)
				.addComponent(ContactDao.class)
			.endModule()
			.beginModule("restServices").withNoAPI().withInheritance(RestfulService.class)
				.addComponent(ComponentCmdRestServices.class)
				.addComponent(WsCommonRestServices.class)
				.addComponent(WsContactsRestServices.class)
				.addComponent(WsRestServices.class)
				.addComponent(WsFileDownload.class)
			.endModule()
			.beginModule("restCore").withNoAPI().withInheritance(Object.class)
				.addComponent(JsonEngine.class, GoogleJsonEngine.class)
				.addComponent(SwaggerRestServices.class)
				.addComponent(CatalogRestServices.class)
				.beginComponent(TokenManager.class, TokenManagerImpl.class)
					.addParam("dataStoreName", "UiSecurityStore")
				.endComponent()
				.addComponent(RestManager.class, RestManagerImpl.class)
					.addPlugin(AnnotationsEndPointIntrospectorPlugin.class)
					.addPlugin(SparkJavaServletFilterWebServerPlugin.class)
					.beginPlugin(SparkJavaEmbeddedWebServerPlugin.class)
						.addParam("port", String.valueOf(WS_PORT))
					.endPlugin()
					//-- Handlers plugins
					.addPlugin(ExceptionRestHandlerPlugin.class)
					.addPlugin(CorsAllowerRestHandlerPlugin.class)
					.addPlugin(SessionInvalidateRestHandlerPlugin.class)
					.addPlugin(SessionRestHandlerPlugin.class)
					.addPlugin(RateLimitingRestHandlerPlugin.class)
					.addPlugin(SecurityRestHandlerPlugin.class)
					.addPlugin(AccessTokenRestHandlerPlugin.class)
					.addPlugin(JsonConverterRestHandlerPlugin.class)
					.addPlugin(ServerSideStateRestHandlerPlugin.class)
					.addPlugin(PaginatorAndSortRestHandlerPlugin.class)
					.addPlugin(ValidatorRestHandlerPlugin.class)
					.addPlugin(RestfulServiceRestHandlerPlugin.class)
			.endModule()
			.beginModule("myApp")
				.addDefinitionResource("classes", DtDefinitions.class.getName())
				.addDefinitionResource("kpr", "io/vertigo/vega/rest/data/execution.kpr")
			.endModule()
		.build();
		// @formatter:on
	}
}
