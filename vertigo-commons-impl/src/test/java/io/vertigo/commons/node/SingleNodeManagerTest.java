package io.vertigo.commons.node;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.commons.impl.node.NodeManagerImpl;

@RunWith(JUnitPlatform.class)
public class SingleNodeManagerTest extends AbstractNodeManagerTest {

	@Override
	protected AppConfig buildAppConfig() {
		return AppConfig.builder()
				.beginBoot()
				.endBoot()
				.addModule(ModuleConfig.builder("nodeManager")
						.addComponent(NodeManager.class, NodeManagerImpl.class)
						.build())
				.addModule(ModuleConfig.builder("db")
						.build())
				.build();
	}

}
