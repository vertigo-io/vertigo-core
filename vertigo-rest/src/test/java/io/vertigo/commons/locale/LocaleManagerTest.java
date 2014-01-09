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

import io.vertigo.AbstractTestCase2JU4;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.lang.MessageKey;
import io.vertigo.kernel.lang.MessageText;
import io.vertigoimpl.commons.locale.LocaleManagerImpl;
import io.vertigoimpl.engines.json.gson.GoogleJsonEngine;
import io.vertigoimpl.engines.rest.grizzly.GrizzlyRestEngine;

import java.io.Serializable;
import java.util.Locale;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author pchretien
 */
public final class LocaleManagerTest extends AbstractTestCase2JU4 {
	@Inject
	private LocaleManager localeManager;

	@Override
	protected void configMe(final ComponentSpaceConfigBuilder componentSpaceConfiguilder) {
		// @formatter:off
		componentSpaceConfiguilder
		.withJsonEngine(new GoogleJsonEngine())
		.withRestEngine(new GrizzlyRestEngine(8080))
		//.withCommandEngine(new VCommandEngineImpl(new GoogleJsonEngine(), 4400))
		.beginModule("spaces").
			beginComponent(LocaleManager.class, LocaleManagerImpl.class)
				//les locales doivent �tre s�par�es par des virgules
				.withParam("locales", "fr_FR, en , de_DE")
			.endComponent()
		.endModule();	
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
		try {
			Thread.sleep(100000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testOverride() {
		//On surcharge le dictionnaire city-guide avec un dictionnaire partiel
		localeManager.override("io.vertigo.commons.locale.popular-guide", CityGuide.values());

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
		//On v�rifie que l'on ne connait pas le japonais
		Assert.assertNull(localeManager.getMessage(CityGuide.HELLO, Locale.JAPANESE));
	}

	@Test
	public void testJapanese() {
		localeManager.registerLocaleProvider(new LocaleProvider() {
			public Locale getCurrentLocale() {
				return Locale.JAPANESE;
			}
		});
		//On v�rifie que l'on ne connait pas le japonais
		final MessageText helloTxt = new MessageText(CityGuide.HELLO);
		Assert.assertEquals("<<ja:HELLO>>", helloTxt.getDisplay());
	}

	@Test
	public void testDynamicMessageKey() {
		/* 
		 * On teste que l'on acc�de au dictionnaire par une cl� sous forme de chaine de caract�res.
		 */
		final MessageKey key = new MessageKey() {
			private static final long serialVersionUID = 4654362997955319282L;

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

			public String name() {
				return "UNKNOWN KEY";
			}
		};
		final MessageText helloTxt = new MessageText("bonjour par d�faut", key);
		Assert.assertEquals("bonjour par d�faut", helloTxt.getDisplay());
	}

	@Test
	public void testUnknownDynamicMessageKey() {
		final MessageKey key = new MessageKey() {
			private static final long serialVersionUID = 4654362997955319282L;

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
