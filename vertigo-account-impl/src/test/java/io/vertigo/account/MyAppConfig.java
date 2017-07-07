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
package io.vertigo.account;

import java.util.Optional;

import io.vertigo.account.data.TestUserSession;
import io.vertigo.account.plugins.identity.memory.MemoryAccountStorePlugin;
import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.AppConfigBuilder;
import io.vertigo.commons.impl.CommonsFeatures;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.impl.DynamoFeatures;
import io.vertigo.persona.impl.security.PersonaFeatures;

public final class MyAppConfig {
	public static final int WS_PORT = 8088;

	private static AppConfigBuilder createAppConfigBuilder(final boolean redis) {
		final String redisHost = "redis-pic.part.klee.lan.net";
		final int redisPort = 6379;
		final int redisDatabase = 15;

		// @formatter:off
		final AppConfigBuilder appConfigBuilder = AppConfig.builder()
			.beginBoot()
				.withLocales("fr")
				.addPlugin( ClassPathResourceResolverPlugin.class)
			.endBoot()
			.addModule(new PersonaFeatures()
					.withUserSession(TestUserSession.class)
					.build());

			final CommonsFeatures commonsFeatures = new CommonsFeatures();
			if (redis) {
				commonsFeatures.withRedisConnector(redisHost, redisPort, redisDatabase, Optional.empty());
			}

			appConfigBuilder
			.addModule(commonsFeatures.build())
			.addModule(new DynamoFeatures().build());
		if (redis){
			return  appConfigBuilder
			.addModule(new AccountFeatures()
					.withRedisAccountStorePlugin()
					.build());
		}
		//else we use memory
		return  appConfigBuilder
			.addModule(new AccountFeatures()
					.withAccountStorePlugin(MemoryAccountStorePlugin.class)
					.build());
		// @formatter:on
	}

	public static AppConfig config(final boolean redis) {
		// @formatter:off
		return createAppConfigBuilder(redis).build();
	}

}
