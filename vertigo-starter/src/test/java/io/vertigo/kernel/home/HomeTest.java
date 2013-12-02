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
package io.vertigo.kernel.home;

import io.vertigo.kernel.Home;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfig;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.home.data.BioManager;
import io.vertigo.kernel.home.data.BioManagerImpl;
import io.vertigo.kernel.home.data.MathManager;
import io.vertigo.kernel.home.data.MathManagerImpl;
import io.vertigo.kernel.home.data.MathPlugin;

import org.junit.Assert;
import org.junit.Test;


public final class HomeTest {

	@Test
	public void testHome() {
		// @formatter:off
		final ComponentSpaceConfig componentSpaceConfig = new ComponentSpaceConfigBuilder()
			.withParam("log4j.configurationFileName", "/log4j.xml")
			.withSilence(false)
			.beginModule("Bio")
				.beginComponent(BioManager.class, BioManagerImpl.class).endComponent()
				.beginComponent(MathManager.class, MathManagerImpl.class)
					.withParam("start", "100")
					.beginPlugin( MathPlugin.class)
						.withParam("factor", "20")
					.endPlugin()
				.endComponent()	
			.endModule()	
		.build();
		// @formatter:on

		Home.start(componentSpaceConfig);
		try {
			final BioManager bioManager = Home.getComponentSpace().resolve(BioManager.class);
			final int res = bioManager.add(1, 2, 3);
			Assert.assertEquals(366, res);
			Assert.assertTrue(bioManager.isActive());
		} finally {
			Home.stop();
		}
	}
}
