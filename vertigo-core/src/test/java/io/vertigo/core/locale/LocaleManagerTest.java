/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.AbstractTestCaseJU5;
import io.vertigo.core.locale.data.CityGuide;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.NodeConfig;
import jakarta.inject.Inject;

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

	// --- Zone tests ---

	@Test
	public void testDefaultZoneId() {
		//Par defaut, le zoneId doit etre celui du systeme
		assertEquals(ZoneId.systemDefault(), localeManager.getCurrentZoneId());
	}

	@Test
	public void testRegisterZoneSupplier() {
		final ZoneId tokyo = ZoneId.of("Asia/Tokyo");
		localeManager.registerZoneSupplier(() -> tokyo);
		assertEquals(tokyo, localeManager.getCurrentZoneId());
	}

	@Test
	public void testZoneSupplierWithSpecificZone() {
		//On verifie que le supplier avec un ZoneId specifique fonctionne correctement
		final ZoneId newYork = ZoneId.of("America/New_York");
		localeManager.registerZoneSupplier(() -> newYork);
		assertEquals(newYork, localeManager.getCurrentZoneId());
	}

	@Test
	public void testZoneSupplierOverridesDefault() {
		//Le supplier doit avoir priorite sur le zoneId par defaut
		final ZoneId london = ZoneId.of("Europe/London");
		localeManager.registerZoneSupplier(() -> london);
		assertEquals(london, localeManager.getCurrentZoneId());
	}

	@Test
	public void testZoneSupplierReturningNull() {
		//Si le supplier retourne null, on doit retomber sur le zone par defaut
		localeManager.registerZoneSupplier(() -> null);
		assertEquals(ZoneId.systemDefault(), localeManager.getCurrentZoneId());
	}

	// --- Double registration tests ---

	@Test
	public void testDoubleRegisterLocaleSupplier() {
		localeManager.registerLocaleSupplier(() -> Locale.GERMANY);
		Assertions.assertThrows(IllegalArgumentException.class,
				//On ne peut pas enregistrer deux fois un locale supplier
				() -> localeManager.registerLocaleSupplier(() -> Locale.ENGLISH));
	}

	@Test
	public void testDoubleRegisterZoneSupplier() {
		localeManager.registerZoneSupplier(() -> ZoneId.of("UTC"));
		Assertions.assertThrows(IllegalArgumentException.class,
				//On ne peut pas enregistrer deux fois un zone supplier
				() -> localeManager.registerZoneSupplier(() -> ZoneId.of("Europe/Paris")));
	}

	// --- LocaleMessageText.getDisplayOpt() tests ---

	@Test
	public void testGetDisplayOptPresent() {
		final LocaleMessageText helloTxt = LocaleMessageText.of(CityGuide.HELLO);
		final Optional<String> displayOpt = helloTxt.getDisplayOpt();
		assertTrue(displayOpt.isPresent());
		assertEquals("bonjour", displayOpt.get());
	}

	@Test
	public void testGetDisplayOptWithDefaultMsg() {
		final LocaleMessageKey unknownKey = () -> "UNKNOWN KEY";
		final LocaleMessageText txt = LocaleMessageText.ofDefaultMsg("message par defaut", unknownKey);
		final Optional<String> displayOpt = txt.getDisplayOpt();
		assertTrue(displayOpt.isPresent());
		assertEquals("message par defaut", displayOpt.get());
	}

	@Test
	public void testGetDisplayOptAbsent() {
		final LocaleMessageKey unknownKey = () -> "UNKNOWN KEY";
		final LocaleMessageText txt = LocaleMessageText.of(unknownKey);
		//Pas de cle en dictionnaire, pas de message par defaut => Optional vide
		assertFalse(txt.getDisplayOpt().isPresent());
	}

	// --- ofDefaultMsg with existing key tests ---

	@Test
	public void testOfDefaultMsgWithExistingKey() {
		//Quand la cle existe, le message du dictionnaire est prioritaire sur le default
		final LocaleMessageText txt = LocaleMessageText.ofDefaultMsg("fallback", CityGuide.HELLO);
		assertEquals("bonjour", txt.getDisplay());
	}

	// --- All messages for all locales ---

	@Test
	public void testAllMessages() {
		assertEquals("bonjour", localeManager.getMessage(CityGuide.HELLO, Locale.FRANCE));
		assertEquals("au revoir", localeManager.getMessage(CityGuide.GOOD_BYE, Locale.FRANCE));
		assertEquals("ville", localeManager.getMessage(CityGuide.CITY, Locale.FRANCE));

		assertEquals("hello", localeManager.getMessage(CityGuide.HELLO, Locale.ENGLISH));
		assertEquals("good bye", localeManager.getMessage(CityGuide.GOOD_BYE, Locale.ENGLISH));
		assertEquals("city", localeManager.getMessage(CityGuide.CITY, Locale.ENGLISH));

		assertEquals("guten tag", localeManager.getMessage(CityGuide.HELLO, Locale.GERMANY));
		assertEquals("auf wiedersehen", localeManager.getMessage(CityGuide.GOOD_BYE, Locale.GERMANY));
		assertEquals("stadt ", localeManager.getMessage(CityGuide.CITY, Locale.GERMANY)); //trailing space in properties file
	}

	// --- Override preserves non-overridden keys ---

	@Test
	public void testOverrideKeepsNonOverriddenKeys() {
		//Le override ne surcharge que HELLO, les autres cles doivent rester intactes
		localeManager.override("io.vertigo.core.locale.data.popular-guide", CityGuide.values());

		//HELLO est surcharge
		assertEquals("salut", localeManager.getMessage(CityGuide.HELLO, Locale.FRANCE));
		//GOOD_BYE et CITY ne sont pas dans popular-guide => valeurs d'origine preservees
		assertEquals("au revoir", localeManager.getMessage(CityGuide.GOOD_BYE, Locale.FRANCE));
		assertEquals("ville", localeManager.getMessage(CityGuide.CITY, Locale.FRANCE));
	}

	// --- getMessage returns null for unknown key ---

	@Test
	public void testGetMessageReturnsNullForUnknownKey() {
		final LocaleMessageKey unknownKey = () -> "NONEXISTENT";
		assertNull(localeManager.getMessage(unknownKey, Locale.FRANCE));
	}

	// --- LocaleMessageText.toString ---

	@Test
	public void testLocaleMessageTextToString() {
		final LocaleMessageText helloTxt = LocaleMessageText.of(CityGuide.HELLO);
		//toString retourne le panic message (format <<locale:key>>)
		final String str = helloTxt.toString();
		assertTrue(str.contains("HELLO") || str.contains("bonjour"), "toString should contain the key or value");
	}
}
