/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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

import io.vertigo.core.AbstractTestCaseJU5;
import io.vertigo.core.locale.data.CityGuide;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.NodeConfig;

/**
 * @author pchretien
 */
public final class LocaleManagerTest extends AbstractTestCaseJU5 {
	@Inject
	private LocaleManager localeManager;

	@Override
	protected NodeConfig buildNodeConfig() {
		//les locales doivent être séparées par des virgules
		final String locales = "fr_FR, en , de_DE";
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales(locales)
						.build())
				.build();
	}

	@Override
	public void doSetUp() {
		localeManager.add("io.vertigo.core.locale.data.city-guide", CityGuide.values());
	}

	@Test
	public void testDictionary() {
		Assertions.assertThrows(IllegalArgumentException.class,
				//On ne charge pas deux fois un dictionnaire
				() -> localeManager.add("io.vertigo.core.locale.data.city-guide", CityGuide.values()));
	}

	@Test
	public void testDefaultDisplay() {
		final LocaleMessageText helloTxt = LocaleMessageText.of(CityGuide.HELLO);
		assertEquals("bonjour", helloTxt.getDisplay());
	}

	@Test
	public void testOverride() {
		//On surcharge le dictionnaire city-guide avec un dictionnaire partiel
		localeManager.override("io.vertigo.core.locale.data.popular-guide", CityGuide.values());

		final LocaleMessageText helloTxt = LocaleMessageText.of(CityGuide.HELLO);
		assertEquals("salut", helloTxt.getDisplay());

		assertEquals("yah2", localeManager.getMessage(CityGuide.HELLO, Locale.ENGLISH));
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
		localeManager.registerLocaleSupplier(() -> Locale.GERMANY);
		assertEquals(Locale.GERMANY, localeManager.getCurrentLocale());
		final LocaleMessageText helloTxt = LocaleMessageText.of(CityGuide.HELLO);
		assertEquals("guten tag", helloTxt.getDisplay());

	}

	@Test
	public void testUnknown() {
		Assertions.assertThrows(IllegalStateException.class,
				//On vérifie que l'on ne connait pas le japonais
				() -> assertNull(localeManager.getMessage(CityGuide.HELLO, Locale.JAPANESE)));
	}

	@Test
	public void testJapanese() {
		localeManager.registerLocaleSupplier(() -> Locale.JAPANESE);
		//On vérifie que l'on ne connait pas le japonais et que l'on retombe sur la langue par défaut
		final LocaleMessageText helloTxt = LocaleMessageText.of(CityGuide.HELLO);
		assertEquals("bonjour", helloTxt.getDisplay());
	}

	@Test
	public void testDynamicMessageKey() {
		/*
		 * On teste que l'on accède au dictionnaire par une clé sous forme de chaine de caractères.
		 */
		final LocaleMessageKey key = () -> "HELLO";

		assertEquals("bonjour", localeManager.getMessage(key, Locale.FRANCE));
		assertEquals("guten tag", localeManager.getMessage(key, Locale.GERMANY));
		assertEquals("hello", localeManager.getMessage(key, Locale.ENGLISH));
	}

	@Test
	public void testDefaultDynamicMessageKey() {
		final LocaleMessageKey key = () -> "UNKNOWN KEY";

		final LocaleMessageText helloTxt = LocaleMessageText.ofDefaultMsg("bonjour par défaut", key);
		assertEquals("bonjour par défaut", helloTxt.getDisplay());
	}

	@Test
	public void testUnknownDynamicMessageKey() {
		final LocaleMessageKey key = () -> "UNKNOWN KEY";

		final LocaleMessageText helloTxt = LocaleMessageText.of(key);
		assertEquals("<<fr:UNKNOWN KEY>>", helloTxt.getDisplay());
	}

	@Test
	public void testMessageTextParams() {
		final LocaleMessageKey key = () -> "UNKNOWN KEY";

		final Serializable param = null;
		LocaleMessageText helloTxt = LocaleMessageText.of(key);
		assertEquals("<<fr:UNKNOWN KEY>>", helloTxt.getDisplay());

		helloTxt = LocaleMessageText.of(key, param);
		assertEquals("<<fr:UNKNOWN KEY[null]>>", helloTxt.getDisplay());

		//		helloTxt = new MessageText(key, null);
		//		assertEquals("<<fr:UNKNOWN KEY[null]>>", helloTxt.getDisplay());

		helloTxt = LocaleMessageText.of(key, null, null);
		assertEquals("<<fr:UNKNOWN KEY[null, null]>>", helloTxt.getDisplay());

		helloTxt = LocaleMessageText.of("default");
		assertEquals("default", helloTxt.getDisplay());

		//		helloTxt = new MessageText("default", null, null);
		//		assertEquals("default", helloTxt.getDisplay());
	}
}
