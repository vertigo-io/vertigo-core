/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.param.xml;

import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.AbstractParamManagerTest;
import io.vertigo.core.param.Param;
import io.vertigo.core.plugins.param.xml.XmlParamPlugin;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;

/**
 * @author pchretien
 */
public final class XmlParamManagerTest extends AbstractParamManagerTest {

	@Override
	protected NodeConfig buildNodeConfig() {
		final String locales = "fr_FR";
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales(locales)
						.addPlugin(ClassPathResourceResolverPlugin.class)
						.addPlugin(XmlParamPlugin.class,
								Param.of("url", "io/vertigo/core/param/xml/basic-app-config.xml"))
						.build())
				.build();
	}
}
