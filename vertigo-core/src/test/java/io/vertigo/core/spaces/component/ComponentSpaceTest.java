/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import io.vertigo.core.App;
import io.vertigo.core.Home;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.config.LogConfig;
import io.vertigo.core.spaces.component.data.BioManager;
import io.vertigo.core.spaces.component.data.BioManagerImpl;
import io.vertigo.core.spaces.component.data.DummyPlugin;
import io.vertigo.core.spaces.component.data.MathManager;
import io.vertigo.core.spaces.component.data.MathManagerImpl;
import io.vertigo.core.spaces.component.data.MathPlugin;

import org.junit.Assert;
import org.junit.Test;

public final class ComponentSpaceTest {

	@Test
	public void testHome() {
		// @formatter:off
		final AppConfig appConfig = new AppConfigBuilder()
			.withLogConfig(new LogConfig("/log4j.xml"))
			.beginModule("Bio")
				.beginComponent(BioManager.class, BioManagerImpl.class).endComponent()
				.beginComponent(MathManager.class, MathManagerImpl.class)
					.addParam("start", "100")
					.beginPlugin( MathPlugin.class)
						.addParam("factor", "20")
					.endPlugin()
				.endComponent()
			.endModule()
		.build();
		// @formatter:on

		try (App app = new App(appConfig)) {
			final BioManager bioManager = Home.getComponentSpace().resolve(BioManager.class);
			final int res = bioManager.add(1, 2, 3);
			Assert.assertEquals(366, res);
			Assert.assertTrue(bioManager.isActive());
		}
	}

	@Test(expected = RuntimeException.class)
	public void testHome2() {
		// @formatter:off
		final AppConfig appConfig = new AppConfigBuilder()
			.withLogConfig(new LogConfig("/log4j.xml"))
			.beginModule("Bio")
				.beginComponent(BioManager.class, BioManagerImpl.class)
					//This plugin DummyPlugin is not used By BioManager !!
					.beginPlugin(DummyPlugin.class).endPlugin()
				.endComponent()
				.beginComponent(MathManager.class, MathManagerImpl.class)
					.addParam("start", "100")
					.beginPlugin( MathPlugin.class)
						.addParam("factor", "20")
					.endPlugin()
				.endComponent()
			.endModule()
		.build();
		// @formatter:on

		try (App app = new App(appConfig)) {
			//
		}
	}

	@Test
	public void testHome3() {
		// @formatter:off
		final AppConfig appConfig = new AppConfigBuilder()
			.withLogConfig(new LogConfig("/log4j.xml"))
			.beginModule("Bio-core")
				.beginComponent(MathManager.class, MathManagerImpl.class)
					.addParam("start", "100")
					.beginPlugin( MathPlugin.class)
						.addParam("factor", "20")
					.endPlugin()
				.endComponent()
			.endModule()
			.beginModule("Bio-spe") //This module depends of Bio-core module
				.beginComponent(BioManager.class, BioManagerImpl.class)
				.endComponent()
			.endModule()
		.build();
		// @formatter:on

		try (App app = new App(appConfig)) {
			//
		}
	}
}
