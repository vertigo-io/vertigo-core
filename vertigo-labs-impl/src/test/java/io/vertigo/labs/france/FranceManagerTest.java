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
package io.vertigo.labs.france;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.labs.france.FranceManager;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author pchretien
 */
public class FranceManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private FranceManager franceManager;

	@Test
	public final void testRegions() {
		Assert.assertEquals(27, franceManager.getRegions().size());
	}

	@Test
	public final void testRegion() {
		Assert.assertEquals("LORRAINE", franceManager.getRegion("41").getLabel().toUpperCase());
	}

	@Test
	public final void testDepartements() {
		Assert.assertEquals(101, franceManager.getDepartements().size());
	}

	@Test
	public final void testDepartement() {
		Assert.assertEquals("MEUSE", franceManager.getDepartement("55").getLabel().toUpperCase());
	}
}
