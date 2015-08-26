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
package io.vertigo.vega.plugins.rest.handler.converter;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.vega.rest.engine.UiList;
import io.vertigo.vega.rest.engine.UiListDelta;
import io.vertigo.vega.rest.engine.UiObject;
import io.vertigo.vega.rest.exception.VSecurityException;
import io.vertigo.vega.rest.metamodel.EndPointParam;

import java.util.Map;
import java.util.Set;

/**
 * @author npiedeloup
 */
final class UiObjectUtil {
	private static final String FORBIDDEN_OPERATION_FIELD_MODIFICATION = "Can't modify field:";

	private UiObjectUtil() {
		//nothing
	}

	static void postReadUiListDelta(final UiListDelta<DtObject> uiListDelta, final String inputKey, final EndPointParam endPointParam) throws VSecurityException {
		final String prefix = inputKey.length() > 0 ? inputKey + "." : "";
		for (final Map.Entry<String, UiObject<DtObject>> entry : uiListDelta.getCreatesMap().entrySet()) {
			final String uiObjectInputKey = prefix + entry.getKey();
			postReadUiObject(entry.getValue(), uiObjectInputKey, endPointParam);
		}
		for (final Map.Entry<String, UiObject<DtObject>> entry : uiListDelta.getUpdatesMap().entrySet()) {
			final String uiObjectInputKey = prefix + entry.getKey();
			postReadUiObject(entry.getValue(), uiObjectInputKey, endPointParam);
		}
		for (final Map.Entry<String, UiObject<DtObject>> entry : uiListDelta.getDeletesMap().entrySet()) {
			final String uiObjectInputKey = prefix + entry.getKey();
			postReadUiObject(entry.getValue(), uiObjectInputKey, endPointParam);
		}
	}

	static void postReadUiList(final UiList<DtObject> uiList, final String inputKey, final EndPointParam endPointParam) throws VSecurityException {
		final String prefix = inputKey.length() > 0 ? inputKey + "." : "";
		int index = 0;
		for (final UiObject<DtObject> entry : uiList) {
			final String uiObjectInputKey = prefix + "idx" + index;
			postReadUiObject(entry, uiObjectInputKey, endPointParam);
			index++;
		}
	}

	static void postReadUiObject(final UiObject<DtObject> uiObject, final String inputKey, final EndPointParam endPointParam) throws VSecurityException {
		uiObject.setInputKey(inputKey);
		checkUnauthorizedFieldModifications(uiObject, endPointParam);
	}

	private static void checkUnauthorizedFieldModifications(final UiObject<DtObject> uiObject, final EndPointParam endPointParam) throws VSecurityException {
		for (final String excludedField : endPointParam.getExcludedFields()) {
			if (uiObject.isModified(excludedField)) {
				throw new VSecurityException(FORBIDDEN_OPERATION_FIELD_MODIFICATION + excludedField);
			}
		}
		final Set<String> includedFields = endPointParam.getIncludedFields();
		if (!includedFields.isEmpty()) {
			for (final String modifiedField : uiObject.getModifiedFields()) {
				if (!includedFields.contains(modifiedField)) {
					throw new VSecurityException(FORBIDDEN_OPERATION_FIELD_MODIFICATION + modifiedField);
				}
			}
		}
	}

}
