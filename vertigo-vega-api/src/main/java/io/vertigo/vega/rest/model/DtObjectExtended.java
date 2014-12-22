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
package io.vertigo.vega.rest.model;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;

import java.io.Serializable;
import java.util.HashMap;

/**
 * UiObjectExtended to extends an dtObject with meta data.
 */
public final class DtObjectExtended<D extends DtObject> extends HashMap<String, Serializable> {
	private static final long serialVersionUID = -8118714236186836600L;

	private final D innerObject;

	/**
	 * Constructor.
	 * @param dtObject inner object
	 */
	public DtObjectExtended(final D dtObject) {
		Assertion.checkNotNull(dtObject);
		//-----
		this.innerObject = dtObject;
	}

	/**
	 * @return Inner object
	 */
	public D getInnerObject() {
		return innerObject;
	}
}
