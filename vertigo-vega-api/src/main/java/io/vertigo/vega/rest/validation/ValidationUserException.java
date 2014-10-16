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
package io.vertigo.vega.rest.validation;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.MessageText;
import io.vertigo.core.lang.VUserException;
import io.vertigo.dynamo.domain.model.DtObject;

/**
* @author npiedeloup 
*/
public final class ValidationUserException extends VUserException {
	private static final long serialVersionUID = 7214302356640340103L;

	private static final MessageText VALIDATE_ERROR_MESSAGE_TEXT = new MessageText("Il y a des erreurs, vous devez corriger votre saisie :", null);

	private final boolean oneField;
	private final DtObject dto;
	private final String fieldName;

	public ValidationUserException() {
		super(VALIDATE_ERROR_MESSAGE_TEXT);
		oneField = false;
		dto = null;
		fieldName = null;
	}

	public ValidationUserException(final MessageText messageText, final DtObject dto, final String fieldName) {
		super(messageText);
		Assertion.checkNotNull(dto, "L'objet est obligatoire");
		Assertion.checkArgNotEmpty(fieldName, "Le champs est obligatoire");
		//---------------------------------------------------------------------
		oneField = true;
		this.dto = dto;
		this.fieldName = fieldName;
	}

	public void flushToUiMessageStack(final UiMessageStack uiMessageStack) {
		if (oneField) {
			uiMessageStack.error(getMessage(), dto, fieldName);
		} //else : already in UiMessageStack
	}

}
