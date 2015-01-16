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

import io.vertigo.dynamo.domain.metamodel.DtFieldName;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.MessageText;
import io.vertigo.lang.VUserException;
import io.vertigo.util.StringUtil;

/**
* @author npiedeloup
*/
public final class ValidationUserException extends VUserException {
	private static final long serialVersionUID = 7214302356640340103L;

	private static final MessageText VALIDATE_ERROR_MESSAGE_TEXT = new MessageText("Il y a des erreurs, vous devez corriger votre saisie :", null);

	private final boolean oneField;
	private final DtObject dto;
	private final String fieldName;

	/**
	 * Exception to launch already fill UiMessageStack.
	 */
	public ValidationUserException() {
		super(VALIDATE_ERROR_MESSAGE_TEXT);
		oneField = false;
		dto = null;
		fieldName = null;
	}

	/**
	 * Create a UserException on a field.
	 * @param messageText Message text
	 * @param dto object
	 * @param fieldName field
	 */
	public ValidationUserException(final MessageText messageText, final DtObject dto, final DtFieldName fieldName) {
		this(messageText, StringUtil.constToCamelCase(fieldName.name(), false), dto);
	}

	/**
	 * Create a UserException on a field.
	 * @param messageText Message text
	 * @param dto object
	 * @param fieldName fieldName in CamelCase
	 */
	public ValidationUserException(final MessageText messageText, final DtObject dto, final String fieldName) {
		this(messageText, fieldName, dto);
	}

	private ValidationUserException(final MessageText messageText, final String fieldName, final DtObject dto) {
		super(messageText);
		Assertion.checkNotNull(dto, "L'objet est obligatoire");
		Assertion.checkArgNotEmpty(fieldName, "Le champs est obligatoire");
		Assertion.checkArgument(fieldName.indexOf('_') == -1, "Le nom du champs doit être en camelCase ({0}).", fieldName);
		//-----
		oneField = true;
		this.dto = dto;
		this.fieldName = fieldName;
	}

	/**
	 * @param uiMessageStack UiMessageStack to fill
	 */
	public void flushToUiMessageStack(final UiMessageStack uiMessageStack) {
		if (oneField) {
			uiMessageStack.error(getMessage(), dto, fieldName);
		} //else : already in UiMessageStack
	}

}
