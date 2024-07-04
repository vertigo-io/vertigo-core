/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2024, Vertigo.io, team@vertigo.io
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

import java.time.ZoneId;
import java.util.Locale;
import java.util.function.Supplier;

import io.vertigo.core.node.component.Manager;

/**
 * The localization manager for applications managed by Vertigo, 
 * supporting multilingual or more specifically, multidictionary management.
 *
 * External resources are managed in dictionaries, where each resource is identified by a unique key:
 * @see MessageKey. For a given component, the list of keys is ideally implemented as an enumeration.
 * Each key is associated with a resource file, also referred to as a dictionary.
 *
 * If a label is not found in a specific language, a "panic" message is returned indicating the requested language,
 * and a warning is logged.
 *
 * Example panic message:
 * MessageText(null,messageKey.TOTO) in 'fr_FR' : <<fr:TOTO>>
 * MessageText(null,messageKey.TOTO) in 'en' : <<en:TOTO>>
 *
 * Labels can be parameterized.
 *
 * @see LocaleMessageText to create labels connected to the dictionary.
 *
 * @author pchretien
 */
public interface LocaleManager extends Manager {

	/**
	 * Registers a strategy to choose a locale.
	 * 
	 * @param localeSupplier Supplies a locale in a given context.
	 */
	void registerLocaleSupplier(Supplier<Locale> localeSupplier);

	/**
	 * Registers a strategy to choose a time zone.
	 * 
	 * @param zoneSupplier Supplies a time zone in a given context.
	 */
	void registerZoneSupplier(Supplier<ZoneId> zoneSupplier);

	/**
	 * Adds a resource dictionary.
	 * All resources identified by a key must be present in the properties file.
	 * This method is not synchronized and should be called at application startup.
	 * 
	 * @param baseName Name and path of the properties file.
	 * @param enums Enumeration (enum) controlling the managed resources.
	 */
	void add(String baseName, LocaleMessageKey[] enums);

	/**
	 * Overrides a resource dictionary.
	 * This method is not synchronized and should be called at application startup.
	 * It is possible to override only one property or a specific dictionary.
	 * 
	 * @param baseName Name and path of the properties file.
	 * @param enums Enumeration (enum) controlling the managed resources.
	 */
	void override(String baseName, LocaleMessageKey[] enums);

	/**
	 * Retrieves the unformatted label of a message identified by its key.
	 * Returns null if the message is not found.
	 * 
	 * @param messageKey Message key.
	 * @param locale Locale.
	 * @return Unformatted message in the language of the locale.
	 */
	String getMessage(LocaleMessageKey messageKey, Locale locale);

	/**
	 * Retrieves the current locale, corresponding to the current user if available,
	 * otherwise corresponds to the application's locale.
	 * 
	 * @return Current locale.
	 */
	Locale getCurrentLocale();

	/**
	 * Retrieves the current time zone, corresponding to the current user if available,
	 * otherwise corresponds to the application's time zone.
	 * 
	 * @return Current time zone.
	 */
	ZoneId getCurrentZoneId();
}
