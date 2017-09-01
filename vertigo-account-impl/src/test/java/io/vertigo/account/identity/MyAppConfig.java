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
package io.vertigo.account.identity;

import java.util.Optional;

import io.vertigo.account.AccountFeatures;
import io.vertigo.account.data.TestUserSession;
import io.vertigo.account.plugins.identity.cache.memory.MemoryAccountCachePlugin;
import io.vertigo.account.plugins.identity.store.text.TextAccountStorePlugin;
import io.vertigo.app.config.AppConfig;
import io.vertigo.commons.impl.CommonsFeatures;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;
import io.vertigo.dynamo.impl.DynamoFeatures;

public final class MyAppConfig {
	private static final String REDIS_HOST = "redis-pic.part.klee.lan.net";
	private static final int REDIS_PORT = 6379;
	private static final int REDIS_DATABASE = 15;

	public static AppConfig config(final boolean redis) {
		final CommonsFeatures commonsFeatures = new CommonsFeatures();
		final AccountFeatures accountFeatures = new AccountFeatures()
				.withUserSession(TestUserSession.class);

		if (redis) {
			commonsFeatures.withRedisConnector(REDIS_HOST, REDIS_PORT, REDIS_DATABASE, Optional.empty());
			accountFeatures.withRedisAccountCachePlugin();
		} else {
			//else we use memory
			accountFeatures
					.withAccountCachePlugin(MemoryAccountCachePlugin.class)
					.withAccountStorePlugin(TextAccountStorePlugin.class,
							Param.of("accountFilePath", "file:/io/vertigo/account/data/identities.txt"),
							Param.of("accountFilePattern", "^(?<id>[^\\s;]+);(?<displayName>[^\\s;]+);(?<email>(?<authToken>[^\\s;@]+)@[^\\s;]+);(?<photoUrl>)$"),
							Param.of("groupFilePath", "file:/io/vertigo/account/data/groups.txt"),
							Param.of("groupFilePattern", "^(?<id>[^\\s;]+);(?<displayName>[^\\s;]+);(?<accountIds>.*)$"));
		}
		return AppConfig.builder()
				.beginBoot()
				.withLocales("fr")
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.endBoot()
				.addModule(commonsFeatures.build())
				.addModule(new DynamoFeatures().build())
				.addModule(accountFeatures.build())
				.build();
	}

}
