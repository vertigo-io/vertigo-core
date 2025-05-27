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

import java.time.ZoneId;
import java.util.Locale;
import java.util.function.Supplier;

import io.vertigo.core.node.component.Manager;

/**
 * Manages localization and internationalization in Vertigo applications.
 * Objectives:
 * - Handle multiple languages and dictionaries
 * - Support dynamic locale selection
 * - Manage timezone preferences
 * - Enable resource overriding
 * - Provide fallback for missing translations
 * 
 * Resources are stored in property files and accessed by enum keys.
 * Missing translations return a formatted panic message: <<lang:KEY>>
 *
 * @author pchretien
 */
public interface LocaleManager extends Manager {

	/**
	 * Sets locale selection strategy.
	 * 
	 * @param localeSupplier Strategy for locale selection
	 */
	void registerLocaleSupplier(Supplier<Locale> localeSupplier);

	/**
	 * Sets timezone selection strategy.
	 * 
	 * @param zoneSupplier Strategy for timezone selection
	 */
	void registerZoneSupplier(Supplier<ZoneId> zoneSupplier);

	/**
	 * Adds base resource dictionary.
	 * All keys must exist in properties file.
	 * Call at startup only - not thread safe.
	 * 
	 * @param baseName Properties file path
	 * @param enums Message key enums
	 */
	void add(String baseName, LocaleMessageKey[] enums);

	/**
	 * Overrides existing dictionary entries.
	 * Call at startup only - not thread safe.
	 * 
	 * @param baseName Properties file path
	 * @param enums Message key enums
	 */
	void override(String baseName, LocaleMessageKey[] enums);

	/**
	 * Gets raw message for key and locale.
	 * Returns null if not found.
	 * 
	 * @param messageKey Message identifier
	 * @param locale Target locale
	 * @return Raw message or null
	 */
	String getMessage(LocaleMessageKey messageKey, Locale locale);

	/**
	 * Gets current locale from context.
	 * Falls back to application default.
	 * 
	 * @return Active locale
	 */
	Locale getCurrentLocale();

	/**
	 * Gets current timezone from context.
	 * Falls back to application default.
	 * 
	 * @return Active timezone
	 */
	ZoneId getCurrentZoneId();
}
