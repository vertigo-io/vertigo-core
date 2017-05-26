/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.vega.webservice;

import java.util.Optional;

import javax.inject.Inject;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.vertigo.app.AutoCloseableApp;
import io.vertigo.commons.node.Node;
import io.vertigo.commons.node.NodeManager;
import io.vertigo.core.component.di.injector.DIInjector;
import io.vertigo.vega.webservice.data.MyAppConfig;

public final class DistantAppTest {

	private static AutoCloseableApp app;

	@Inject
	private NodeManager nodeManager;

	@BeforeClass
	public static void setUp() {
		app = new AutoCloseableApp(MyAppConfig.config());
	}

	@Before
	public void doBefore() {
		DIInjector.injectMembers(this, app.getComponentSpace());
	}

	@AfterClass
	public static void tearDown() {
		if (app != null) {
			app.close();
		}
	}

	@Test
	public void testDistantAppConfig() {

		final String currentNodeId = nodeManager.getCurrentNode().getId();
		final Optional<Node> appNode = nodeManager.find(currentNodeId);
		Assert.assertTrue(appNode.isPresent());

		Assert.assertTrue(nodeManager.getConfig().containsKey(currentNodeId));
	}

}
