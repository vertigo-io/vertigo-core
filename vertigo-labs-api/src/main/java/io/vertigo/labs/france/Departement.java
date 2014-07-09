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
package io.vertigo.labs.france;

import io.vertigo.kernel.lang.Assertion;

/**
 * @author pchretien
 */
public final class Departement /*implements DtObject*/{
	private final String code;
	private final String label;
	private final Region region;

	public Departement(String code, String label, Region region) {
		Assertion.checkArgNotEmpty(code);
		Assertion.checkArgNotEmpty(label);
		Assertion.checkNotNull(region);
		//--------------------------------------------------------------------
		this.code = code;
		this.label = label;
		this.region = region;
	}

	public String getLabel() {
		return label;
	}

	public String getCode() {
		return code;
	}

	public Region getRegion() {
		return region;
	}
}
