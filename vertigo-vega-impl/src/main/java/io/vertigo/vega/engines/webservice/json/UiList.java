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
package io.vertigo.vega.engines.webservice.json;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.webservice.validation.DtObjectValidator;
import io.vertigo.vega.webservice.validation.UiMessageStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Delta operations on List.
 * @author npiedeloup (16 sept. 2014 18:13:55)
 * @param <D> Object type
 */
public final class UiList<D extends DtObject> extends ArrayList<UiObject<D>> {

	private static final long serialVersionUID = -8008715790791553036L;
	private final Class<D> objectType;

	/**
	 * @param objectType Object type
	 */
	UiList(final Class<D> objectType) {
		this.objectType = objectType;
	}

	/**
	 * @return Object type
	 */
	public Class<D> getObjectType() {
		return objectType;
	}

	/**
	 * Merged and validate input data and set error into message stack.
	 * @param dtObjectValidators Used validators, may depends on object type.
	 * @param uiMessageStack Message stack to update
	 * @return Updated and validated business object
	 */
	public DtList<D> mergeAndCheckInput(final List<DtObjectValidator<D>> dtObjectValidators, final UiMessageStack uiMessageStack) {
		Assertion.checkNotNull(dtObjectValidators);
		//-----
		final DtList<D> dtList = new DtList<>(objectType);
		for (final UiObject<D> element : this) {
			//entry.getValue().setInputKey(inputKey + "." + listName + "." + entry.getKey());
			final D dto = element.mergeAndCheckInput(dtObjectValidators, uiMessageStack);
			dtList.add(dto);
		}
		return dtList;
	}

	@Override
	public boolean equals(final Object o) {
		/* A list equals only the same list */
		return (o == this);
	}
}
