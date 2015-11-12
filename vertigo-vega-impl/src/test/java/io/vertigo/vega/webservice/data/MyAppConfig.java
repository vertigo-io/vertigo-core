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
package io.vertigo.vega.webservice.data;

import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.AppConfigBuilder;
import io.vertigo.commons.impl.CommonsFeatures;
import io.vertigo.commons.plugins.cache.memory.MemoryCachePlugin;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.impl.DynamoFeatures;
import io.vertigo.dynamo.impl.kvstore.KVStoreManagerImpl;
import io.vertigo.dynamo.kvstore.KVStoreManager;
import io.vertigo.dynamo.plugins.environment.loaders.java.AnnotationLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.KprLoaderPlugin;
import io.vertigo.dynamo.plugins.environment.registries.domain.DomainDynamicRegistryPlugin;
import io.vertigo.dynamo.plugins.kvstore.delayedmemory.DelayedMemoryKVStorePlugin;
import io.vertigo.persona.impl.security.PersonaFeatures;
import io.vertigo.persona.plugins.security.loaders.SecurityResourceLoaderPlugin;
import io.vertigo.vega.VegaFeatures;
import io.vertigo.vega.engines.webservice.cmd.ComponentCmdWebServices;
import io.vertigo.vega.webservice.WebServices;
import io.vertigo.vega.webservice.data.domain.Address;
import io.vertigo.vega.webservice.data.domain.Contact;
import io.vertigo.vega.webservice.data.domain.ContactCriteria;
import io.vertigo.vega.webservice.data.domain.ContactDao;
import io.vertigo.vega.webservice.data.domain.ContactView;
import io.vertigo.vega.webservice.data.user.TestUserSession;
import io.vertigo.vega.webservice.data.ws.CommonWebServices;
import io.vertigo.vega.webservice.data.ws.ContactsWebServices;
import io.vertigo.vega.webservice.data.ws.FileDownloadWebServices;
import io.vertigo.vega.webservice.data.ws.TestWebServices;

import java.util.Arrays;
import java.util.Iterator;

public final class MyAppConfig {
	public static final int WS_PORT = 8088;

	public static final class DtDefinitions implements Iterable<Class<?>> {
		@Override
		public Iterator<Class<?>> iterator() {
			return Arrays.asList(new Class<?>[] {
					Contact.class, ContactCriteria.class,
					Address.class, ContactView.class
			}).iterator();
		}
	}

	public static AppConfig config() {
		// @formatter:off
		return new AppConfigBuilder()
			.beginBootModule("fr")
				.addPlugin( ClassPathResourceResolverPlugin.class)
				.addPlugin(SecurityResourceLoaderPlugin.class)
				.addPlugin(AnnotationLoaderPlugin.class)
				.addPlugin(KprLoaderPlugin.class)
				.addPlugin(DomainDynamicRegistryPlugin.class)
			.endModule()
			.beginBoot()
				.silently()
			.endBoot()
			.beginModule(PersonaFeatures.class).withUserSession(TestUserSession.class).endModule()
			.beginModule(CommonsFeatures.class).withCache(MemoryCachePlugin.class).endModule()
			.beginModule(DynamoFeatures.class)
				//.withStore()
				.getModuleConfigBuilder()
				.addComponent(KVStoreManager.class, KVStoreManagerImpl.class)
			//	.addPlugin(PDFExporterPlugin.class) //pour exportManager
//				.beginPlugin(PostgreSqlDataStorePlugin.class)
//					.addParam("sequencePrefix","SEQ_")
//				.endPlugin()
				.beginPlugin(DelayedMemoryKVStorePlugin.class)
					.addParam("dataStoreName", "UiSecurityStore")
					.addParam("collections", "sessions")
					.addParam("timeToLiveSeconds", "120")
				.endPlugin()
			.endModule()
			.beginModule(VegaFeatures.class)
				.withTokens()
				.withMisc()
				.withEmbeddedServer(WS_PORT)
			.endModule()
			//-----
			.beginModule("dao-app").withNoAPI().withInheritance(Object.class)
				.addComponent(ContactDao.class)
			.endModule()
			.beginModule("webservices-app").withNoAPI().withInheritance(WebServices.class)
				.addComponent(ComponentCmdWebServices.class)
				.addComponent(CommonWebServices.class)
				.addComponent(ContactsWebServices.class)
				.addComponent(TestWebServices.class)
				.addComponent(FileDownloadWebServices.class)
			.endModule()
			.beginModule("myApp")
				.addDefinitionResource("classes", DtDefinitions.class.getName())
				.addDefinitionResource("kpr", "io/vertigo/vega/webservice/data/execution.kpr")
			.endModule()
		.build();
		// @formatter:on
	}
}
