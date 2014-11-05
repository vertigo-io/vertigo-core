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
package io.vertigo.xml;

import io.vertigo.core.Home;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.home.componentspace.data.BioManager;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

public final class XmlHomeTest {

	@Test
	public void HomeTest() {
		final AppConfig componentSpaceConfig = new AppConfigBuilder()
				.withSilence(false)
				.withLoader(new XMLModulesLoader(XmlHomeTest.class.getResource("bio.xml"), new Properties()))
				.build();

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
