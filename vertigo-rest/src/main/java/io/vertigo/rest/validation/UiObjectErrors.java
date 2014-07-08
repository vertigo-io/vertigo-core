package io.vertigo.rest.validation;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageText;
import io.vertigo.kernel.util.StringUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Liste des erreurs d'un objet métier.
 * 
 * @author pchretien, npiedeloup
 * @version $Id: UiObjectErrors.java,v 1.5 2014/02/26 17:43:46 npiedeloup Exp $
 */
public final class UiObjectErrors {

	private final List<MessageText> objectErrors = new ArrayList<>();
	private final Map<DtField, List<MessageText>> fieldsErrors = new LinkedHashMap<>();
	private final DtObject dtObject;

	// ==========================================================================

	UiObjectErrors(final DtObject dtObject) {
		this.dtObject = dtObject;
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

	public void flushIntoAction(final UiMessageStack uiMessageStack) {
		for (final MessageText errorMessage : objectErrors) {
			uiMessageStack.addGlobalMessage(UiMessageStack.Level.ERROR, errorMessage.getDisplay());
		}
		for (final Map.Entry<DtField, List<MessageText>> entry : fieldsErrors.entrySet()) {
			for (final MessageText errorMessage : entry.getValue()) {
				uiMessageStack.addFieldMessage(UiMessageStack.Level.ERROR, errorMessage.getDisplay(), dtObject, getCamelCaseFieldName(entry.getKey()));
				//action.addActionError("<b>" + entry.getKey().getLabel().getDisplay() + "</b> : " + errorMessage.getDisplay());
			}
		}
	}
}
