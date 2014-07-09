package io.vertigo.rest;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.kernel.lang.MessageText;
import io.vertigo.kernel.util.DateUtil;
import io.vertigo.rest.validation.DtObjectErrors;
import io.vertigo.rest.validation.DtObjectValidator;

import java.util.Date;

/**
 * Example of specific validator.
 * @author npiedeloup (9 juil. 2014 17:44:00)
 */
public class ContactValidator extends DtObjectValidator<Contact> {

	@Override
	protected void checkMonoFieldConstraints(final Contact dtObject, final DtField dtField, final DtObjectErrors dtObjectErrors) {
		if ("birthday".equals(dtField.getName()) && !dtObjectErrors.hasError(dtField)) {
			final Date birthday = dtObject.getBirthday();
			if (DateUtil.daysBetween(birthday, new Date()) < (16 * 365)) { //if less than 16
				dtObjectErrors.addError(dtField, new MessageText("You can't add contact younger than 16", null, null));
			}
		}
	}
}
