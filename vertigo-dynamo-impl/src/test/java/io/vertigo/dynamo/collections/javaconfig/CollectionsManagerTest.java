package io.vertigo.dynamo.collections.javaconfig;

import io.vertigo.core.config.AppConfig;

/**
 * @author pchretien
 */
public class CollectionsManagerTest extends io.vertigo.dynamo.collections.AbstractCollectionsManagerTest {

	@Override
	protected AppConfig buildAppConfig() {
		return MyAppConfig.config();
	}
}
