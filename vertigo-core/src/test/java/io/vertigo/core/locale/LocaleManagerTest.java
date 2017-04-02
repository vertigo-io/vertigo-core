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
package io.vertigo.core.locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.Serializable;
import java.util.Locale;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.AppConfigBuilder;
import io.vertigo.core.locale.data.CityGuide;
import io.vertigo.lang.MessageKey;
import io.vertigo.lang.MessageText;

/**
 * @author pchretien
 */
@RunWith(JUnitPlatform.class)
public final class LocaleManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private LocaleManager localeManager;

	@Override
	protected AppConfig buildAppConfig() {
		//les locales doivent être séparées par des virgules
		final String locales = "fr_FR, en , de_DE";
		return new AppConfigBuilder()
				.beginBoot()
				.withLocales(locales)
				.endBoot()
				.build();
	}

	@Override
	public void doSetUp() {
		localeManager.add("io.vertigo.core.locale.data.city-guide", CityGuide.values());
	}

	@Test
	public void testDictionary() {
		Assertions.assertThrows(IllegalStateException.class,
				//On ne charge pas deux fois un dictionnaire
				() -> localeManager.add("io.vertigo.core.locale.data.city-guide", CityGuide.values()));
	}

	@Test
	public void testDefaultDisplay() {
		final MessageText helloTxt = new MessageText(CityGuide.HELLO);
		assertEquals("bonjour", helloTxt.getDisplay());
	}

	@Test
	public void testOverride() {
		//On surcharge le dictionnaire city-guide avec un dictionnaire partiel
		localeManager.override("io.vertigo.core.locale.data.popular-guide", CityGuide.values());

		final MessageText helloTxt = new MessageText(CityGuide.HELLO);
		assertEquals("salut", helloTxt.getDisplay());
	}

	@Test
	public void testMessage() {
		assertEquals("bonjour", localeManager.getMessage(CityGuide.HELLO, Locale.FRANCE));
		assertEquals("guten tag", localeManager.getMessage(CityGuide.HELLO, Locale.GERMANY));
		assertEquals("hello", localeManager.getMessage(CityGuide.HELLO, Locale.ENGLISH));
	}

	@Test
	public void testCurrentLocale() {
		assertEquals(Locale.FRANCE, localeManager.getCurrentLocale());
	}

	@Test
	public void testLocaleProvider() {
		localeManager.registerLocaleProvider(() -> Locale.GERMANY);
		assertEquals(Locale.GERMANY, localeManager.getCurrentLocale());
		final MessageText helloTxt = new MessageText(CityGuide.HELLO);
		assertEquals("guten tag", helloTxt.getDisplay());

	}

	@Test
	public void testUnknown() {
		Assertions.assertThrows(IllegalArgumentException.class,
				//On vérifie que l'on ne connait pas le japonais
				() -> assertNull(localeManager.getMessage(CityGuide.HELLO, Locale.JAPANESE)));
	}

	@Test
	public void testJapanese() {
		localeManager.registerLocaleProvider(() -> Locale.JAPANESE);
		//On vérifie que l'on ne connait pas le japonais et que l'on retombe sur la langue par défaut
		final MessageText helloTxt = new MessageText(CityGuide.HELLO);
		assertEquals("bonjour", helloTxt.getDisplay());
	}

	@Test
	public void testDynamicMessageKey() {
		/*
		 * On teste que l'on accède au dictionnaire par une clé sous forme de chaine de caractères.
		 */
		final MessageKey key = new MessageKey() {
			private static final long serialVersionUID = 4654362997955319282L;

			@Override
			public String name() {
				return "HELLO";
			}
		};
		assertEquals("bonjour", localeManager.getMessage(key, Locale.FRANCE));
		assertEquals("guten tag", localeManager.getMessage(key, Locale.GERMANY));
		assertEquals("hello", localeManager.getMessage(key, Locale.ENGLISH));
	}

	@Test
	public void testDefaultDynamicMessageKey() {
		final MessageKey key = new MessageKey() {
			private static final long serialVersionUID = 4654362997955319282L;

			@Override
			public String name() {
				return "UNKNOWN KEY";
			}
		};
		final MessageText helloTxt = new MessageText("bonjour par défaut", key);
		assertEquals("bonjour par défaut", helloTxt.getDisplay());
	}

	@Test
	public void testUnknownDynamicMessageKey() {
		final MessageKey key = new MessageKey() {
			private static final long serialVersionUID = 4654362997955319282L;

			@Override
			public String name() {
				return "UNKNOWN KEY";
			}
		};
		final MessageText helloTxt = new MessageText(key);
		assertEquals("<<fr:UNKNOWN KEY>>", helloTxt.getDisplay());
	}

	@Test
	public void testMessageTextParams() {
		final MessageKey key = new MessageKey() {
			private static final long serialVersionUID = 4654362997955319282L;

			@Override
			public String name() {
				return "UNKNOWN KEY";
			}
		};
		final Serializable param = null;
		MessageText helloTxt = new MessageText(key);
		assertEquals("<<fr:UNKNOWN KEY>>", helloTxt.getDisplay());

		helloTxt = new MessageText(key, param);
		assertEquals("<<fr:UNKNOWN KEY[null]>>", helloTxt.getDisplay());

		//		helloTxt = new MessageText(key, null);
		//		assertEquals("<<fr:UNKNOWN KEY[null]>>", helloTxt.getDisplay());

		helloTxt = new MessageText(key, null, null);
		assertEquals("<<fr:UNKNOWN KEY[null, null]>>", helloTxt.getDisplay());

		helloTxt = new MessageText("default", null);
		assertEquals("default", helloTxt.getDisplay());

		//		helloTxt = new MessageText("default", null, null);
		//		assertEquals("default", helloTxt.getDisplay());
	}
}
