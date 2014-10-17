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
package io.vertigo.vega.rest;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.lang.MessageText;
import io.vertigo.util.DateUtil;
import io.vertigo.vega.rest.validation.AbstractDtObjectValidator;
import io.vertigo.vega.rest.validation.DtObjectErrors;

import java.util.Date;

/**
 * Example of specific validator.
 * @author npiedeloup (9 juil. 2014 17:44:00)
 */
public class ContactValidator extends AbstractDtObjectValidator<Contact> {

	/** {@inheritDoc} */
	@Override
	protected void checkMonoFieldConstraints(final Contact dtObject, final DtField dtField, final DtObjectErrors dtObjectErrors) {
		final String camelCaseFieldName = getCamelCaseFieldName(dtField);
		if ("birthday".equals(camelCaseFieldName) && !dtObjectErrors.hasError(camelCaseFieldName)) {
			final Date birthday = dtObject.getBirthday();
			if (DateUtil.daysBetween(birthday, DateUtil.newDate()) < (16 * 365)) { //if less than 16
				dtObjectErrors.addError(camelCaseFieldName, new MessageText("You can't add contact younger than 16", null));
			}
		}
	}
}
