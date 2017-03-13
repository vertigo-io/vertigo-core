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
package io.vertigo.core.component;

import java.util.Date;

import io.vertigo.lang.Assertion;

/**
 * The ComponentInfo class defines an info and its associated value.
 * 
 * Value may have only a few types. (boolean, string, numeric, date)
 *
 * @author npiedeloup
 */
public final class ComponentInfo {
	private final Object value;
	private final String title;

	/**
	 * Constructor.
	 * @param title Title which defines the componentInfo
	 * @param value Value
	 */
	public ComponentInfo(final String title, final boolean value) {
		this(title, value, false);
	}

	/**
	 * Constructor.
	 * @param title Title which defines the componentInfo
	 * @param value Value
	 */
	public ComponentInfo(final String title, final String value) {
		this(title, value, false);
	}

	/**
	 * Constructor.
	 * @param title Title which defines the componentInfo
	 * @param value Value
	 */
	public ComponentInfo(final String title, final Long value) {
		this(title, value, false);
	}

	/**
	 * Constructor.
	 * @param title Title which defines the componentInfo
	 * @param value Value
	 */
	public ComponentInfo(final String title, final Integer value) {
		this(title, value, false);
	}

	/**
	 * Constructor.
	 * @param title Title which defines the componentInfo
	 * @param value Value
	 */
	public ComponentInfo(final String title, final Double value) {
		this(title, value, false);
	}

	/**
	 * Constructor.
	 * @param title Title which defines the componentInfo
	 * @param value Value
	 */
	public ComponentInfo(final String title, final Date value) {
		this(title, value, false);
	}

	private ComponentInfo(final String title, final Object value, final boolean dummy) {
		Assertion.checkArgNotEmpty(title);
		//-----
		this.title = title;
		this.value = value;
	}

	/**
	 * Returns the title
	 * @return title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Returns the value
	 * @return value
	 */
	public Object getValue() {
		return value;
	}
}
