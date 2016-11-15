/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.spaces.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.app.AutoCloseableApp;
import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.AppConfigBuilder;
import io.vertigo.app.config.LogConfig;
import io.vertigo.core.spaces.component.data.BioManager;
import io.vertigo.core.spaces.component.data.BioManagerImpl;
import io.vertigo.core.spaces.component.data.DummyPlugin;
import io.vertigo.core.spaces.component.data.MathManager;
import io.vertigo.core.spaces.component.data.MathManagerImpl;
import io.vertigo.core.spaces.component.data.MathPlugin;

@RunWith(JUnitPlatform.class)
public final class ComponentSpaceTest {

	@Test
	public void testHome() {
		// @formatter:off
		final AppConfig appConfig = new AppConfigBuilder()
			.beginBoot()
				.withLogConfig(new LogConfig("/log4j.xml"))
			.endBoot()
			.beginModule("Bio")
				.addComponent(BioManager.class, BioManagerImpl.class)
				.beginComponent(MathManager.class, MathManagerImpl.class)
					.addParam("start", "100")
				.endComponent()
				.beginPlugin( MathPlugin.class)
					.addParam("factor", "20")
				.endPlugin()
			.endModule()
		.build();
		// @formatter:on

		try (AutoCloseableApp app = new AutoCloseableApp(appConfig)) {
			final BioManager bioManager = app.getComponentSpace().resolve(BioManager.class);
			final int res = bioManager.add(1, 2, 3);
			assertEquals(366, res);
			assertTrue(bioManager.isActive());
		}
	}

	@Test
	public void testHome2() {
		// @formatter:off
		final AppConfig appConfig = new AppConfigBuilder()
			.beginBoot()
				.withLogConfig(new LogConfig("/log4j.xml"))
			.endBoot()
			.beginModule("Bio")
				.beginComponent(BioManager.class, BioManagerImpl.class).endComponent()
				//This plugin DummyPlugin is not used By BioManager !!
				.addPlugin(DummyPlugin.class)
				.beginComponent(MathManager.class, MathManagerImpl.class)
					.addParam("start", "100")
				.endComponent()
				.beginPlugin( MathPlugin.class)
					.addParam("factor", "20")
				.endPlugin()
			.endModule()
		.build();
		// @formatter:on

		Assertions.assertThrows(RuntimeException.class,
				() -> {
					try (AutoCloseableApp app = new AutoCloseableApp(appConfig)) {
						//
					}
				});
	}

	@Test
	public void testHome3() {
		// @formatter:off
		final AppConfig appConfig = new AppConfigBuilder()
			.beginBoot()
				.withLogConfig(new LogConfig("/log4j.xml"))
			.endBoot()
			.beginModule("Bio-core")
				.beginComponent(MathManager.class, MathManagerImpl.class)
					.addParam("start", "100")
				.endComponent()
				.beginPlugin( MathPlugin.class)
					.addParam("factor", "20")
				.endPlugin()
			.endModule()
			.beginModule("Bio-spe") //This module depends of Bio-core module
				.beginComponent(BioManager.class, BioManagerImpl.class).endComponent()
			.endModule()
		.build();
		// @formatter:on

		try (AutoCloseableApp app = new AutoCloseableApp(appConfig)) {
			//
		}
	}
}
