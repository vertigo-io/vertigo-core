/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.app.config.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.invoke.MethodHandles.Lookup;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.app.Home;
import io.vertigo.app.config.discovery.data.DiscoveryA;
import io.vertigo.app.config.discovery.data.DiscoveryB;
import io.vertigo.app.config.discovery.data.DiscoveryD;
import io.vertigo.core.component.ComponentSpace;
import io.vertigo.util.AbstractTestCaseJU4;

/**
* @author pchretien
*/
@RunWith(JUnitPlatform.class)
public final class DiscoveryTest extends AbstractTestCaseJU4 {
	
	@Override
	protected Map<String, Function<Class, Lookup>> getPrivateLookups() {
		return Map.of("proxyMethod", AbstractTestCaseJU4.getCoreLookup());
	}

	@Test
	public void testComponentSpace() {
		final ComponentSpace componentSpace = Home.getApp().getComponentSpace();
		assertEquals(componentSpace.keySet().size(), 2 + 3); //ParamManager and ResourceManager are automaticaly declared
		final DiscoveryB discoveryB = componentSpace.resolve(DiscoveryB.class);
		//---
		assertTrue(DiscoveryA.class.getName().equals(discoveryB.getClass().getName()));
		//---
		final DiscoveryD discoveryD = componentSpace.resolve(DiscoveryD.class);
		assertTrue(discoveryD.isTrue());
	}

}
