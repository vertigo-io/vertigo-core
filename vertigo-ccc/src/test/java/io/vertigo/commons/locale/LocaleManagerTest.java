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
package io.vertigo.commons.locale;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.commons.locale.data.CityGuide;
import io.vertigo.core.config.AppConfig;
import io.vertigo.core.config.AppConfigBuilder;
import io.vertigo.engines.command.TcpVCommandEngine;
import io.vertigo.lang.MessageText;
import io.vertigoimpl.commons.locale.LocaleManagerImpl;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author pchretien
 */
public final class LocaleManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private LocaleManager localeManager;

	@Override
	protected AppConfig buildAppConfig() {
		// @formatter:off
		return new AppConfigBuilder()
			//.withRestEngine(new GrizzlyRestEngine(8080))
			.withCommandEngine(new TcpVCommandEngine( 4406))
			.beginModule("spaces").
				beginComponent(LocaleManager.class, LocaleManagerImpl.class)
					//les locales doivent être séparées par des virgules
					.withParam("locales", "fr_FR, en , de_DE")
				.endComponent()
			.endModule()
			.build();
		// @formatter:on
	}

	@Before
	public void setup() {
		localeManager.add("io.vertigo.commons.locale.city-guide", CityGuide.values());
	}

	@Test(expected = IllegalStateException.class)
	public void testDictionary() {
		//On ne charge pas deux fois un dictionnaire
		localeManager.add("io.vertigo.commons.locale.city-guide", CityGuide.values());
	}

	@Test
	public void testDefaultDisplay() {
		final MessageText helloTxt = new MessageText(CityGuide.HELLO);
		Assert.assertEquals("bonjour", helloTxt.getDisplay());
	}
}
