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
package io.vertigo.core.impl.locale;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.locale.LocaleManager;
import io.vertigo.core.locale.LocaleMessageKey;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.util.StringUtil;

/**
 * This class manages locales and provides localization services.
 *
 * @author pchretien
 */
public final class LocaleManagerImpl implements LocaleManager {
	private static final Logger LOG = LogManager.getLogger(LocaleManagerImpl.class);

	/**
	 * Set of keys not found to avoid logging them again.
	 * It is synchronized because it is a shared resource modified by all threads.
	 */
	private final Set<String> notFoundKeys = Collections.synchronizedSet(new HashSet<String>());

	//Bundle for the default locale.
	private final Map<Locale, Map<String, String>> dictionaries = new HashMap<>();

	/** list of managed locales. */
	private final List<Locale> locales;

	/** Default zone. */
	private final ZoneId defaultZoneId;

	/**
	 * Locale selection strategy.
	 * If no strategy or no locale found then default locale = first declared locale.
	 */
	private Supplier<Locale> localeSupplier;

	/**
	 * Zone selection strategy.
	 * If no strategy or no zone found then default zone.
	 */
	private Supplier<ZoneId> zoneSupplier;

	/**
	 * Constructor.
	 * Exceptions are always thrown.
	 * Field labels are not localized.
	 * The first Locale is used if no user is declared (batch cases).
	 * You must specify the list of locales as a string
	 * examples :
	 * Locale.french : 'fr'
	 * Locale.FRANCE + Locale.us : 'fr_FR,us'
	 * A locale is defined by a language{_Country{_Variant}}
	 *
	 * @param locales List of locales managed by the application.
	 * @param defaultZoneId Default ZoneId used by the application.
	 */
	@Inject
	public LocaleManagerImpl(@ParamValue("locales") final String locales, @ParamValue("defaultZoneId") final Optional<String> defaultZoneId) {
		Assertion.check()
				.isNotBlank(locales)
				.isNotNull(defaultZoneId);
		//-----
		this.locales = createLocales(locales);
		this.defaultZoneId = createDefaultZoneId(defaultZoneId);
		//-----
		Assertion.check()
				.isNotNull(this.locales)
				.isFalse(this.locales.isEmpty(), "At least one locale must be declared");
		//-----
		for (final Locale locale : this.locales) {
			dictionaries.put(locale, new HashMap<>());
		}
	}

	private static ZoneId createDefaultZoneId(final Optional<String> defaultZoneIdOpt) {
		return defaultZoneIdOpt
				.map(ZoneId::of)
				.orElseGet(ZoneId::systemDefault);
	}

	private static List<Locale> createLocales(final String locales) {
		return Stream.of(locales.split(","))
				.map(locale -> {
					final String[] loc = locale.trim().split("_");
					Assertion.check().isTrue(loc.length > 0, "Empty locale specified");
					final String country = loc.length > 1 ? loc[1] : "";
					final String variant = loc.length > 2 ? loc[2] : "";
					return new Locale(loc[0], country, variant);
				})
				.collect(Collectors.toUnmodifiableList());

	}

	/** {@inheritDoc} */
	@Override
	public void registerZoneSupplier(final Supplier<ZoneId> newZoneSupplier) {
		Assertion.check()
				.isNull(zoneSupplier, "zoneSupplier already registered")
				.isNotNull(newZoneSupplier);
		//---
		zoneSupplier = newZoneSupplier;
	}

	/** {@inheritDoc} */
	@Override
	public void registerLocaleSupplier(final Supplier<Locale> newLocaleSupplier) {
		Assertion.check()
				.isNull(localeSupplier, "localeSupplier already registered")
				.isNotNull(newLocaleSupplier);
		//---
		localeSupplier = newLocaleSupplier;
	}

	/** {@inheritDoc} */
	@Override
	public void add(final String baseName, final LocaleMessageKey[] enums) {
		add(baseName, enums, false);
	}

	/** {@inheritDoc} */
	@Override
	public void override(final String baseName, final LocaleMessageKey[] enums) {
		add(baseName, enums, true);
	}

