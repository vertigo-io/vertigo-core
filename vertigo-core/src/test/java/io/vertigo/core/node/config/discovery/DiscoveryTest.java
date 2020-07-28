/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, Vertigo.io, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.core.node.config.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.vertigo.core.AbstractTestCaseJU5;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.ComponentSpace;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.node.config.discovery.data.DiscoveryA;
import io.vertigo.core.node.config.discovery.data.DiscoveryB;
import io.vertigo.core.node.config.discovery.data.DiscoveryD;
import io.vertigo.core.node.config.discovery.data.TrueProxyMethod;

/**
* @author pchretien
*/
public final class DiscoveryTest extends AbstractTestCaseJU5 {

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.verbose()
						.build())
				.addModule(ModuleConfig.builder("proxyMethod")
						.addProxyMethod(TrueProxyMethod.class)
						.build())
				.addModule(new TestModuleDiscoveryFeatures().build())
				.build();
	}

	@Test
	public void testComponentSpace() {
		final ComponentSpace componentSpace = Node.getNode().getComponentSpace();
		assertEquals(componentSpace.keySet().size(), 4 + 3); //ParamManager, ResourceManager, DaemonManager and AnalyticsManager are automaticaly declared
		final DiscoveryB discoveryB = componentSpace.resolve(DiscoveryB.class);
		//---
		assertTrue(DiscoveryA.class.getName().equals(discoveryB.getClass().getName()));
		//---
		final DiscoveryD discoveryD = componentSpace.resolve(DiscoveryD.class);
		assertTrue(discoveryD.isTrue());
	}

}
