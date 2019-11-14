/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.node.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.node.AutoCloseableApp;
import io.vertigo.core.node.component.data.BioManager;
import io.vertigo.core.node.component.data.BioManagerImpl;
import io.vertigo.core.node.component.data.DummyPlugin;
import io.vertigo.core.node.component.data.MathManager;
import io.vertigo.core.node.component.data.MathManagerImpl;
import io.vertigo.core.node.component.data.MathPlugin;
import io.vertigo.core.node.config.LogConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.Param;

public final class ComponentSpaceTest {

	@Test
	public void testHome() {
		final NodeConfig nodeConfig = NodeConfig.builder()
				.beginBoot()
				.withLogConfig(new LogConfig("/log4j.xml"))
				.endBoot()
				.addModule(ModuleConfig.builder("Bio")
						.addComponent(BioManager.class, BioManagerImpl.class)
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.addPlugin(MathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.build();

		try (AutoCloseableApp app = new AutoCloseableApp(nodeConfig)) {
			final BioManager bioManager = app.getComponentSpace().resolve(BioManager.class);
			final int res = bioManager.add(1, 2, 3);
			assertEquals(366, res);
			assertTrue(bioManager.isActive());
		}
	}

	@Test
	public void testHome2() {
		final NodeConfig nodeConfig = NodeConfig.builder()
				.beginBoot()
				.withLogConfig(new LogConfig("/log4j.xml"))
				.endBoot()
				.addModule(ModuleConfig.builder("Bio")
						.addComponent(BioManager.class, BioManagerImpl.class)
						//This plugin DummyPlugin is not used By BioManager !!
						.addPlugin(DummyPlugin.class)
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.addPlugin(MathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.build();

		Assertions.assertThrows(RuntimeException.class,
				() -> {
					try (AutoCloseableApp app = new AutoCloseableApp(nodeConfig)) {
						//
					}
				});
	}

	@Test
	public void testHome3() {
		final NodeConfig nodeConfig = NodeConfig.builder()
				.beginBoot()
				.withLogConfig(new LogConfig("/log4j.xml"))
				.endBoot()
				.addModule(ModuleConfig.builder("Bio-core")
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.addPlugin(MathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.addModule(ModuleConfig.builder("Bio-spe") //This module depends of Bio-core module
						.addComponent(BioManager.class, BioManagerImpl.class)
						.build())
				.build();

		try (AutoCloseableApp app = new AutoCloseableApp(nodeConfig)) {
			//
		}
	}
}