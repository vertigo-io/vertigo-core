package io.vertigo.rest;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtField.FieldType;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.kernel.lang.MessageText;
import io.vertigo.rest.validation.DtObjectErrors;
import io.vertigo.rest.validation.DtObjectValidator;

public class MandatoryPkValidator<O extends DtObject> extends DtObjectValidator<O> {

	@Override
	protected void checkMonoFieldConstraints(final O dtObject, final DtField dtField, final DtObjectErrors dtObjectErrors) {
		if (dtField.getType() == FieldType.PRIMARY_KEY && !dtObjectErrors.hasError(dtField)) {
			if (DtObjectUtil.getId(dtObject) == null) {
				dtObjectErrors.addError(dtField, new MessageText("Id is mandatory", null, null));
			}
		}
	}
}
