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
package io.vertigo.core.param.hierarchy;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.commons.plugins.config.xml.XmlConfigPlugin;
import io.vertigo.commons.plugins.resource.java.ClassPathResourceResolverPlugin;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.param.ConfigManager;
import io.vertigo.core.param.ServerConfig;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author prahmoune
 */
public final class ConfigManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private ConfigManager configManager;

	@Override
	protected AppConfig buildAppConfig() {
		final String locales = "fr_FR";

		// @formatter:off
		return new AppConfigBuilder()
			.beginBootModule(locales)
				.addPlugin( ClassPathResourceResolverPlugin.class)
				.beginPlugin(XmlConfigPlugin.class)
					.addParam("url", "io/vertigo/core/param/hierarchy/basic-app-config.xml")
				.endPlugin()
			.endModule()
			.build();
		// @formatter:on
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFailPath() {
		//On vérifie que le path doit respecter la regex @See ConfigManager.REGEX_PATH
		final String value = configManager.getStringValue("Server", "name");
		nop(value);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFailPath2() {
		//On vérifie que le path doit respecter la regex @See ConfigManager.REGEX_PATH
		final String value = configManager.getStringValue("server.Fr", "name");
		nop(value);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFailProperty() {
		//On vérifie que la propert doit respecter la regex @See ConfigManager.REGEX_PROPERTY
		final String value = configManager.getStringValue("server", "Name");
		nop(value);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFailProperty2() {
		//On vérifie que la propert doit respecter la regex @See ConfigManager.REGEX_PROPERTY
		final String value = configManager.getStringValue("server", "name.first");
		nop(value);
	}

	@Test
	public void test0() {
		final String value = configManager.getStringValue("server", "name");
		Assert.assertEquals("monBeauServer", value);
	}

	@Test
	public void test1() {
		final String value = configManager.getStringValue("server.fr", "name");
		Assert.assertEquals("monBeauServer", value);
	}

	@Test
	public void test2() {
		final String value = configManager.getStringValue("server.fr.dev", "name");
		Assert.assertEquals("marecette", value);
	}

	@Test(expected = Exception.class)
	public void test3() {
		configManager.getStringValue("server", "host");
	}

	@Test
	public void test4() {
		final String value = configManager.getStringValue("server.fr", "host");
		Assert.assertEquals("http://wwww/fr", value);
	}

	@Test
	public void test5() {
		final String value = configManager.getStringValue("server.fr.unknown", "host");
		Assert.assertEquals("http://wwww/fr", value);
	}

	@Test(expected = Exception.class)
	public void testVo() {
		configManager.resolve("serverTest", ServerConfigVo.class);
		//Le résolve ne doit pas être possible : les Values Object ne sont pas gérés.");
	}

	@Test
	public void testBean0() {
		final ServerConfigBean serverConfig = configManager.resolve("serverTest", ServerConfigBean.class);
		Assert.assertEquals("monBeauServer", serverConfig.getName());
		Assert.assertEquals(99, serverConfig.getPort());
		Assert.assertEquals("http://wwww", serverConfig.getHost());
		Assert.assertTrue(serverConfig.isActive());
	}

	@Test(expected = Exception.class)
	public void testBean1() {
		configManager.resolve("server", ServerConfigBean.class);
		//"Le résolve ne doit pas être possible : il manque une propriété.");
	}

	@Test
	public void testBean2() {
		final ServerConfigBean serverConfig = configManager.resolve("server.fr", ServerConfigBean.class);
		Assert.assertEquals("monBeauServer", serverConfig.getName());
		Assert.assertEquals(99, serverConfig.getPort());
		Assert.assertEquals("http://wwww/fr", serverConfig.getHost());
		Assert.assertFalse(serverConfig.isActive());
	}

	@Test
	public void testBean3() {
		final ServerConfigBean serverConfig = configManager.resolve("server.en", ServerConfigBean.class);
		Assert.assertEquals("monBeauServer", serverConfig.getName());
		Assert.assertEquals(99, serverConfig.getPort());
		Assert.assertEquals("http://wwww/en", serverConfig.getHost());
		Assert.assertFalse(serverConfig.isActive());
	}

	@Test
	public void testBean4() {
		final ServerConfigBean serverConfig = configManager.resolve("server.fr.dev", ServerConfigBean.class);
		Assert.assertEquals("marecette", serverConfig.getName());
		Assert.assertEquals(8080, serverConfig.getPort());
		Assert.assertEquals("http://wwww/fr", serverConfig.getHost());
		Assert.assertTrue(serverConfig.isActive());
	}

	@Test
	public void testBean5() {
		final ServerConfigBean serverConfig = configManager.resolve("server.fr.prod", ServerConfigBean.class);
		Assert.assertEquals("monsite", serverConfig.getName());
		Assert.assertEquals(80, serverConfig.getPort());
		Assert.assertEquals("http://wwww/fr", serverConfig.getHost());
		Assert.assertTrue(serverConfig.isActive());
	}

	@Test
	public void testBean6() {
		final ServerConfigBean serverConfig = configManager.resolve("server.fr.prod.unknown", ServerConfigBean.class);
		Assert.assertEquals("monsite", serverConfig.getName());
		Assert.assertEquals(80, serverConfig.getPort());
		Assert.assertEquals("http://wwww/fr", serverConfig.getHost());
		Assert.assertTrue(serverConfig.isActive());
	}

	@Test
	public void testInterface() {
		final ServerConfig serverConfig = configManager.resolve("server.fr.prod.unknown", ServerConfig.class);
		Assert.assertEquals("monsite", serverConfig.getName());
		Assert.assertEquals(80, serverConfig.getPort());
		Assert.assertEquals("http://wwww/fr", serverConfig.getHost());
		Assert.assertTrue(serverConfig.isActive());
	}
}
