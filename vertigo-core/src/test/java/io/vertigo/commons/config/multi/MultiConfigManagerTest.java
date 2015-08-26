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
package io.vertigo.commons.config.multi;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.commons.config.ConfigManager;
import io.vertigo.commons.config.ServerConfig;
import io.vertigo.commons.impl.config.ConfigManagerImpl;
import io.vertigo.commons.plugins.config.properties.PropertiesConfigPlugin;
import io.vertigo.commons.plugins.config.xml.XmlConfigPlugin;
import io.vertigo.commons.plugins.resource.java.ClassPathResourceResolverPlugin;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.impl.resource.ResourceManagerImpl;
import io.vertigo.core.resource.ResourceManager;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author prahmoune
 */
public final class MultiConfigManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private ConfigManager configManager;

	@Override
	protected AppConfig buildAppConfig() {
		// @formatter:off
		return new AppConfigBuilder()
			.beginModule("vertigo")
				.addComponent(ResourceManager.class, ResourceManagerImpl.class)
				.beginPlugin( ClassPathResourceResolverPlugin.class).endPlugin()
				.beginComponent(ConfigManager.class, ConfigManagerImpl.class).endComponent()
				.beginPlugin(XmlConfigPlugin.class)
					.addParam("url", "io/vertigo/commons/config/multi/app-config.xml")
				.endPlugin()
				.beginPlugin( PropertiesConfigPlugin.class)
					.addParam("url", "io/vertigo/commons/config/multi/app-config.properties")
					.addParam("configPath", "server.fr")
				.endPlugin()
				.beginPlugin( PropertiesConfigPlugin.class)
					.addParam("url", "io/vertigo/commons/config/multi/app-config2.properties")
					.addParam("configPath", "server.en")
				.endPlugin()
				.beginPlugin(XmlConfigPlugin.class)
					.addParam("url", "io/vertigo/commons/config/multi/app-config2.xml")
				.endPlugin()
			.endModule()
			.build();
		// @formatter:on
	}

	@Test(expected = Exception.class)
	public void testFail0() {
		configManager.getStringValue("completely", "wrong");
	}

	@Test
	public void testString() {
		final String value = configManager.getStringValue("server.fr", "mail");
		Assert.assertEquals("john.doe@free.fr", value);
	}

	@Test
	public void testInt() {
		final int value = configManager.getIntValue("server.fr", "weight");
		Assert.assertEquals(55, value);
	}

	@Test
	public void testBoolean1() {
		final boolean value = configManager.getBooleanValue("server.en", "active");
		Assert.assertTrue(value);
	}

	@Test
	public void testBoolean2() {
		final boolean value = configManager.getBooleanValue("server.en", "closed");
		Assert.assertFalse(value);
	}

	@Test
	public void testBoolean3() {
		final boolean value = configManager.getBooleanValue("server.fr", "changed");
		Assert.assertTrue(value);
	}

	@Test
	public void testString2() {
		final String value = configManager.getStringValue("server", "name");
		Assert.assertEquals("monBeauServer", value);
	}

	@Test
	public void testResolve() {
		final ServerConfig serverConfig = configManager.resolve("server.en", ServerConfig.class);
		Assert.assertEquals("myBeautifullServer", serverConfig.getName());
		Assert.assertEquals(99, serverConfig.getPort());
		Assert.assertEquals("http://wwww/en", serverConfig.getHost());
		Assert.assertTrue(serverConfig.isActive());
	}
}
