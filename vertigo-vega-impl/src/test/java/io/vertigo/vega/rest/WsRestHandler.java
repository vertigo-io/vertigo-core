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
import io.vertigo.core.Home;
import io.vertigo.core.config.ComponentSpaceConfig;
import io.vertigo.core.config.ComponentSpaceConfigBuilder;
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
import io.vertigo.vega.impl.rest.filter.JettyMultipartConfig;
import io.vertigo.vega.impl.rest.handler.RateLimitingHandler;
import io.vertigo.vega.impl.security.UiSecurityTokenManagerImpl;
import io.vertigo.vega.plugins.rest.instrospector.annotations.AnnotationsEndPointIntrospectorPlugin;
import io.vertigo.vega.plugins.rest.routesregister.sparkjava.SparkJavaRoutesRegister;
import io.vertigo.vega.security.UiSecurityTokenManager;
import io.vertigoimpl.commons.locale.LocaleManagerImpl;
import io.vertigoimpl.engines.rest.cmd.ComponentCmdRestServices;

import java.util.Arrays;
import java.util.Iterator;

import spark.Spark;

/**
 * Main WebService Route handler.
 * TODO : make configurable
 * @author npiedeloup
 */
public final class WsRestHandler {

	public static final class DtDefinitions implements Iterable<Class<?>> {
		public Iterator<Class<?>> iterator() {
			return Arrays.asList(new Class<?>[] { //
					Contact.class, ContactCriteria.class //
			}).iterator();
		}
	}

	public static void main(final String[] args) {
		Spark.setPort(8088);

		//		<final component api="EnvironmentManager" class="io.vertigo.dynamo.impl.environment.EnvironmentManagerImpl">
		//
		//       <plugin class="io.vertigo.dynamo.plugins.environment.loaders.kpr.KprLoaderPlugin" >
		//            <param name ="kpr" value="invs/mdo/execution.kpr"/>
		//        </plugin>
		//        <plugin class="io.vertigo.dynamo.plugins.environment.registries.domain.DomainDynamicRegistryPlugin" />
		//    </component>

		// @formatter:off
		// Création de l'état de l'application
		// Initialisation de l'état de l'application

		final ComponentSpaceConfig config = new ComponentSpaceConfigBuilder()//
				.withSilence(false)//
				//.withRestEngine(new GrizzlyRestEngine(8080))
				.withCommandEngine(new TcpVCommandEngine(4406))//
				.beginModule("commons") //
					.beginComponent(LocaleManager.class, LocaleManagerImpl.class)
						.withParam("locales", "fr")
					.endComponent()
					.beginComponent(ResourceManager.class, ResourceManagerImpl.class)
						.beginPlugin( ClassPathResourceResolverPlugin.class).endPlugin()
					.endComponent()
					.beginComponent(KSecurityManager.class, KSecurityManagerImpl.class)//
						.withParam("userSessionClassName", TestUserSession.class.getName())
						.beginPlugin(SecurityResourceLoaderPlugin.class).endPlugin()
					.endComponent() //
				.endModule()
				.beginModule("dynamo").withNoAPI() //
					.beginComponent(CodecManager.class, CodecManagerImpl.class).endComponent() //
					.beginComponent(CollectionsManager.class, CollectionsManagerImpl.class).endComponent() //
					.beginComponent(FileManager.class, FileManagerImpl.class).endComponent() //
					.beginComponent(KVDataStoreManager.class, KVDataStoreManagerImpl.class)
						.beginPlugin(DelayedMemoryKVDataStorePlugin.class)
							.withParam("dataStoreName", "UiSecurityStore")
							.withParam("timeToLiveSeconds", "120")
						.endPlugin()
					.endComponent() //
					.beginComponent(PersistenceManager.class, PersistenceManagerImpl.class)
						.beginPlugin(PostgreSqlDataStorePlugin.class) //
							.withParam("sequencePrefix","SEQ_") //
						.endPlugin() //
					.endComponent() //
					.beginComponent(CacheManager.class, CacheManagerImpl.class)
						.beginPlugin( MapCachePlugin.class).endPlugin()
					.endComponent() //
					.beginComponent(TaskManager.class, TaskManagerImpl.class).endComponent() //
//					.beginComponent(WorkManager.class, WorkManagerImpl.class)
//						.withParam("workerCount", "2") //
//					.endComponent() //
					.beginComponent(ExportManager.class, ExportManagerImpl.class)//
						.beginPlugin(PDFExporterPlugin.class).endPlugin() //
					.endComponent() //
					.beginComponent(EnvironmentManagerImpl.class) //
						.beginPlugin(AnnotationLoaderPlugin.class).endPlugin() //
						.beginPlugin(KprLoaderPlugin.class).endPlugin() //
						.beginPlugin(DomainDynamicRegistryPlugin.class).endPlugin()	//
					.endComponent()
					.withResource("classes", DtDefinitions.class.getName())
					.withResource("kpr", "ksp/execution.kpr")
				.endModule()
				.beginModule("restServices").withNoAPI().withInheritance(RestfulService.class) //
					.beginComponent(ComponentCmdRestServices.class).endComponent() //
					.beginComponent(ContactsRestServices.class).endComponent() //
					.beginComponent(TesterRestServices.class).endComponent() //
					.beginComponent(TesterFileDownload.class).endComponent() //
				.endModule()
				.beginModule("restCore").withNoAPI().withInheritance(Object.class) //
					.beginComponent(RestManager.class, RestManagerImpl.class)
						.beginPlugin(AnnotationsEndPointIntrospectorPlugin.class).endPlugin() //
					.endComponent() //
					.beginComponent(SwaggerRestServices.class).endComponent() //
					.beginComponent(CatalogRestServices.class).endComponent() //
					.beginComponent(RateLimitingHandler.class).endComponent() //
					.beginComponent(UiSecurityTokenManager.class, UiSecurityTokenManagerImpl.class)
						.withParam("storeName", "UiSecurityStore") //
					.endComponent() //
				.endModule()

				.build();
		// @formatter:on
		Home.start(config);

		//test
		/*Spark.get(new Route("familles") {
			@Override
			public Object handle(final Request request, final Response response) {
				final Object value = famillesRestFulService.readList();
				return new Gson().toJson(value);
			}
		});*/

		// Will serve all static file are under "/public" in classpath if the route isn't consumed by others routes.
		// When using Maven, the "/public" folder is assumed to be in "/main/resources"
		Spark.externalStaticFileLocation("d:/Projets/Projet_Kasper/SPA-Fmk/SPA-skeleton/public/");
		//Spark.externalStaticFileLocation("D:/@GitHub/vertigo/vertigo-vega-impl/src/test/resources/");
		//Spark.before(new IE8CompatibilityFix("8"));
		//Spark.before(new CorsAllower());
		//Translate EndPoint to route
		final String tempDir = System.getProperty("java.io.tmpdir");
		Spark.before(new JettyMultipartConfig(tempDir));
		//Spark.before(new VegaMultipartConfig(tempDir));

		new SparkJavaRoutesRegister().init();
	}
}
