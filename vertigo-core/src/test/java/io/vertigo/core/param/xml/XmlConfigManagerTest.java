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
package io.vertigo.core.param.xml;

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
public final class XmlConfigManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private ConfigManager configManager;

	@Override
	protected AppConfig buildAppConfig() {
		final String locales = "fr_FR";
		//@formatter:off
		return new AppConfigBuilder()
			.beginBootModule(locales)
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.beginPlugin( XmlConfigPlugin.class)
					.addParam("url", "io/vertigo/core/param/xml/basic-app-config.xml")
				.endPlugin()
			.endModule()
			.build();
		// @formatter:on
	}

	@Test(expected = Exception.class)
	public void test0() {
		configManager.getStringValue("completely", "wrong");
	}

	@Test
	public void test1() {
		final String value = configManager.getStringValue("test", "prop1");
		Assert.assertEquals("prop1val", value);
	}

	@Test(expected = Exception.class)
	public void test2() {
		configManager.getStringValue("test2", "wrong");
	}

	@Test
	public void test3() {
		final int value = configManager.getIntValue("test3", "intProp");
		Assert.assertEquals(12, value);
	}

	@Test(expected = Exception.class)
	public void test4() {
		configManager.getIntValue("test4", "intBadProp");
	}

	@Test
	public void test5() {
		final boolean value = configManager.getBooleanValue("test5", "boolProp");
		Assert.assertTrue(value);
	}

	@Test
	public void test6() {
		final boolean value = configManager.getBooleanValue("test6", "boolBadProp1");
		Assert.assertTrue(value);
	}

	@Test(expected = Exception.class)
	public void test7() {
		final boolean b = configManager.getBooleanValue("test7", "boolBadProp2");
		nop(b);
	}

	@Test
	public void test8() {
		final ServerConfig serverConfig = configManager.resolve("server", ServerConfig.class);
		Assert.assertEquals("monBeauServer", serverConfig.getName());
		Assert.assertEquals(99, serverConfig.getPort());
		Assert.assertEquals("http://wwww", serverConfig.getHost());
		Assert.assertTrue(serverConfig.isActive());
	}
}
