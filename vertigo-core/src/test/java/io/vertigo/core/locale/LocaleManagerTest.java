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
package io.vertigo.core.locale;

import java.io.Serializable;
import java.util.Locale;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.app.config.AppConfig;
import io.vertigo.app.config.AppConfigBuilder;
import io.vertigo.core.locale.data.CityGuide;
import io.vertigo.lang.MessageKey;
import io.vertigo.lang.MessageText;

/**
 * @author pchretien
 */
public final class LocaleManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private LocaleManager localeManager;

	@Override
	protected AppConfig buildAppConfig() {
		//les locales doivent être séparées par des virgules
		final String locales = "fr_FR, en , de_DE";
		// @formatter:off
		return new AppConfigBuilder()
			.beginBootModule(locales).endModule()
			.build();
		// @formatter:on
	}

	@Override
	public void doSetUp() {
		localeManager.add("io.vertigo.core.locale.data.city-guide", CityGuide.values());
	}

	@Test(expected = IllegalStateException.class)
	public void testDictionary() {
		//On ne charge pas deux fois un dictionnaire
		localeManager.add("io.vertigo.core.locale.data.city-guide", CityGuide.values());
	}

	@Test
	public void testDefaultDisplay() {
		final MessageText helloTxt = new MessageText(CityGuide.HELLO);
		Assert.assertEquals("bonjour", helloTxt.getDisplay());
	}

	@Test
	public void testOverride() {
		//On surcharge le dictionnaire city-guide avec un dictionnaire partiel
		localeManager.override("io.vertigo.core.locale.data.popular-guide", CityGuide.values());

		final MessageText helloTxt = new MessageText(CityGuide.HELLO);
		Assert.assertEquals("salut", helloTxt.getDisplay());
	}

	@Test
	public void testMessage() {
		Assert.assertEquals("bonjour", localeManager.getMessage(CityGuide.HELLO, Locale.FRANCE));
		Assert.assertEquals("guten tag", localeManager.getMessage(CityGuide.HELLO, Locale.GERMANY));
		Assert.assertEquals("hello", localeManager.getMessage(CityGuide.HELLO, Locale.ENGLISH));
	}

	@Test
	public void testCurrentLocale() {
		Assert.assertEquals(Locale.FRANCE, localeManager.getCurrentLocale());
	}

	@Test
	public void testLocaleProvider() {
		localeManager.registerLocaleProvider(new LocaleProvider() {
			@Override
			public Locale getCurrentLocale() {
				return Locale.GERMANY;
			}
		});
		Assert.assertEquals(Locale.GERMANY, localeManager.getCurrentLocale());
		final MessageText helloTxt = new MessageText(CityGuide.HELLO);
		Assert.assertEquals("guten tag", helloTxt.getDisplay());

	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnknown() {
		//On vérifie que l'on ne connait pas le japonais
		Assert.assertNull(localeManager.getMessage(CityGuide.HELLO, Locale.JAPANESE));
	}

	@Test
	public void testJapanese() {
		localeManager.registerLocaleProvider(new LocaleProvider() {
			@Override
			public Locale getCurrentLocale() {
				return Locale.JAPANESE;
			}
		});
		//On vérifie que l'on ne connait pas le japonais et que l'on retombe sur la langue par défaut
		final MessageText helloTxt = new MessageText(CityGuide.HELLO);
		Assert.assertEquals("bonjour", helloTxt.getDisplay());
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
		Assert.assertEquals("bonjour", localeManager.getMessage(key, Locale.FRANCE));
		Assert.assertEquals("guten tag", localeManager.getMessage(key, Locale.GERMANY));
		Assert.assertEquals("hello", localeManager.getMessage(key, Locale.ENGLISH));
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
		Assert.assertEquals("bonjour par défaut", helloTxt.getDisplay());
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
		Assert.assertEquals("<<fr:UNKNOWN KEY>>", helloTxt.getDisplay());
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
		Assert.assertEquals("<<fr:UNKNOWN KEY>>", helloTxt.getDisplay());

		helloTxt = new MessageText(key, param);
		Assert.assertEquals("<<fr:UNKNOWN KEY[null]>>", helloTxt.getDisplay());

		//		helloTxt = new MessageText(key, null);
		//		Assert.assertEquals("<<fr:UNKNOWN KEY[null]>>", helloTxt.getDisplay());

		helloTxt = new MessageText(key, null, null);
		Assert.assertEquals("<<fr:UNKNOWN KEY[null, null]>>", helloTxt.getDisplay());

		helloTxt = new MessageText("default", null);
		Assert.assertEquals("default", helloTxt.getDisplay());

		//		helloTxt = new MessageText("default", null, null);
		//		Assert.assertEquals("default", helloTxt.getDisplay());
	}
}
