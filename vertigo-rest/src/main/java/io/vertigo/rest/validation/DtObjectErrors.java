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
package io.vertigo.rest.validation;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;
import io.vertigo.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Liste des erreurs d'un objet métier.
 * @author pchretien, npiedeloup
 */
public final class DtObjectErrors {

	private final List<MessageText> objectErrors = new ArrayList<>();
	private final Map<DtField, List<MessageText>> fieldsErrors = new LinkedHashMap<>();
	private final String contextKey;

	// ==========================================================================

	DtObjectErrors(final String contextKey) {
		this.contextKey = contextKey;
	}

	public boolean hasError() {
		return !objectErrors.isEmpty() || !fieldsErrors.isEmpty();
	}

	public boolean hasError(final DtField dtField) {
		return fieldsErrors.containsKey(dtField);
	}

	void clearErrors(final DtField dtField) {
		Assertion.checkNotNull(dtField);
		//---------------------------------------------------------------------
		fieldsErrors.remove(dtField);
	}

	void clearErrors() {
		objectErrors.clear();
		fieldsErrors.clear();
	}

	public void addError(final MessageText messageText) {
		objectErrors.add(messageText);
	}

	public void addError(final DtField dtField, final MessageText messageText) {
		List<MessageText> errors = fieldsErrors.get(dtField);
		if (errors == null) {
			errors = new ArrayList<>();
			fieldsErrors.put(dtField, errors);
		}
		errors.add(messageText);
	}

	private final static String getCamelCaseFieldName(final DtField dtField) {
		return StringUtil.constToCamelCase(dtField.getName(), false);
	}

	public void flushIntoMessageStack(final UiMessageStack uiMessageStack) {
		for (final MessageText errorMessage : objectErrors) {
			uiMessageStack.addGlobalMessage(UiMessageStack.Level.ERROR, errorMessage.getDisplay());
		}
		for (final Map.Entry<DtField, List<MessageText>> entry : fieldsErrors.entrySet()) {
			for (final MessageText errorMessage : entry.getValue()) {
				uiMessageStack.addFieldMessage(UiMessageStack.Level.ERROR, errorMessage.getDisplay(), contextKey, getCamelCaseFieldName(entry.getKey()));
				//action.addActionError("<b>" + entry.getKey().getLabel().getDisplay() + "</b> : " + errorMessage.getDisplay());
			}
		}
	}
}
