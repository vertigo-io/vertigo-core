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
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.param.ParamManager;
import io.vertigo.core.param.ServerConfig;
import io.vertigo.core.plugins.param.xml.XmlParamPlugin;
import io.vertigo.core.plugins.resource.classpath.ClassPathResourceResolverPlugin;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author prahmoune
 */
public final class XmlParamManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private ParamManager paramManager;

	@Override
	protected AppConfig buildAppConfig() {
		final String locales = "fr_FR";
		//@formatter:off
		return new AppConfigBuilder()
			.beginBootModule(locales)
				.addPlugin(ClassPathResourceResolverPlugin.class)
				.beginPlugin( XmlParamPlugin.class)
					.addParam("url", "io/vertigo/core/param/xml/basic-app-config.xml")
				.endPlugin()
			.endModule()
			.build();
		// @formatter:on
	}

	@Test(expected = Exception.class)
	public void test0() {
		paramManager.getStringValue("completely", "wrong");
	}

	@Test
	public void test1() {
		final String value = paramManager.getStringValue("test", "prop1");
		Assert.assertEquals("prop1val", value);
	}

	@Test(expected = Exception.class)
	public void test2() {
		paramManager.getStringValue("test2", "wrong");
	}

	@Test
	public void test3() {
		final int value = paramManager.getIntValue("test3", "intProp");
		Assert.assertEquals(12, value);
	}

	@Test(expected = Exception.class)
	public void test4() {
		paramManager.getIntValue("test4", "intBadProp");
	}

	@Test
	public void test5() {
		final boolean value = paramManager.getBooleanValue("test5", "boolProp");
		Assert.assertTrue(value);
	}

	@Test
	public void test6() {
		final boolean value = paramManager.getBooleanValue("test6", "boolBadProp1");
		Assert.assertTrue(value);
	}

	@Test(expected = Exception.class)
	public void test7() {
		final boolean b = paramManager.getBooleanValue("test7", "boolBadProp2");
		nop(b);
	}

	@Test
	public void test8() {
		final ServerConfig serverConfig = paramManager.resolve("server", ServerConfig.class);
		Assert.assertEquals("monBeauServer", serverConfig.getName());
		Assert.assertEquals(99, serverConfig.getPort());
		Assert.assertEquals("http://wwww", serverConfig.getHost());
		Assert.assertTrue(serverConfig.isActive());
	}
}
