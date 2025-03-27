/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.param.multi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.AbstractParamManagerTest;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.param.manifest.ManifestParamPlugin;
import io.vertigo.core.plugins.param.properties.PropertiesParamPlugin;
import io.vertigo.core.plugins.param.xml.XmlParamPlugin;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;

/**
 * @author prahmoune
 */
public final class MultiParamManagerTest extends AbstractParamManagerTest {

	@Override
	protected NodeConfig buildNodeConfig() {
		final String locales = "fr_FR";

		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales(locales)
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.addPlugin(XmlParamPlugin.class,
								Param.of("url", "io/vertigo/core/param/multi/app-config.xml"))
						.addPlugin(PropertiesParamPlugin.class,
								Param.of("url", "io/vertigo/core/param/multi/app-config.properties"))
						.addPlugin(PropertiesParamPlugin.class,
								Param.of("url", "io/vertigo/core/param/multi/app-config2.properties"))
						.addPlugin(XmlParamPlugin.class,
								Param.of("url", "io/vertigo/core/param/multi/app-config2.xml"))
						.addPlugin(ManifestParamPlugin.class,
								Param.of("url", "META-INF/MANIFEST_test.MF"))
						.build())
				.build();
	}

	@Test
	public void testManifest() {
		final Param param = paramManager.getParam("App-Version");
		assertEquals("3.x.x", param.getValueAsString());
	}
}
