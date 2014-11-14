package io.vertigo.vega.rest;

import io.vertigo.commons.cache.CacheManager;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.impl.cache.CacheManagerImpl;
import io.vertigo.commons.impl.codec.CodecManagerImpl;
import io.vertigo.commons.impl.resource.ResourceManagerImpl;
import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.commons.plugins.cache.map.MapCachePlugin;
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
import io.vertigo.dynamo.plugins.persistence.postgresql.PostgreSqlDataStorePlugin;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.engines.command.TcpVCommandEngine;
import io.vertigo.persona.impl.security.KSecurityManagerImpl;
import io.vertigo.persona.plugins.security.loaders.SecurityResourceLoaderPlugin;
import io.vertigo.persona.security.KSecurityManager;
import io.vertigo.vega.impl.rest.RestManagerImpl;
import io.vertigo.vega.impl.rest.catalog.CatalogRestServices;
import io.vertigo.vega.impl.rest.catalog.SwaggerRestServices;
import io.vertigo.vega.impl.rest.handler.RateLimitingHandler;
import io.vertigo.vega.impl.security.UiSecurityTokenManagerImpl;
import io.vertigo.vega.plugins.rest.instrospector.annotations.AnnotationsEndPointIntrospectorPlugin;
import io.vertigo.vega.rest.WsRestHandler.DtDefinitions;
import io.vertigo.vega.security.UiSecurityTokenManager;
import io.vertigoimpl.commons.locale.LocaleManagerImpl;
import io.vertigoimpl.engines.rest.cmd.ComponentCmdRestServices;

public final class MyApp {
	static AppConfig config() {
		// @formatter:off
		return new AppConfigBuilder()
			.withSilence(false)
			.withCommandEngine(new TcpVCommandEngine(4406))
			.withResource("classes", DtDefinitions.class.getName())
			.withResource("kpr", "ksp/execution.kpr")
			.beginModule("commons")
				.beginComponent(LocaleManager.class, LocaleManagerImpl.class)
					.withParam("locales", "fr")
				.endComponent()
				.beginComponent(ResourceManager.class, ResourceManagerImpl.class)
					.beginPlugin( ClassPathResourceResolverPlugin.class).endPlugin()
				.endComponent()
				.beginComponent(KSecurityManager.class, KSecurityManagerImpl.class)
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
					.beginPlugin( MapCachePlugin.class).endPlugin()
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
				.beginComponent(ContactsRestServices.class).endComponent()
				.beginComponent(TesterRestServices.class).endComponent()
				.beginComponent(TesterFileDownload.class).endComponent()
			.endModule()
			.beginModule("restCore").withNoAPI().withInheritance(Object.class)
				.beginComponent(RestManager.class, RestManagerImpl.class)
					.beginPlugin(AnnotationsEndPointIntrospectorPlugin.class).endPlugin()
				.endComponent()
				.beginComponent(SwaggerRestServices.class).endComponent()
				.beginComponent(CatalogRestServices.class).endComponent()
				.beginComponent(RateLimitingHandler.class).endComponent()
				.beginComponent(UiSecurityTokenManager.class, UiSecurityTokenManagerImpl.class)
					.withParam("storeName", "UiSecurityStore")
				.endComponent()
			.endModule()
		.build();
		// @formatter:on
	}
}
