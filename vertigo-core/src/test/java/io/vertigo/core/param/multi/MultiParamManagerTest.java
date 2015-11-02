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
package io.vertigo.core.param.multi;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.param.ParamManager;
import io.vertigo.core.param.ServerConfig;
import io.vertigo.core.plugins.param.properties.PropertiesParamPlugin;
import io.vertigo.core.plugins.param.xml.XmlParamPlugin;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author prahmoune
 */
public final class MultiParamManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private ParamManager paramManager;

	@Override
	protected AppConfig buildAppConfig() {
		final String locales = "fr_FR";

		// @formatter:off
		return new AppConfigBuilder()
			.beginBootModule(locales)
				.addPlugin( ClassPathResourceResolverPlugin.class)
				.beginPlugin(XmlParamPlugin.class)
					.addParam("url", "io/vertigo/core/param/multi/app-config.xml")
				.endPlugin()
				.beginPlugin( PropertiesParamPlugin.class)
					.addParam("url", "io/vertigo/core/param/multi/app-config.properties")
					.addParam("configPath", "server.fr")
				.endPlugin()
				.beginPlugin( PropertiesParamPlugin.class)
					.addParam("url", "io/vertigo/core/param/multi/app-config2.properties")
					.addParam("configPath", "server.en")
				.endPlugin()
				.beginPlugin(XmlParamPlugin.class)
					.addParam("url", "io/vertigo/core/param/multi/app-config2.xml")
				.endPlugin()
			.endModule()
			.build();
		// @formatter:on
	}

	@Test(expected = Exception.class)
	public void testFail0() {
		paramManager.getStringValue("completely", "wrong");
	}

	@Test
	public void testString() {
		final String value = paramManager.getStringValue("server.fr", "mail");
		Assert.assertEquals("john.doe@free.fr", value);
	}

	@Test
	public void testInt() {
		final int value = paramManager.getIntValue("server.fr", "weight");
		Assert.assertEquals(55, value);
	}

	@Test
	public void testBoolean1() {
		final boolean value = paramManager.getBooleanValue("server.en", "active");
		Assert.assertTrue(value);
	}

	@Test
	public void testBoolean2() {
		final boolean value = paramManager.getBooleanValue("server.en", "closed");
		Assert.assertFalse(value);
	}

	@Test
	public void testBoolean3() {
		final boolean value = paramManager.getBooleanValue("server.fr", "changed");
		Assert.assertTrue(value);
	}

	@Test
	public void testString2() {
		final String value = paramManager.getStringValue("server", "name");
		Assert.assertEquals("monBeauServer", value);
	}

	@Test
	public void testResolve() {
		final ServerConfig serverConfig = paramManager.resolve("server.en", ServerConfig.class);
		Assert.assertEquals("myBeautifullServer", serverConfig.getName());
		Assert.assertEquals(99, serverConfig.getPort());
		Assert.assertEquals("http://wwww/en", serverConfig.getHost());
		Assert.assertTrue(serverConfig.isActive());
	}
}
