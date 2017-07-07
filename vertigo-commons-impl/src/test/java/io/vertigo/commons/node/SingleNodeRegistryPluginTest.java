package io.vertigo.commons.node;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.app.config.AppConfig;
import io.vertigo.commons.impl.CommonsFeatures;

@RunWith(JUnitPlatform.class)
public class SingleNodeRegistryPluginTest extends AbstractNodeManagerTest {

	@Override
	protected AppConfig buildAppConfig() {
		return buildRootAppConfig()
				.addModule(new CommonsFeatures()
						.build())
				.build();
	}

}
