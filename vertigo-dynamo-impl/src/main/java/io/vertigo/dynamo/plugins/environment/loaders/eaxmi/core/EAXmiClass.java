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
package io.vertigo.dynamo.plugins.environment.loaders.eaxmi.core;

import io.vertigo.lang.Assertion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
* @author pforhan
*/
public final class EAXmiClass {
	private final String code;
	private final String packageName;
	private final List<EAXmiAttribute> keyAttributes;
	private final List<EAXmiAttribute> fieldAttributes;

	EAXmiClass(final String code, final String packageName, final List<EAXmiAttribute> keyAttributes, final List<EAXmiAttribute> fieldAttributes) {
		Assertion.checkArgNotEmpty(code);
		//Assertion.notEmpty(packageName);
		Assertion.checkNotNull(keyAttributes);
		Assertion.checkNotNull(fieldAttributes);
		//---------------------------------------------------------------------
		this.code = code;
		this.packageName = packageName;
		this.keyAttributes = Collections.unmodifiableList(new ArrayList<>(keyAttributes));
		this.fieldAttributes = Collections.unmodifiableList(new ArrayList<>(fieldAttributes));
	}

	/**
	 * @return Code.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return Nom du package.
	 */
	public String getPackageName() {
		return packageName;
	}

	/**
	 * @return Listes des champs identifiants (PK).
	 */
	public List<EAXmiAttribute> getKeyAttributes() {
		return keyAttributes;
	}

	/***
	 * @return Liste des champs non PK.
	 */
	public List<EAXmiAttribute> getFieldAttributes() {
		return fieldAttributes;
	}

}
