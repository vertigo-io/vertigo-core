package io.vertigo.commons.node;

import java.util.Optional;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.commons.impl.CommonsFeatures;
import io.vertigo.commons.plugins.node.registry.redis.RedisNodeRegistryPlugin;

@RunWith(JUnitPlatform.class)
public class RedisNodeRegistryPluginTest extends AbstractNodeManagerTest {

	@Override
	protected AppConfig buildAppConfig() {

		final String redisHost = "redis-pic.part.klee.lan.net";
		final int redisPort = 6379;
		final int redisDatabase = 11;

		return AppConfig.builder()
				.beginBoot()
				.endBoot()
				.addModule(new CommonsFeatures()
						.withRedisConnector(redisHost, redisPort, redisDatabase, Optional.empty())
						.withNodeRegistryPlugin(RedisNodeRegistryPlugin.class)
						.build())
				.addModule(ModuleConfig.builder("db")
						.build())
				.build();
	}

}
