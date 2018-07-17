/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.component;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.app.AutoCloseableApp;
import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.LogConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.util.AbstractTestCaseJU4;

@RunWith(JUnitPlatform.class)
public final class ComponentSpace4Test {

	@Test
	public void testStartedComponent() {
		final AppConfig appConfig = AppConfig.builder()
				.beginBoot()
				.withLogConfig(new LogConfig("/log4j.xml"))
				.endBoot()
				.addModule(ModuleConfig.builder("Started", AbstractTestCaseJU4.getCoreLookup())
						.addComponent(StartedManager.class, StartedManagerImpl.class)
						.build())
				.addInitializer(StartedManagerInitializer.class)
				.build();
		final StartedManager startedManager;
		try (AutoCloseableApp app = new AutoCloseableApp(appConfig)) {
			startedManager = app.getComponentSpace().resolve(StartedManager.class);
			assertTrue(startedManager.isInitialized(), "Component StartedManager not Initialized");
			assertTrue(startedManager.isStarted(), "Component StartedManager not Started");
			assertTrue(startedManager.isAppPreActivated(), "Component StartedManager not PostStarted");
		}
		assertFalse(startedManager.isStarted(), "Component StartedManager not Stopped");
	}
}
