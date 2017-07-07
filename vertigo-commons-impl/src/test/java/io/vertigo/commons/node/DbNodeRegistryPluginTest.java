package io.vertigo.commons.node;

import org.h2.Driver;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.app.config.AppConfig;
import io.vertigo.commons.impl.CommonsFeatures;
import io.vertigo.commons.plugins.node.registry.db.DbNodeRegistryPlugin;
import io.vertigo.core.param.Param;

@RunWith(JUnitPlatform.class)
public class DbNodeRegistryPluginTest extends AbstractNodeManagerTest {

	@Override
	protected AppConfig buildAppConfig() {

		return buildRootAppConfig()
				.addModule(new CommonsFeatures()
						.withNodeRegistryPlugin(DbNodeRegistryPlugin.class,
								Param.of("driverClassName", Driver.class.getName()),
								Param.of("jdbcUrl", "jdbc:h2:mem:database"))
						.build())
				.build();
	}

}
