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
package io.vertigo.core.spaces.component;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.core.spaces.component.data.BioManager;
import io.vertigo.core.spaces.component.data.BioManagerImpl;
import io.vertigo.core.spaces.component.data.MathManager;
import io.vertigo.core.spaces.component.data.MathManagerImpl;
import io.vertigo.core.spaces.component.data.MathPlugin;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

public final class ComponentSpace2Test extends AbstractTestCaseJU4 {
	@Inject
	private BioManager bioManager;

	@Test
	public void testCalcul() {
		final int res = bioManager.add(1, 2, 3);
		Assert.assertEquals(366, res);
	}

	//On vérifie que les composants ont bien été démarrés
	@Test
	public void testActive() {
		Assert.assertTrue(bioManager.isActive());
	}

	@Override
	protected AppConfig buildAppConfig() {
		// @formatter:off
		return new AppConfigBuilder()
		.beginModule("bio").
			beginComponent(BioManager.class, BioManagerImpl.class).endComponent()
			.beginComponent(MathManager.class, MathManagerImpl.class)
				.addParam("start", "100")
				.beginPlugin(MathPlugin.class)
					.addParam("factor", "20")
				.endPlugin()
			.endComponent()
		.endModule()
		.build();
		// @formatter:on
	}
}