	private void add(final String baseName, final LocaleMessageKey[] enums, final boolean override) {
		for (final Locale locale : locales) {
			//For each managed locale, we load the corresponding dictionary
			final ResourceBundle resourceBundle;
			try {
				resourceBundle = ResourceBundle.getBundle(baseName, locale);
			} catch (final MissingResourceException e) {
				if (override) {
					//If we are in override mode, we allow partial loading of dictionaries
					continue;
				}
				throw WrappedException.wrap(e, "The dictionary for the locale '{0}' is not set", locale);
			}
			//We found a dictionary
			check(resourceBundle, enums, override);
			load(locale, resourceBundle, override);
		}
	}

	private Map<String, String> getDictionary(final Locale locale) {
		Assertion.check().isTrue(dictionaries.containsKey(locale), "The locale {0} is not managed", locale);
		//---
		return dictionaries.get(locale);
	}

	private void load(final Locale locale, final ResourceBundle resourceBundle, final boolean override) {
		for (final String key : Collections.list(resourceBundle.getKeys())) {
			final String value = resourceBundle.getString(key);
			Assertion.check().isNotNull(value);
			final String oldValue = getDictionary(locale).put(key, value);
			if (!override) {
				Assertion.check().isNull(oldValue, "Value already set for {0}", key);
			}
		}
	}

	private void check(final ResourceBundle resourceBundle, final LocaleMessageKey[] enums, final boolean override) {
		//============================================
		//==We check that the lists are complete==
		//============================================
		final List<String> resourcesKeys = Arrays.stream(enums)
				.map(LocaleMessageKey::name)
				.toList();

		//1- All keys from the properties file are in the enum of resources
		for (final String key : Collections.list(resourceBundle.getKeys())) {
			if (!StringUtil.isBlank(key) && !resourcesKeys.contains(key)) {
				throw new IllegalStateException(
						"A key from the properties file (bundle '" + resourceBundle.getBaseBundleName() + "', locale '" + resourceBundle.getLocale() + "') is unknown : '" + key + "'");
			}
		}

		//2- All keys from the enum are in the properties file
		if (!override) {
			for (final String resourceKey : resourcesKeys) {
				if (!resourceBundle.containsKey(resourceKey)) {
					onResourceNotFound(resourceKey);
					throw new IllegalStateException("A resource is not declared in the properties file : " + resourceKey);
				}
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public String getMessage(final LocaleMessageKey messageKey, final Locale locale) {
		Assertion.check()
				.isNotNull(messageKey)
				.isNotNull(locale);
		//-----
		final String msg = getDictionary(locale).get(messageKey.name());
		//Abnormal case: where the resource is not present.
		if (msg == null) {
			//Abnormal case: where the resource is not present.
			onResourceNotFound(messageKey.name());
			return null;
		}
		return msg;
	}

	/** {@inheritDoc} */
	@Override
	public ZoneId getCurrentZoneId() {
		return (zoneSupplier != null && zoneSupplier.get() != null)
				? zoneSupplier.get()
				//If there is no user, we can pick the default zone.
				: defaultZoneId;
	}

	/** {@inheritDoc} */
	@Override
	public Locale getCurrentLocale() {
		if (localeSupplier != null && localeSupplier.get() != null) {
			final Locale currentLocale = localeSupplier.get();
			//We have to check if the currentLocale belongs to locales.
			if (!locales.contains(localeSupplier.get())) {
				LOG.error("CurrentLocale '{}' is not allowed, it must be in '{}'", currentLocale, locales);
				//So, we can pick the default language.
				return locales.get(0);
			}
			return currentLocale;
		}
		//If there is no user, we can pick the default language.
		return locales.get(0);
	}

	private void onResourceNotFound(final String key) {
		if (!notFoundKeys.contains(key)) {
			//	If the resource is not present then
			//  - we log that the resource was not found
			//  - we store the key to avoid relogging
			//and we continue the processing
			logResourceNotFound(key, locales.size() > 1);
			notFoundKeys.add(key);
		}
	}

	/**
	 * Event raised when an externalized resource is not found.
	 *
	 * @param resource Name of the externalized resource not found
	 * @param isMultiLocales If multilingual application
	 */
	private static void logResourceNotFound(final String resource, final boolean isMultiLocales) {
		if (isMultiLocales) {
			LOG.warn("Resource {} not found", resource);
		} else {
			LOG.info("Resource {} not found", resource);
		}
	}
}
