package io.vertigo.core.plugins.node.registry.redis;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.core.connectors.ConnectorsFeatures;
import io.vertigo.core.node.AbstractNodeManagerTest;
import io.vertigo.core.node.NodeManager;
import io.vertigo.core.node.NodeManagerImpl;
import io.vertigo.core.plugins.node.registry.redis.RedisNodeRegistryPlugin;

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
				.addModule(new ConnectorsFeatures()
						.withRedisConnector(redisHost, redisPort, redisDatabase)
						.build())
				.addModule(ModuleConfig.builder("nodeManager")
						.addComponent(NodeManager.class, NodeManagerImpl.class)
						.addPlugin(RedisNodeRegistryPlugin.class)
						.build())
				.addModule(ModuleConfig.builder("db")
						.build())
				.build();
	}

}
