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
package io.vertigo.core.component.aop;

import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.app.AutoCloseableApp;
import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.xml.XMLAppConfigBuilder;

@RunWith(JUnitPlatform.class)
public final class Aspect2Test {

	@Test
	public final void testLoadComponentsWithoutDeclaredAspects() {
		Assertions.assertThrows(IllegalStateException.class,
				() -> {
					try (final AutoCloseableApp app = new AutoCloseableApp(buildAppConfig())) {
						//nop
					}
				});
	}

	private AppConfig buildAppConfig() {
		//si présent on récupère le paramétrage du fichier externe de paramétrage log4j
		return new XMLAppConfigBuilder()
				.withModules(getClass(), new Properties(), getManagersXmlFileName())
				.build();
	}

	private static String[] getManagersXmlFileName() {
		return new String[] { "./managers-without-aspects-test.xml", };
	}

}
